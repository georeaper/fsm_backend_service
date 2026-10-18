package com.example.feature.auth.repository

import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AuthContextRepositoryImpl : AuthContextRepository {
    override fun findActiveUser(userId: Int, companyId: Int): AuthContextUser? =
        transaction(DatabaseFactory.centralDb) {
            (UserTable innerJoin CompanyTable)
                .select {
                    (UserTable.id eq userId) and
                        (UserTable.companyId eq companyId) and
                        (CompanyTable.id eq companyId) and
                        (UserTable.status eq "active")
                }
                .limit(1)
                .map { row ->
                    AuthContextUser(
                        userId = row[UserTable.id],
                        companyId = row[UserTable.companyId],
                        username = row[UserTable.username],
                        role = row[UserTable.role],
                        databaseName = row[CompanyTable.databaseName]
                    )
                }
                .firstOrNull()
        }
}
