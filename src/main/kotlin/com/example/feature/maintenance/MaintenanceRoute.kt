package com.example.feature.maintenance

import com.example.core.RequestContext
import com.example.feature.maintenance.dto.CreateMaintenanceResponse
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
    createMaintenanceUseCase: CreateMaintenanceUseCase
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
        post("/maintenances"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateMaintenanceResponse>()
            val result=createMaintenanceUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
