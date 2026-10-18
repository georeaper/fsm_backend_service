package com.example.feature.inventory

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
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
    deleteInventoryUseCase: DeleteInventoryUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/inventory"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getInventoryUseCase.execute(ctx)
            call.respond(result)
        }
        post("/inventory"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateInventoryResponse>()
            val result=createInventoryUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/inventory/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditInventoryResponse>().copy(InventoryID = id)
            call.respond(HttpStatusCode.OK, updateInventoryUseCase.execute(ctx, request))
        }

        delete("/inventory/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (deleteInventoryUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
