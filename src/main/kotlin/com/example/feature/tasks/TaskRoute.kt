package com.example.feature.tasks

import com.example.core.respondPaginated

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.tasks.dto.CreateTaskResponse
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.usecase.UpdateTaskUseCase
import com.example.feature.tasks.usecase.DeleteTaskUseCase
import com.example.feature.tasks.usecase.CreateTaskUseCase
import com.example.feature.tasks.usecase.GetTaskUseCase
import com.example.feature.notification.model.NotificationEntityType
import com.example.feature.notification.usecase.NotificationCoordinator
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
    contextResolver: AuthenticatedRequestContextResolver,
    notifications: NotificationCoordinator
){
    authenticate("auth-jwt-v2") {
        get("/tasks"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getTaskUseCase.execute(ctx)
            call.respondPaginated(result)
        }
        post("/tasks"){
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val request=call.receive<CreateTaskResponse>() 
            val result=createTaskUseCase.execute(ctx,request)
            notifications.onCreated(ctx, authenticated.companyId, NotificationEntityType.TASK, result.TaskID)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/tasks/{id}") {
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<EditTaskResponse>().copy(TaskID = id)
            val before = notifications.snapshot(ctx, NotificationEntityType.TASK, id)
            val result = updateTaskUseCase.execute(ctx, request)
            notifications.snapshot(ctx, NotificationEntityType.TASK, id)?.let { notifications.onUpdated(ctx, authenticated.companyId, before, it) }
            call.respond(HttpStatusCode.OK, result)
        }

        delete("/tasks/{id}") {
            val authenticated = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val before = notifications.snapshot(ctx, NotificationEntityType.TASK, id)
            if (deleteTaskUseCase.execute(ctx, id)) {
                before?.let { notifications.onDeleted(ctx, authenticated.companyId, it) }
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
