package com.example.feature.equipments

import com.example.core.RequestContext
import com.example.feature.equipments.dto.CreateEquipmentResponse
import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.usecase.GetEquipmentUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.equipmentsRoute(
    getEquipmentUseCase: GetEquipmentUseCase,
    createEquipmentUseCase: CreateEquipmentUseCase
){
    authenticate("auth-jwt") {
        get("/equipments"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getEquipmentUseCase.execute(ctx)
            call.respond(result)
        }
        post("/equipments"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateEquipmentResponse>()
            val result=createEquipmentUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}