package com.example.feature.contracts

import com.example.core.RequestContext
import com.example.feature.contracts.dto.CreateContractResponse
import com.example.feature.contracts.usecase.CreateContractUseCase
import com.example.feature.contracts.usecase.GetContractUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.contractsRoute(
    getContractUseCase: GetContractUseCase,
    createContractUseCase: CreateContractUseCase
){
    authenticate("auth-jwt") {
        get("/contracts"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getContractUseCase.execute(ctx)
            call.respond(result)
        }
        post("/contracts"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateContractResponse>()
            val result=createContractUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
