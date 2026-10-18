package com.example.feature.ticket

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.ticket.dto.CreateTicketResponse
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.usecase.UpdateTicketUseCase
import com.example.feature.ticket.usecase.DeleteTicketUseCase
import com.example.feature.ticket.usecase.CreateTicketUseCase
import com.example.feature.ticket.usecase.GetTicketUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.ticketsRoute(
    getTicketUseCase: GetTicketUseCase,
    createTicketUseCase: CreateTicketUseCase,
    updateTicketUseCase: UpdateTicketUseCase,
    deleteTicketUseCase: DeleteTicketUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/tickets"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getTicketUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tickets"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateTicketResponse>()
            val result=createTicketUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/tickets/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditTicketResponse>().copy(TicketID = id)
            call.respond(HttpStatusCode.OK, updateTicketUseCase.execute(ctx, request))
        }

        delete("/tickets/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (deleteTicketUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
