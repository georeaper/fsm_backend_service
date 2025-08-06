package com.example.routes

import com.example.models.databaseModels.customerTable
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction





fun Route.customersRoute() {
    authenticate("auth-jwt") {
        get("/get-customers") {
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
//            // 2. Extract your custom claim from the token (e.g. companyDatabase)

            if (dbName.isNullOrEmpty()) {
                call.application.environment.log.warn("Unauthorized: Missing companyDatabase claim")
                return@get call.respond(HttpStatusCode.Unauthorized, "Invalid token data")
            }

//            // 3. Get DB connection dynamically using your secret or logic

            val database = Database.connect(
                url = "jdbc:postgresql://localhost:5432/$dbName",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "Giorgos13"
            )
            // 4. Query the database using Exposed inside a transaction
            val customers = transaction(database) {


                customerTable.selectAll().map {
                    mapOf(
                        "ID" to it[customerTable.customerId],
                        "Name" to it[customerTable.name],
                        "Email" to it[customerTable.email],
                        "Phone" to it[customerTable.phone],
                        "Address" to it[customerTable.address],
                        "City" to it[customerTable.city],
                        "Zip Code" to it[customerTable.zipCode],
                        "Notes" to it[customerTable.notes],
                        "Description" to it[customerTable.description],
                        "Date Created" to it[customerTable.dateCreated],
                        "Last Modified" to it[customerTable.lastModified],
                        "Status" to it[customerTable.customerStatus]
                        // add other fields as needed
                    )
                }
            }

            // 5. Respond with the data
            call.respond(customers)
        }
    }
}

