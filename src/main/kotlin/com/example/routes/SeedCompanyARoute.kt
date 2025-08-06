package com.example.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.utils.createDatabaseIfNotExists

import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insertAndGetId


import java.time.LocalDateTime

fun Route.seedCompanyARoute() {
    route("/seed/companyA") {
        get {
            val createdCompanyId = transaction {
                // Check if company already exists
                val exists = CompanyTable.select { CompanyTable.name eq "CompanyA" }.singleOrNull()
                if (exists != null) {
                    return@transaction exists[CompanyTable.id]
                }

                // Insert CompanyA
                CompanyTable.insert {
                    it[name] = "CompanyA"
                    it[contactEmail] = "info@companya32.com"
                    it[databaseName] = "CompanyA"
                    it[databaseHost] = "localhost"
                    it[databasePort] = 5432
                    it[databaseUser] = "postgres"
                    it[databasePassword] = "Giorgos13"

                }[CompanyTable.id]
            }

            // Seed users
            transaction {
                val hashedPassword = BCrypt.withDefaults().hashToString(10, "Giorgos13".toCharArray())
                UserTable.insert {
                it[UserTable.username] = "george_mk9"
                it[UserTable.email] = "george_mk1@companya68.com"
                it[UserTable.password] = hashedPassword
                it[UserTable.companyId] = createdCompanyId
                it[UserTable.role] = "admin"
                it[UserTable.status] = "active"

                }
            }
            createDatabaseIfNotExists("CompanyA")


        }
    }
}
