package com.example.feature.manufacturer

import com.example.core.RequestContext
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
    deleteManufacturerUseCase: DeleteManufacturerUseCase
){
    authenticate("auth-jwt") {
        get("/manufacturer"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getManufacturerUseCase.execute(ctx)
            call.respond(result)
        }
        post("/manufacturer"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateManufacturerResponse>()
            val result=createManufacturerUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        delete("/manufacturer/{id}"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteManufacturerUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
