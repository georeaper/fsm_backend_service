package com.example.feature.user.repository

import com.example.feature.subscription.SubscriptionInactiveException
import com.example.feature.subscription.UserLimitReachedException
import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.AuthSessionTable
import com.example.models.authenticationModels.CompanySubscriptionTable
import com.example.models.authenticationModels.SubscriptionPlanTable
import com.example.models.authenticationModels.UserTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction

class AuthUserProvisioningRepositoryImpl : AuthUserProvisioningRepository {
    override fun createActiveUser(
        companyId: Int,
        username: String,
        email: String,
        passwordHash: String,
        role: String
    ): Int = transaction(DatabaseFactory.centralDb) {
        val entitlement = lockedEntitlement(companyId)
        requireCapacity(companyId, entitlement)

        if (UserTable.select { UserTable.username eq username }.limit(1).any()) {
            throw IllegalArgumentException("Username already exists")
        }
        if (UserTable.select { UserTable.email eq email }.limit(1).any()) {
            throw IllegalArgumentException("Email already exists")
        }

        UserTable.insert {
            it[UserTable.username] = username
            it[UserTable.email] = email
            it[password] = passwordHash
            it[UserTable.companyId] = companyId
            it[UserTable.role] = role
            it[status] = "active"
        }[UserTable.id]
    }

    override fun activateUser(companyId: Int, authUserId: Int): Boolean =
        transaction(DatabaseFactory.centralDb) {
            val user = UserTable.select {
                (UserTable.id eq authUserId) and (UserTable.companyId eq companyId)
            }.limit(1).firstOrNull() ?: return@transaction false

            if (user[UserTable.status] == "active") return@transaction true
            requireCapacity(companyId, lockedEntitlement(companyId))

            UserTable.update({
                (UserTable.id eq authUserId) and (UserTable.companyId eq companyId)
            }) {
                it[status] = "active"
                it[updatedAt] = nowUtc()
            } > 0
        }

    override fun deactivateUser(companyId: Int, authUserId: Int): Boolean =
        transaction(DatabaseFactory.centralDb) {
            val changed = UserTable.update({
                (UserTable.id eq authUserId) and
                    (UserTable.companyId eq companyId) and
                    (UserTable.status eq "active")
            }) {
                it[status] = "inactive"
                it[updatedAt] = nowUtc()
            } > 0

            if (changed) {
                val now = nowUtc()
                AuthSessionTable.update({
                    (AuthSessionTable.userId eq authUserId) and AuthSessionTable.revokedAt.isNull()
                }) {
                    it[revokedAt] = now
                    it[lastUsedAt] = now
                }
            }
            changed
        }

    override fun deleteProvisionedUser(companyId: Int, authUserId: Int) {
        transaction(DatabaseFactory.centralDb) {
            AuthSessionTable.deleteWhere { AuthSessionTable.userId eq authUserId }
            UserTable.deleteWhere {
                (UserTable.id eq authUserId) and (UserTable.companyId eq companyId)
            }
        }
    }

    override fun findUsers(
        companyId: Int,
        authUserIds: Collection<Int>
    ): Map<Int, AuthUserAccount> {
        if (authUserIds.isEmpty()) return emptyMap()
        return transaction(DatabaseFactory.centralDb) {
            UserTable.select {
                (UserTable.companyId eq companyId) and (UserTable.id inList authUserIds.toList())
            }.associate { row -> row[UserTable.id] to row.toAccount() }
        }
    }

    override fun findUser(companyId: Int, authUserId: Int): AuthUserAccount? =
        transaction(DatabaseFactory.centralDb) {
            UserTable.select {
                (UserTable.companyId eq companyId) and (UserTable.id eq authUserId)
            }.limit(1).firstOrNull()?.toAccount()
        }

    override fun updateUser(
        companyId: Int,
        authUserId: Int,
        username: String,
        email: String,
        passwordHash: String?,
        role: String
    ): AuthUserAccount? = transaction(DatabaseFactory.centralDb) {
        val previous = UserTable.select {
            (UserTable.companyId eq companyId) and (UserTable.id eq authUserId)
        }.forUpdate().limit(1).firstOrNull()?.toAccount() ?: return@transaction null

        if (UserTable.select {
                (UserTable.username eq username) and (UserTable.id neq authUserId)
            }.limit(1).any()
        ) throw IllegalArgumentException("Username already exists")
        if (UserTable.select {
                (UserTable.email eq email) and (UserTable.id neq authUserId)
            }.limit(1).any()
        ) throw IllegalArgumentException("Email already exists")

        UserTable.update({
            (UserTable.companyId eq companyId) and (UserTable.id eq authUserId)
        }) {
            it[UserTable.username] = username
            it[UserTable.email] = email
            it[UserTable.role] = role
            passwordHash?.let { hash -> it[password] = hash }
            it[updatedAt] = nowUtc()
        }
        if (passwordHash != null) revokeSessions(authUserId)
        previous
    }

    override fun restoreUser(companyId: Int, account: AuthUserAccount) {
        transaction(DatabaseFactory.centralDb) {
            UserTable.update({
                (UserTable.companyId eq companyId) and (UserTable.id eq account.id)
            }) {
                it[username] = account.username
                it[email] = account.email
                it[password] = account.passwordHash
                it[role] = account.role
                it[status] = account.status
                it[updatedAt] = nowUtc()
            }
        }
    }

    override fun updatePassword(companyId: Int, authUserId: Int, passwordHash: String): Boolean =
        transaction(DatabaseFactory.centralDb) {
            val changed = UserTable.update({
                (UserTable.companyId eq companyId) and
                    (UserTable.id eq authUserId) and
                    (UserTable.status eq "active")
            }) {
                it[password] = passwordHash
                it[updatedAt] = nowUtc()
            } > 0
            if (changed) revokeSessions(authUserId)
            changed
        }

    private fun revokeSessions(authUserId: Int) {
        val now = nowUtc()
        AuthSessionTable.update({
            (AuthSessionTable.userId eq authUserId) and AuthSessionTable.revokedAt.isNull()
        }) {
            it[revokedAt] = now
            it[lastUsedAt] = now
        }
    }

    private fun lockedEntitlement(companyId: Int): ResultRow {
        val row = (CompanySubscriptionTable innerJoin SubscriptionPlanTable)
            .select { CompanySubscriptionTable.companyId eq companyId }
            .forUpdate()
            .limit(1)
            .firstOrNull()
            ?: throw SubscriptionInactiveException()

        if (row[CompanySubscriptionTable.status] !in ACTIVE_SUBSCRIPTION_STATUSES ||
            !row[SubscriptionPlanTable.active]
        ) {
            throw SubscriptionInactiveException()
        }
        return row
    }

    private fun requireCapacity(companyId: Int, entitlement: ResultRow) {
        val activeUsers = UserTable.select {
            (UserTable.companyId eq companyId) and (UserTable.status eq "active")
        }.count()
        val allowedUsers = entitlement[SubscriptionPlanTable.includedUsers] +
            entitlement[CompanySubscriptionTable.additionalUserSeats]
        if (activeUsers >= allowedUsers) {
            throw UserLimitReachedException(activeUsers, allowedUsers)
        }
    }

    private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    private fun ResultRow.toAccount() = AuthUserAccount(
        id = this[UserTable.id],
        username = this[UserTable.username],
        email = this[UserTable.email],
        passwordHash = this[UserTable.password],
        role = this[UserTable.role],
        status = this[UserTable.status]
    )

    private companion object {
        val ACTIVE_SUBSCRIPTION_STATUSES = setOf("trial", "active")
    }
}
