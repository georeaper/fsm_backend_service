package com.example.feature.equipments

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.equipments.dto.CreateEquipmentResponse
import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.usecase.DeleteEquipmentUseCase
import com.example.feature.equipments.usecase.GetEquipmentUseCase
import com.example.feature.equipments.usecase.UpdateEquipmentUseCase
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
    createEquipmentUseCase: CreateEquipmentUseCase,
    updateEquipmentUseCase: UpdateEquipmentUseCase,
    deleteEquipmentUseCase: DeleteEquipmentUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/equipments"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@get call.respond(HttpStatusCode.Forbidden)

            val ctx = authenticated.toRequestContext()
            val result=getEquipmentUseCase.execute(ctx)
            call.respond(result)
        }
        post("/equipments"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@post call.respond(HttpStatusCode.Forbidden)

            val ctx = authenticated.toRequestContext()
            val request=call.receive<CreateEquipmentResponse>()
            val result=createEquipmentUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }

        put("/equipments/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@put call.respond(HttpStatusCode.Forbidden)

            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val ctx = authenticated.toRequestContext()
            val request = call.receive<CreateEquipmentResponse>()
            val result = updateEquipmentUseCase.execute(ctx, id, request)

            if (result == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, result)
            }
        }

        delete("/equipments/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@delete call.respond(HttpStatusCode.Forbidden)

            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val ctx = authenticated.toRequestContext()
            val deleted = deleteEquipmentUseCase.execute(ctx, id)

            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private const val OWNER_ROLE = "owner"
