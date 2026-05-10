package com.example.feature.user

import com.example.core.RequestContext
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
    createUserUseCase: CreateUserUseCase
){
    authenticate("auth-jwt") {
        get("/users"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getUserUseCase.execute(ctx)
            call.respond(result)
        }
        post("/users"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateUserResponse>()
            val result=createUserUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
