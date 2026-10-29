package com.example.feature.model

import com.example.core.respondPaginated

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.model.dto.CreateModelResponse
import com.example.feature.model.usecase.CreateModelUseCase
import com.example.feature.model.usecase.DeleteModelUseCase
import com.example.feature.model.usecase.GetModelUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.modelRoute(
    getModelUseCase: GetModelUseCase,
    createModelUseCase: CreateModelUseCase,
    deleteModelUseCase: DeleteModelUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/model"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getModelUseCase.execute(ctx)
            call.respondPaginated(result)
        }
        post("/model"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateModelResponse>()
            val result=createModelUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        delete("/model/{id}"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteModelUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
