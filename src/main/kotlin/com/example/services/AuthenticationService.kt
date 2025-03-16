package com.example.services

import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class UserCredentials(val username: String, val password: String)
data class CompanyDatabaseInfo(val databaseName: String, val host: String, val port: Int, val user: String, val password: String)

object AuthenticationService {
    fun authenticateUser(credentials: UserCredentials): CompanyDatabaseInfo? {
        return transaction {
            val user = UserTable
                .select { UserTable.username eq credentials.username }
                .singleOrNull() ?: return@transaction null

            if (!verifyPassword(credentials.password, user[UserTable.password])) {
                return@transaction null
            }

            val company = CompanyTable
                .select { CompanyTable.id eq user[UserTable.companyId] }
                .singleOrNull() ?: return@transaction null

            CompanyDatabaseInfo(
                databaseName = company[CompanyTable.databaseName],
                host = company[CompanyTable.databaseHost],
                port = company[CompanyTable.databasePort],
                user = company[CompanyTable.databaseUser],
                password = company[CompanyTable.databasePassword]
            )
        }
    }

    private fun verifyPassword(inputPassword: String, hashedPassword: String): Boolean {
        // Replace this with a proper hashing algorithm (e.g., bcrypt)
        return inputPassword == hashedPassword
    }
}