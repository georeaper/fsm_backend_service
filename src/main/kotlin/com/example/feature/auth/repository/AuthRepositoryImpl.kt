package com.example.feature.auth.repository

import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.AuthSessionTable
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

class AuthRepositoryImpl : AuthRepository {
    override fun findActiveUser(username: String): AuthenticatedUser? =
        transaction(DatabaseFactory.centralDb) {
            (UserTable innerJoin CompanyTable)
                .select {
                    (UserTable.username eq username) and
                        (UserTable.status eq "active")
                }
                .limit(1)
                .map(::toAuthenticatedUser)
                .firstOrNull()
        }

    override fun createSession(userId: Int, refreshTokenHash: String) {
        transaction(DatabaseFactory.centralDb) {
            val now = nowUtc()
            AuthSessionTable.insert {
                it[AuthSessionTable.userId] = userId
                it[AuthSessionTable.refreshTokenHash] = refreshTokenHash
                it[createdAt] = now
                it[expiresAt] = refreshExpiryUtc()
            }
        }
    }

    override fun rotateSession(
        currentTokenHash: String,
        newTokenHash: String
    ): AuthenticatedUser? = transaction(DatabaseFactory.centralDb) {
        val now = nowUtc()
        val row = (AuthSessionTable innerJoin UserTable innerJoin CompanyTable)
            .select {
                AuthSessionTable.refreshTokenHash eq currentTokenHash
            }
            .forUpdate()
            .limit(1)
            .firstOrNull() ?: return@transaction null

        if (row[AuthSessionTable.revokedAt] != null) {
            AuthSessionTable.update({
                (AuthSessionTable.userId eq row[UserTable.id]) and
                    AuthSessionTable.revokedAt.isNull()
            }) {
                it[revokedAt] = now
            }
            return@transaction null
        }

        if (row[AuthSessionTable.expiresAt] <= now || row[UserTable.status] != "active") {
            AuthSessionTable.update({ AuthSessionTable.id eq row[AuthSessionTable.id] }) {
                it[revokedAt] = now
                it[lastUsedAt] = now
            }
            return@transaction null
        }

        AuthSessionTable.update({ AuthSessionTable.id eq row[AuthSessionTable.id] }) {
            it[revokedAt] = now
            it[lastUsedAt] = now
        }

        AuthSessionTable.insert {
            it[userId] = row[UserTable.id]
            it[refreshTokenHash] = newTokenHash
            it[createdAt] = now
            it[expiresAt] = refreshExpiryUtc()
        }

        toAuthenticatedUser(row)
    }

    override fun revokeSession(refreshTokenHash: String): Boolean =
        transaction(DatabaseFactory.centralDb) {
            val now = nowUtc()
            AuthSessionTable.update({
                (AuthSessionTable.refreshTokenHash eq refreshTokenHash) and
                    AuthSessionTable.revokedAt.isNull()
            }) {
                it[revokedAt] = now
                it[lastUsedAt] = now
            } > 0
        }

    private fun toAuthenticatedUser(row: org.jetbrains.exposed.sql.ResultRow) =
        AuthenticatedUser(
            id = row[UserTable.id],
            username = row[UserTable.username],
            companyId = row[UserTable.companyId],
            role = row[UserTable.role],
            passwordHash = row[UserTable.password]
        )

    private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    private fun refreshExpiryUtc() =
        (Clock.System.now() + 30.days).toLocalDateTime(TimeZone.UTC)
}
