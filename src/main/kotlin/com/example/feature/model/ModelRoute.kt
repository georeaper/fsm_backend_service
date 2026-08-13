package com.example.feature.model

import com.example.core.RequestContext
import com.example.feature.model.dto.CreateModelResponse
import com.example.feature.model.usecase.CreateModelUseCase
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
    createModelUseCase: CreateModelUseCase
){
    authenticate("auth-jwt") {
        get("/model"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getModelUseCase.execute(ctx)
            call.respond(result)
        }
        post("/model"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateModelResponse>()
            val result=createModelUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
