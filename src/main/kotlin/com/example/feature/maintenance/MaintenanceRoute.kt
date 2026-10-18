package com.example.feature.maintenance

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.maintenance.dto.CreateMaintenanceWithCheckFormsRequest
import com.example.feature.maintenance.dto.UpdateMaintenanceWithCheckFormsRequest
import com.example.feature.maintenance.usecase.UpdateMaintenanceUseCase
import com.example.feature.maintenance.usecase.DeleteMaintenanceUseCase
import com.example.feature.maintenance.usecase.CreateMaintenanceUseCase
import com.example.feature.maintenance.usecase.GetMaintenanceUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.maintenanceRoute(
    getMaintenanceUseCase: GetMaintenanceUseCase ,
    createMaintenanceUseCase: CreateMaintenanceUseCase,
    updateMaintenanceUseCase: UpdateMaintenanceUseCase,
    deleteMaintenanceUseCase: DeleteMaintenanceUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/maintenances"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getMaintenanceUseCase.execute(ctx)
            call.respond(result)
        }
        get("/maintenances/{maintenanceId}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val maintenanceId = call.parameters["maintenanceId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val result = getMaintenanceUseCase.executeById(ctx, maintenanceId)

                ?: return@get call.respond(HttpStatusCode.NotFound)
            println("result: $result")
            call.respond(HttpStatusCode.OK, result)
        }
        post("/maintenances"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<CreateMaintenanceWithCheckFormsRequest>()
            val result=createMaintenanceUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/maintenance/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<UpdateMaintenanceWithCheckFormsRequest>()
                .copy(MaintenanceID = id)
            val result = updateMaintenanceUseCase.execute(ctx, request)
                ?: return@put call.respond(HttpStatusCode.NotFound)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/maintenance/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (deleteMaintenanceUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
