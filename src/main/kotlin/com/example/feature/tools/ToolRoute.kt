package com.example.feature.tools

import com.example.core.respondPaginated

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
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
    deleteToolUseCase: DeleteToolUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/tools"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getToolUseCase.execute(ctx)
            call.respondPaginated(result)
        }
        post("/tools"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateToolResponse>()
            val result=createToolUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/tools/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditToolResponse>().copy(ToolID = id)
            call.respond(HttpStatusCode.OK, updateToolUseCase.execute(ctx, request))
        }

        delete("/tools/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (deleteToolUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
