package com.example.feature.tasks

import com.example.core.RequestContext
import com.example.feature.tasks.dto.CreateTaskResponse
import com.example.feature.tasks.usecase.CreateTaskUseCase
import com.example.feature.tasks.usecase.GetTaskUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.tasksRoute(
    getTaskUseCase: GetTaskUseCase,
    createTaskUseCase: CreateTaskUseCase
){
    authenticate("auth-jwt") {
        get("/tasks"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getTaskUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tasks"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateTaskResponse>() 
            val result=createTaskUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
