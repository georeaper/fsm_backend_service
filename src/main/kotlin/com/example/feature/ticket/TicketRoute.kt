package com.example.feature.ticket

import com.example.core.DocumentNumberException
import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.ticket.dto.CreateTicketResponse
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.usecase.UpdateTicketUseCase
import com.example.feature.ticket.usecase.DeleteTicketUseCase
import com.example.feature.ticket.usecase.CreateTicketUseCase
import com.example.feature.ticket.usecase.GetTicketUseCase
import com.example.feature.notification.model.NotificationEntityType
import com.example.feature.notification.usecase.NotificationCoordinator
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
    contextResolver: AuthenticatedRequestContextResolver,
    notifications: NotificationCoordinator
){
    authenticate("auth-jwt-v2") {
        get("/tickets"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getTicketUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tickets"){
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val request=call.receive<CreateTicketResponse>()
            try {
                val result=createTicketUseCase.execute(ctx,request)
                notifications.onCreated(ctx, authenticated.companyId, NotificationEntityType.TECHNICAL_CASE, result.TicketID)
                call.respond(HttpStatusCode.Created, result)
            } catch (error: DocumentNumberException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        }
        put("/tickets/{id}") {
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditTicketResponse>().copy(TicketID = id)
            val before = notifications.snapshot(ctx, NotificationEntityType.TECHNICAL_CASE, id)
            val result = updateTicketUseCase.execute(ctx, request)
            notifications.snapshot(ctx, NotificationEntityType.TECHNICAL_CASE, id)?.let { notifications.onUpdated(ctx, authenticated.companyId, before, it) }
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/tickets/{id}") {
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val before = notifications.snapshot(ctx, NotificationEntityType.TECHNICAL_CASE, id)
            if (deleteTicketUseCase.execute(ctx, id)) {
                before?.let { notifications.onDeleted(ctx, authenticated.companyId, it) }
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
