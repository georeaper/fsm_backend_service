package com.example.feature.maintenance

import com.example.core.RequestContext
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
    deleteMaintenanceUseCase: DeleteMaintenanceUseCase
){
    authenticate("auth-jwt") {
        get("/maintenances"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getMaintenanceUseCase.execute(ctx)
            call.respond(result)
        }
        get("/maintenances/{maintenanceId}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val maintenanceId = call.parameters["maintenanceId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result = getMaintenanceUseCase.executeById(ctx, maintenanceId)

                ?: return@get call.respond(HttpStatusCode.NotFound)
            println("result: $result")
            call.respond(HttpStatusCode.OK, result)
        }
        post("/maintenances"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request = call.receive<CreateMaintenanceWithCheckFormsRequest>()
            val result=createMaintenanceUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/maintenance/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            val request = call.receive<UpdateMaintenanceWithCheckFormsRequest>()
                .copy(MaintenanceID = id)
            val result = updateMaintenanceUseCase.execute(ctx, request)
                ?: return@put call.respond(HttpStatusCode.NotFound)
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/maintenance/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            if (deleteMaintenanceUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
