package com.example.feature.categories

import com.example.core.RequestContext
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
    deleteCategoryUseCase: DeleteCategoryUseCase
){
    authenticate("auth-jwt") {
        get("/settings/equipment-categories"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getCategoryUseCase.execute(ctx)
            call.respond(result)
        }
        post("/settings/equipment-categories"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateCategoryResponse>()
            val result=createCategoryUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        delete("/settings/equipment-categories/{id}"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteCategoryUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
