package com.example


import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.routes.authRoutes
import com.example.routes.dbCreation
import com.example.routes.equipmentsRoute
import com.example.routes.protectedRoutes
import com.example.routes.seedCompanyARoute
import com.example.routes.syncRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*

import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory


fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)  // <-- Add this line!
        allowHeader("X-Custom-Header")
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)  // <-- Also add OPTIONS to handle preflight
        allowCredentials = true
    }

    install(Authentication) { // ✅ Install Authentication
        jwt("auth-jwt") {
            realm = "ktor app"
            verifier(JwtConfig.verifier) // Ensure you have a proper JWT verifier
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty())
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

    val logger = LoggerFactory.getLogger("Application")
    val dbConfig = environment.config.config("ktor.database.centralDatabase")
    logger.info(dbConfig.toString())

    Database.connect(
        url = dbConfig.property("url").getString(),
        driver = dbConfig.property("driver").getString(),
        user = dbConfig.property("user").getString(),
        password = dbConfig.property("password").getString()
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(CompanyTable, UserTable)
    }

    logger.info("Database initialized successfully!")

    configureRouting()

    routing {
        equipmentsRoute()
        seedCompanyARoute()
        authRoutes()
        dbCreation()
        authenticate("auth-jwt") {  // ✅ Wrap protectedRoutes inside authenticate
            protectedRoutes()
        }
        syncRoutes()

    }
}