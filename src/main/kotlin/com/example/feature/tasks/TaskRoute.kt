package com.example.feature.tasks

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.tasks.dto.CreateTaskResponse
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.usecase.UpdateTaskUseCase
import com.example.feature.tasks.usecase.DeleteTaskUseCase
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
    createTaskUseCase: CreateTaskUseCase,
    updateTaskUseCase: UpdateTaskUseCase,
    deleteTaskUseCase: DeleteTaskUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/tasks"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getTaskUseCase.execute(ctx)
            call.respond(result)
        }
        post("/tasks"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateTaskResponse>() 
            val result=createTaskUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/tasks/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditTaskResponse>().copy(TaskID = id)
            call.respond(HttpStatusCode.OK, updateTaskUseCase.execute(ctx, request))
        }

        delete("/tasks/{id}") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (deleteTaskUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
