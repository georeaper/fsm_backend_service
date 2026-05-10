package com.example.feature.tools

import com.example.core.RequestContext
import com.example.feature.tools.dto.CreateToolResponse
import com.example.feature.tools.usecase.CreateToolUseCase
import com.example.feature.tools.usecase.GetToolUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.toolsRoute(
    getToolUseCase: GetToolUseCase,
    createToolUseCase: CreateToolUseCase 
){
    authenticate("auth-jwt") {
        get("/tools"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getToolUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tools"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateToolResponse>()
            val result=createToolUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
