package com.example.feature.subscription

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.subscription.dto.SubscriptionErrorResponse
import com.example.feature.subscription.repository.SubscriptionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.subscriptionRoutes(
    repository: SubscriptionRepository,
    contextResolver: AuthenticatedRequestContextResolver
) {
    authenticate("auth-jwt-v2") {
        get("/subscription") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val usage = repository.usage(authenticated.companyId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    SubscriptionErrorResponse("SUBSCRIPTION_NOT_FOUND", "Subscription not found")
                )
            call.respond(usage)
        }

        get("/subscription/usage") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val authenticated = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val usage = repository.usage(authenticated.companyId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    SubscriptionErrorResponse("SUBSCRIPTION_NOT_FOUND", "Subscription not found")
                )
            call.respond(usage)
        }
    }
}
