package com.example.feature.equipments

import com.example.core.respondPaginated

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.equipments.dto.CreateEquipmentResponse
import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.usecase.DeleteEquipmentUseCase
import com.example.feature.equipments.usecase.GetEquipmentUseCase
import com.example.feature.equipments.usecase.UpdateEquipmentUseCase
import com.example.feature.equipments.usecase.EquipmentKnowledgeBaseQuery
import com.example.feature.equipments.dto.EquipmentKnowledgeBaseError
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
        get("/equipment-knowledge-base/equipment") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val status = call.request.queryParameters["status"]?.uppercase()
            val warrantyStatus = call.request.queryParameters["warrantyStatus"]?.uppercase()
            val openTechnicalCases = call.request.queryParameters["openTechnicalCases"]?.uppercase()
            if (status != null && status !in setOf("ACTIVE", "INACTIVE")) {
                return@get call.respond(HttpStatusCode.BadRequest, EquipmentKnowledgeBaseError("INVALID_STATUS", "status must be ACTIVE or INACTIVE"))
            }
            if (warrantyStatus != null && warrantyStatus !in setOf("ACTIVE", "EXPIRED", "UNKNOWN")) {
                return@get call.respond(HttpStatusCode.BadRequest, EquipmentKnowledgeBaseError("INVALID_WARRANTY_STATUS", "warrantyStatus must be ACTIVE, EXPIRED, or UNKNOWN"))
            }
            if (openTechnicalCases != null && openTechnicalCases !in setOf("ANY", "WITH_OPEN", "WITHOUT_OPEN")) {
                return@get call.respond(HttpStatusCode.BadRequest, EquipmentKnowledgeBaseError("INVALID_OPEN_TC_FILTER", "openTechnicalCases must be ANY, WITH_OPEN, or WITHOUT_OPEN"))
            }
            val customerIds = call.request.queryParameters.getAll("customerId").orEmpty()
                .flatMap { it.split(',') }.map(String::trim).filter(String::isNotEmpty).toSet()
            val result = getEquipmentUseCase.knowledgeBase(
                authenticated.toRequestContext(),
                EquipmentKnowledgeBaseQuery(
                    search = call.request.queryParameters["search"],
                    manufacturer = call.request.queryParameters["manufacturer"],
                    customerIds = customerIds,
                    status = status,
                    warrantyStatus = warrantyStatus,
                    openTechnicalCases = openTechnicalCases
                )
            )
            call.respondPaginated(result)
        }
        get("/equipment-knowledge-base/filters") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(getEquipmentUseCase.knowledgeBaseFilters(authenticated.toRequestContext()))
        }
        get("/equipment-knowledge-base/equipment/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.takeIf(String::isNotBlank)
                ?: return@get call.respond(HttpStatusCode.BadRequest, EquipmentKnowledgeBaseError("INVALID_EQUIPMENT_ID", "equipment id is required"))
            val result = getEquipmentUseCase.knowledgeBaseDetail(authenticated.toRequestContext(), id)
                ?: return@get call.respond(HttpStatusCode.NotFound, EquipmentKnowledgeBaseError("EQUIPMENT_NOT_FOUND", "Equipment was not found"))
            call.respond(result)
        }
        get("/equipments"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@get call.respond(HttpStatusCode.Forbidden)

            val ctx = authenticated.toRequestContext()
            val result=getEquipmentUseCase.execute(ctx)
            call.respondPaginated(result)
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
