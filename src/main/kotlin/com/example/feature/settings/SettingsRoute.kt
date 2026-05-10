package com.example.feature.settings

import com.example.core.RequestContext
import com.example.feature.settings.dto.CreateSettingsResponse
import com.example.feature.settings.usecase.CreateSettingsUseCase
import com.example.feature.settings.usecase.GetSettingsUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.settingsRoute(
    getSettingsUseCase: GetSettingsUseCase,
    createSettingsUseCase: CreateSettingsUseCase
){
    authenticate("auth-jwt") {
        get("/settings"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getSettingsUseCase.execute(ctx)
            call.respond(result)
        }
        post("/settings"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateSettingsResponse>()
            val result=createSettingsUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
