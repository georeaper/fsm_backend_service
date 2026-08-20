package com.example.feature.inventory

import com.example.core.RequestContext
import com.example.feature.inventory.dto.CreateInventoryResponse
import com.example.feature.inventory.dto.EditInventoryResponse
import com.example.feature.inventory.usecase.UpdateInventoryUseCase
import com.example.feature.inventory.usecase.DeleteInventoryUseCase
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
    createInventoryUseCase: CreateInventoryUseCase,
    updateInventoryUseCase: UpdateInventoryUseCase,
    deleteInventoryUseCase: DeleteInventoryUseCase
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
        put("/inventory/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            val request = call.receive<EditInventoryResponse>().copy(InventoryID = id)
            call.respond(HttpStatusCode.OK, updateInventoryUseCase.execute(ctx, request))
        }

        delete("/inventory/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            if (deleteInventoryUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
