package com.example.feature.fieldreport

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.*
import com.example.feature.fieldreport.repository.FieldReportApiRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.fieldReportRoute(repository: FieldReportApiRepository) {
    authenticate("auth-jwt") {
        get("/fieldreports") {
            val ctx = call.fieldReportContext() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(repository.findAll(ctx))
        }
        get("/fieldreports/{id}") {
            val ctx = call.fieldReportContext() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            repository.findById(ctx, id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        post("/fieldreports") {
            val ctx = call.fieldReportContext() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            call.respond(HttpStatusCode.Created, repository.create(ctx, call.receive<FieldReportCreateDto>()))
        }
        put("/fieldreports/{id}") {
            val ctx = call.fieldReportContext() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.update(ctx, id, call.receive<FieldReportUpdateDto>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/equipments") {
            val ctx = call.fieldReportContext() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncEquipments(ctx, id, call.receive<FieldReportEquipmentSyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/inventories") {
            val ctx = call.fieldReportContext() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncInventories(ctx, id, call.receive<FieldReportInventorySyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        put("/fieldreports/{id}/special-tools") {
            val ctx = call.fieldReportContext() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            repository.syncTools(ctx, id, call.receive<FieldReportToolsSyncRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
        delete("/fieldreports/{id}") {
            val ctx = call.fieldReportContext() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (repository.delete(ctx, id)) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.fieldReportContext(): RequestContext? {
    val principal = principal<JWTPrincipal>() ?: return null
    val dbName = principal.getClaim("databaseName", String::class) ?: return null
    return RequestContext(dbName, principal.getClaim("username", String::class))
}
