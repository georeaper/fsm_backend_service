package com.example.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.JwtConfig
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.services.api.LoginRequest
import com.example.services.api.LoginResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction



fun Route.authRoutes() {
    route("/auth") {
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                println("Received Login Request: $request")  // Log the incoming request

                // Connect to central_db (stores user credentials and database info)
                Database.connect(
                    url = "jdbc:postgresql://localhost:5432/central_db",
                    driver = "org.postgresql.Driver",
                    user = "postgres",
                    password = "Giorgos13"
                )

                val user = transaction {
                    (UserTable innerJoin CompanyTable)
                        .select { UserTable.username eq request.username }
                        .map {
                            Triple(
                                it[UserTable.username],
                                it[UserTable.password],
                                mapOf(
                                    "databaseName" to it[CompanyTable.databaseName],
                                    "databaseHost" to it[CompanyTable.databaseHost],
                                    "databasePort" to it[CompanyTable.databasePort],
                                    "databaseUser" to it[CompanyTable.databaseUser],
                                    "databasePassword" to it[CompanyTable.databasePassword]
                                )
                            )
                        }
                        .firstOrNull()
                }

                if (user != null) {
                    println("User found in DB: $user")  // Log the user retrieved from DB
                    println("Password matching : $request")
                    val passwordVerified = BCrypt.verifyer().verify(request.password.toCharArray(), user.second.toCharArray()).verified
                    println("Password verification result: $passwordVerified")  // Log password verification result

                    if (passwordVerified) {
                        val username = user.first
                        val companyDatabase = user.third

                        // Generate JWT token with company database details
                        val token = JwtConfig.generateToken(username, companyDatabase)
                        println("Generated Token: $token")  // Log the generated token

                        // Send token & username to frontend
                        call.respond(HttpStatusCode.OK, LoginResponse(token, username))
                    } else {
                        println("Invalid credentials: Incorrect password")  // Log invalid password
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    }
                } else {
                    println("User not found for username: ${request.username}")  // Log if user is not found
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")  // Log error message
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
            }
        }
    }
}
