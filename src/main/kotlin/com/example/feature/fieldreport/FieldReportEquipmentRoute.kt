package com.example.feature.fieldreport

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.fieldreport.dto.FieldReportEquipment
import com.example.feature.fieldreport.usecase.CreateFieldReportEquipmentUseCase
import com.example.feature.fieldreport.usecase.DeleteFieldReportEquipmentUseCase
import com.example.feature.fieldreport.usecase.UpdateFieldReportEquipmentUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.fieldReportEquipmentRoute(
    createUseCase: CreateFieldReportEquipmentUseCase,
    updateUseCase: UpdateFieldReportEquipmentUseCase,
    deleteUseCase: DeleteFieldReportEquipmentUseCase,
    contextResolver: AuthenticatedRequestContextResolver
) {
    authenticate("auth-jwt-v2") {
        post("/fieldreports/{fieldReportId}/equipments") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val fieldReportId = call.parameters["fieldReportId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            val result = createUseCase.execute(ctx, fieldReportId, call.receive<FieldReportEquipment>())

            if (result == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.Created, result)
        }

        put("/fieldreports/{fieldReportId}/equipments/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val fieldReportId = call.parameters["fieldReportId"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val result = updateUseCase.execute(ctx, fieldReportId, id, call.receive<FieldReportEquipment>())

            if (result == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, result)
        }

        delete("/fieldreports/{fieldReportId}/equipments/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val fieldReportId = call.parameters["fieldReportId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            if (deleteUseCase.execute(ctx, fieldReportId, id)) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}
