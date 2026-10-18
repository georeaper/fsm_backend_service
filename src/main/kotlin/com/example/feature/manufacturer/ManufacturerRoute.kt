package com.example.feature.manufacturer

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.manufacturer.dto.CreateManufacturerResponse
import com.example.feature.manufacturer.usecase.CreateManufacturerUseCase
import com.example.feature.manufacturer.usecase.DeleteManufacturerUseCase
import com.example.feature.manufacturer.usecase.GetManufacturerUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.manufacturerRoute(
    getManufacturerUseCase: GetManufacturerUseCase,
    createManufacturerUseCase: CreateManufacturerUseCase,
    deleteManufacturerUseCase: DeleteManufacturerUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/manufacturer"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getManufacturerUseCase.execute(ctx)
            call.respond(result)
        }
        post("/manufacturer"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateManufacturerResponse>()
            val result=createManufacturerUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        delete("/manufacturer/{id}"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteManufacturerUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
