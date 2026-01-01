package com.example


import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.routes.authRoutes
import com.example.routes.dbCreation
import com.example.routes.equipmentsRoute
import com.example.routes.protectedRoutes
import com.example.routes.seedCompanyARoute
import com.example.routes.syncRoutes
import io.github.cdimascio.dotenv.dotenv
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
import java.io.File


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
    val dotenv = loadDotenv()

    val logger = LoggerFactory.getLogger("Application")

    val url ="jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DB_NAME"]}"
    val driver = "org.postgresql.Driver"
    val user ="${dotenv["DB_USER"]}"
    val password ="${dotenv["DB_PASSWORD"]}"
    println("======CONFIG DB INIT========")
    println(url)
    println(driver)
    println(user)
    println(password)
    println("======CONFIG DB END========")
    Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password
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


fun loadDotenv(): io.github.cdimascio.dotenv.Dotenv {
    val env = System.getenv("ENV") ?: "dev"

    val fileName = when (env) {
        "dev" -> "env.dev"
        "prod" -> "env.prod"
        else -> error("Invalid ENV value: $env (use dev or prod)")
    }

    val file = File("assets/$fileName")
    require(file.exists()) {
        "Missing environment file: assets/$fileName"
    }

    println("Running in ENV=$env")

    return dotenv {
        directory = "./assets"
        filename = fileName
    }


}
