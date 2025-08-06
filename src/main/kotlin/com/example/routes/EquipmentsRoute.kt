package com.example.routes

import com.example.models.databaseModels.equipmentTable
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedConnection
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager




fun Route.equipmentsRoute() {
    authenticate("auth-jwt") {
        get("/equipments") {
            // 1. Get JWT principal provided by Ktor's JWT auth
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.getClaim("username", String::class)
            val dbName = principal?.getClaim("databaseName", String::class)


            println("Username from token: $username")
            println("Database name from token: $dbName")

            // Use the claims as needed

            if (principal == null) {
                call.application.environment.log.warn("Unauthorized: Missing JWT Principal")
                return@get call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }

            // 2. Extract your custom claim from the token (e.g. companyDatabase)

            if (dbName.isNullOrEmpty()) {
                call.application.environment.log.warn("Unauthorized: Missing companyDatabase claim")
                return@get call.respond(HttpStatusCode.Unauthorized, "Invalid token data")
            }

            // 3. Get DB connection dynamically using your secret or logic

            val database = Database.connect(
                url = "jdbc:postgresql://localhost:5432/$dbName",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "Giorgos13"
            )
            // 4. Query the database using Exposed inside a transaction
            val equipments = transaction(database) {


                equipmentTable.selectAll().map {
                    mapOf(
                        "ID" to it[equipmentTable.equipmentId],
                        "Name" to it[equipmentTable.name],
                        "Serial number" to it[equipmentTable.serialNumber],
                        "Manufacturer" to it[equipmentTable.manufacturer],
                        "Model" to it[equipmentTable.model],
                        "Description" to it[equipmentTable.description],
                        "Category" to it[equipmentTable.equipmentCategory],
                        "Equipment version" to it[equipmentTable.equipmentVersion],
                        "Installation date" to it[equipmentTable.installationDate],
                        "Status" to it[equipmentTable.equipmentStatus],
                        "Warranty" to it[equipmentTable.warranty],
                        "Notes" to it[equipmentTable.notes],
                        "Name" to it[equipmentTable.name],
                        "Date created" to it[equipmentTable.dateCreated],
                        "Last modified" to it[equipmentTable.lastModified],
                        "Customer name" to it[equipmentTable.customerId],

                        // add other fields as needed
                    )
                }
            }

            // 5. Respond with the data
            call.respond(equipments)
        }
    }
}

