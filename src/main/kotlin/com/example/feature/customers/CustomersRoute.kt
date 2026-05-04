package com.example.feature.customers

import com.example.feature.customers.dto.CustomersDto
import com.example.core.RequestContext
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.loadDotenv
import com.example.models.databaseModels.customerTable
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction





fun Route.customersRoute(getCustomersUseCase: GetCustomersUseCase,
                         createCustomerUseCase: CreateCustomerUseCase ) {

    authenticate("auth-jwt") {

        get("/customers"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result = getCustomersUseCase.execute(ctx)
            call.respond(result)

        }
        post("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )

            val request = call.receive<CreateCustomerRequest>()

            val result = createCustomerUseCase.execute(ctx, request)

            call.respond(HttpStatusCode.Created, result)
        }
    }
}


