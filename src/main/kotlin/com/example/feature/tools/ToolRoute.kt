package com.example.feature.tools

import com.example.core.RequestContext
import com.example.feature.tools.dto.CreateToolResponse
import com.example.feature.tools.dto.EditToolResponse
import com.example.feature.tools.usecase.UpdateToolUseCase
import com.example.feature.tools.usecase.DeleteToolUseCase
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
    createToolUseCase: CreateToolUseCase,
    updateToolUseCase: UpdateToolUseCase,
    deleteToolUseCase: DeleteToolUseCase
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
        put("/tools/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            val request = call.receive<EditToolResponse>().copy(ToolID = id)
            call.respond(HttpStatusCode.OK, updateToolUseCase.execute(ctx, request))
        }

        delete("/tools/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            if (deleteToolUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
