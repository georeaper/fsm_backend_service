package com.example.feature.categories

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.categories.dto.CreateCategoryResponse
import com.example.feature.categories.usecase.CreateCategoryUseCase
import com.example.feature.categories.usecase.GetCategoryUseCase
import com.example.feature.categories.usecase.DeleteCategoryUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.categoriesRoute(
    getCategoryUseCase: GetCategoryUseCase,
    createCategoryUseCase: CreateCategoryUseCase,
    deleteCategoryUseCase: DeleteCategoryUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/settings/equipment-categories"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }

            val ctx = authenticated.toRequestContext()
            val result=getCategoryUseCase.execute(ctx)
            call.respond(result)
        }
        post("/settings/equipment-categories"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }

            val ctx = authenticated.toRequestContext()
            val request=call.receive<CreateCategoryResponse>()
            val result=createCategoryUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        delete("/settings/equipment-categories/{id}"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@delete call.respond(HttpStatusCode.Forbidden)
            }

            val ctx = authenticated.toRequestContext()
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteCategoryUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}

private const val OWNER_ROLE = "owner"
