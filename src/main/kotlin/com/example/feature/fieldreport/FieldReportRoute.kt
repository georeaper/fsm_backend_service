package com.example.feature.fieldreport

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.core.DocumentNumberException
import com.example.feature.fieldreport.dto.*
import com.example.feature.fieldreport.repository.FieldReportApiRepository
import com.example.feature.notification.model.NotificationEntityType
import com.example.feature.notification.usecase.NotificationCoordinator
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.fieldReportRoute(
    repository: FieldReportApiRepository,
    contextResolver: AuthenticatedRequestContextResolver,
    notifications: NotificationCoordinator
) {
    authenticate("auth-jwt-v2") {
        get("/fieldreports") {
            val ctx = call.authenticatedRequestContext(contextResolver) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(repository.findAll(ctx))
        }
        get("/fieldreports/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            repository.findById(ctx, id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        post("/fieldreports") {
            val authenticated = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.let(contextResolver::resolve) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            try {
                val result = repository.create(authenticated.toRequestContext(), call.receive<FieldReportCreateDto>())
                notifications.onCreated(authenticated.toRequestContext(), authenticated.companyId, NotificationEntityType.WORK_ORDER, result.FieldReportID)
                call.respond(HttpStatusCode.Created, result)
            } catch (error: DocumentNumberException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        }
        put("/fieldreports/{id}") {
            val authenticated = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.let(contextResolver::resolve) ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val before = notifications.snapshot(ctx, NotificationEntityType.WORK_ORDER, id)
            repository.update(ctx, id, call.receive<FieldReportUpdateDto>())?.let {
                notifications.snapshot(ctx, NotificationEntityType.WORK_ORDER, id)?.let { after -> notifications.onUpdated(ctx, authenticated.companyId, before, after) }
                call.respond(it)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/equipments") {
            val ctx = call.authenticatedRequestContext(contextResolver) ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncEquipments(ctx, id, call.receive<FieldReportEquipmentSyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/inventories") {
            val ctx = call.authenticatedRequestContext(contextResolver) ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncInventories(ctx, id, call.receive<FieldReportInventorySyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/special-tools") {
            val ctx = call.authenticatedRequestContext(contextResolver) ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncTools(ctx, id, call.receive<FieldReportToolsSyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        delete("/fieldreports/{id}") {
            val authenticated = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.let(contextResolver::resolve) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val before = notifications.snapshot(ctx, NotificationEntityType.WORK_ORDER, id)
            if (repository.delete(ctx, id)) {
                before?.let { notifications.onDeleted(ctx, authenticated.companyId, it) }
                call.respond(HttpStatusCode.NoContent)
            } else call.respond(HttpStatusCode.NotFound)
        }
    }
}
