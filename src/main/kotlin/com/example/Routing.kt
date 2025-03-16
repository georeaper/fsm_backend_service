package com.example

import com.example.services.AuthenticationService
import com.example.services.UserCredentials
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        post("/loginOld") {
            val credentials = call.receive<UserCredentials>()
            val companyInfo = AuthenticationService.authenticateUser(credentials)

            if (companyInfo != null) {
                call.respond(mapOf("status" to "success", "database" to companyInfo))
            } else {
                call.respond(mapOf("status" to "failure", "message" to "Invalid credentials"))
            }
        }
    }


}