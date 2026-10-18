package com.example.feature.user

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.user.dto.CreateUserResponse
import com.example.feature.user.usecase.CreateUserUseCase
import com.example.feature.user.usecase.GetUserUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.usersRoute(
    getUserUseCase: GetUserUseCase,
    createUserUseCase: CreateUserUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/users"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getUserUseCase.execute(ctx)
            call.respond(result)
        }
        post("/users"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateUserResponse>()
            val result=createUserUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
