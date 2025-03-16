package com.example.routes

import com.example.JwtConfig
import com.example.models.authenticationModels.UserTable
import com.example.services.DatabaseConnectionManager

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Route.protectedRoutes() {
    authenticate("auth-jwt") {
        get("/user/data") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username")?.asString()
            val databaseName = principal?.payload?.getClaim("databaseName")?.asString()
            val databaseHost = principal?.payload?.getClaim("databaseHost")?.asString()
            val databasePort = principal?.payload?.getClaim("databasePort")?.asInt()
            val databaseUser = principal?.payload?.getClaim("databaseUser")?.asString()
            val databasePassword = principal?.payload?.getClaim("databasePassword")?.asString()

            if (username != null && databaseName != null && databaseHost != null && databaseUser != null && databasePassword != null) {
                try {
                    Database.connect(
                        url = "jdbc:postgresql://$databaseHost:$databasePort/$databaseName",
                        driver = "org.postgresql.Driver",
                        user = databaseUser,
                        password = databasePassword
                    )

                    val userData = transaction {
                        UserTable.select { UserTable.username eq username }
                            .map {
                                mapOf(
                                    "username" to it[UserTable.username],
                                    "email" to it[UserTable.email],
                                    "role" to it[UserTable.role]
                                )
                            }
                            .firstOrNull()
                    }

                    if (userData != null) {
                        call.respond(HttpStatusCode.OK, userData)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User data not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error: ${e.localizedMessage}"))
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
            }
        }
    }
}


private val logger = LoggerFactory.getLogger("SyncRoutes")

fun Route.syncRoutes() {
    route("/sync") {

        /**
         * Endpoint: POST /sync/fetchEntities
         * Description: Fetches entity data based on request parameters.
         * Requires: Bearer Token Authentication
         */
        post("/fetchEntities") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (token.isNullOrEmpty() || !isValidToken(token)) {
                logger.warn("Unauthorized access attempt with token: $token")
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }else{
                try {
                    logger.warn("Token matched: $token")
                    val request = call.receive<FetchRequest>()
                    val secretKey="super_secret_key"

                    // Log the received request and token
                    logger.info("Received Sync Request: Entity = ${request.entityName}, Timestamp = ${request.timestamp}")
                    logger.info("Token used: $token")
                    val dbManager = DatabaseConnectionManager(secretKey)
                    val connection = dbManager.getConnectionFromJwt(token)

                    if (connection != null) {
                        println("Database connected successfully!")
                        val data = fetchEntityDataFromDB()

                        call.respond(HttpStatusCode.OK, data)
                        connection.close()
                    } else {
                        println("Failed to connect to the database.")
                    }
                    // Fetch data from DB

                } catch (e: Exception) {
                    logger.error("Error processing sync request", e)
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                }
            }


        }
    }
}

/**
 * Simulated function to validate token.
 * Replace this with actual authentication logic.
 */
fun isValidToken(token: String): Boolean {
    val isValid = JwtConfig.validateToken(token)
    println("Token is valid: $isValid")
    // Example: Token should be "valid_token" (Replace this with DB verification)
    return isValid
}

/**
 * Simulated function to fetch data from the database.
 */
fun fetchEntityDataFromDB(): List<String> {
    val list =ArrayList<String>()
    for (i in 1..5){
        list.add("list$i")

    }
    return list as List<String>
}

/**
 * Data class for sync requests
 */
@Serializable
data class FetchRequest(
    val entityName: String,
    val timestamp: String
)