package com.example.feature.inventory

import com.example.core.RequestContext
import com.example.feature.inventory.dto.CreateInventoryResponse
import com.example.feature.inventory.usecase.CreateInventoryUseCase
import com.example.feature.inventory.usecase.GetInventoryUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.inventoryRoute(
    getInventoryUseCase: GetInventoryUseCase,
    createInventoryUseCase: CreateInventoryUseCase
){
    authenticate("auth-jwt") {
        get("/inventory"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getInventoryUseCase.execute(ctx)
            call.respond(result)
        }
        post("/inventory"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateInventoryResponse>()
            val result=createInventoryUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
