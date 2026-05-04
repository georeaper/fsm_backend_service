package com.example.feature.customers

import com.example.feature.customers.dto.CustomersDto
import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.loadDotenv
import com.example.models.databaseModels.customerTable
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction





fun Route.customersRoute(getCustomersUseCase: GetCustomersUseCase) {

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
            val dotenv = loadDotenv()

            val url ="jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/$dbName"
            val driver = "org.postgresql.Driver"
            val userDb ="${dotenv["DB_USER"]}"
            val password ="${dotenv["DB_PASSWORD"]}"
//            // 3. Get DB connection dynamically using your secret or logic

            val database = Database.connect(
                url = url,
                driver = driver,
                user = userDb,
                password = password
            )
            // 4. Query the database using Exposed inside a transaction
            val customers = transaction(database) {


                customerTable.selectAll().map {
                    CustomersDto(
                        id = it[customerTable.customerId],
                        name = it[customerTable.name],
                        email = it[customerTable.email],
                        phone = it[customerTable.phone],
                        address = it[customerTable.address],
                        city = it[customerTable.city],
                        zipCode = it[customerTable.zipCode],
                        notes = it[customerTable.notes],
                        description = it[customerTable.description],
                        dateCreated = it[customerTable.dateCreated]?.toString(),
                        lastModified = it[customerTable.lastModified]?.toString(),
                        status = it[customerTable.customerStatus]?.toString()
                    )
                }
            }

            // 5. Respond with the data
            call.respond(customers)
        }

        get("/customers"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )

            val result = getCustomersUseCase.execute(ctx)

            call.respond(result)
        }
    }
}


