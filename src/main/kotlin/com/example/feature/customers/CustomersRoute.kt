package com.example.feature.customers


import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.customers.usecase.CreateCustomerUseCase
import com.example.feature.customers.usecase.DeleteCustomerUseCase
import com.example.feature.customers.usecase.GetCustomersUseCase
import com.example.feature.customers.usecase.UpdateCustomerUseCase
import com.example.feature.auth.service.AuthenticatedRequestContextResolver

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive






fun Route.customersRoute(getCustomersUseCase: GetCustomersUseCase,
                         createCustomerUseCase: CreateCustomerUseCase,
                         updateCustomerUseCase: UpdateCustomerUseCase,
                         deleteCustomerUseCase: DeleteCustomerUseCase,
                         contextResolver: AuthenticatedRequestContextResolver
) {

    authenticate("auth-jwt-v2") {

        get("/customers"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }

            val ctx = authenticated.toRequestContext()
            val result = getCustomersUseCase.execute(ctx)
            call.respond(result)

        }
        post("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }

            val ctx = authenticated.toRequestContext()

            val request = call.receive<CreateCustomerRequest>()

            val result = createCustomerUseCase.execute(ctx, request)

            call.respond(HttpStatusCode.Created, result)
        }

        put("/customers/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@put call.respond(HttpStatusCode.Forbidden)
            }

            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val ctx = authenticated.toRequestContext()
            val request = call.receive<CreateCustomerRequest>()
            val result = updateCustomerUseCase.execute(ctx, id, request)

            if (result == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, result)
            }
        }

        delete("/customers/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val authenticated = contextResolver.resolve(principal)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            if (authenticated.role != OWNER_ROLE) {
                return@delete call.respond(HttpStatusCode.Forbidden)
            }

            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val ctx = authenticated.toRequestContext()
            val deleted = deleteCustomerUseCase.execute(ctx, id)

            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private const val OWNER_ROLE = "owner"


