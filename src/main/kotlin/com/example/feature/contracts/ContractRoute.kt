package com.example.feature.contracts

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.contracts.dto.CreateContractResponse
import com.example.feature.contracts.dto.EditContractResponse
import com.example.feature.contracts.usecase.UpdateContractUseCase
import com.example.feature.contracts.usecase.DeleteContractUseCase
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
    createContractUseCase: CreateContractUseCase,
    updateContractUseCase: UpdateContractUseCase,
    deleteContractUseCase: DeleteContractUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/contracts"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@get call.respond(HttpStatusCode.Forbidden)

            val ctx = authenticated.toRequestContext()
            val result=getContractUseCase.execute(ctx)
            call.respond(result)
        }
        post("/contracts"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) return@post call.respond(HttpStatusCode.Forbidden)

            val ctx = authenticated.toRequestContext()
            val request=call.receive<CreateContractResponse>()
            val result=createContractUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/contracts/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (authenticated.role != OWNER_ROLE) return@put call.respond(HttpStatusCode.Forbidden)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val ctx = authenticated.toRequestContext()
            val request = call.receive<EditContractResponse>().copy(ContractID = id)
            call.respond(HttpStatusCode.OK, updateContractUseCase.execute(ctx, request))
        }

        delete("/contracts/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            if (authenticated.role != OWNER_ROLE) return@delete call.respond(HttpStatusCode.Forbidden)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val ctx = authenticated.toRequestContext()
            if (deleteContractUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private const val OWNER_ROLE = "owner"
