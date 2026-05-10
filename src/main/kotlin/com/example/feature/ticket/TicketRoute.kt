package com.example.feature.ticket

import com.example.core.RequestContext
import com.example.feature.ticket.dto.CreateTicketResponse
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
    createTicketUseCase: CreateTicketUseCase 
){
    authenticate("auth-jwt") {
        get("/tickets"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getTicketUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tickets"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateTicketResponse>()
            val result=createTicketUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
