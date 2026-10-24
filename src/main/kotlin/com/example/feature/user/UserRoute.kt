package com.example.feature.user

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.user.dto.CreateUserResponse
import com.example.feature.user.dto.UpdateUserRequest
import com.example.feature.user.dto.UpdateMyProfileRequest
import com.example.feature.user.dto.ChangePasswordRequest
import com.example.feature.user.dto.UserApiErrorResponse
import com.example.feature.user.usecase.CreateUserUseCase
import com.example.feature.user.usecase.ChangeUserStatusUseCase
import com.example.feature.user.usecase.GetUserUseCase
import com.example.feature.user.usecase.UpdateUserUseCase
import com.example.feature.user.usecase.UpdateMyProfileUseCase
import com.example.feature.user.usecase.ChangeMyPasswordUseCase
import com.example.feature.subscription.SubscriptionInactiveException
import com.example.feature.subscription.UserLimitReachedException
import com.example.feature.subscription.dto.SubscriptionErrorResponse
import com.example.feature.subscription.dto.UserLimitErrorResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.usersRoute(
    getUserUseCase: GetUserUseCase,
    createUserUseCase: CreateUserUseCase,
    changeUserStatusUseCase: ChangeUserStatusUseCase,
    updateUserUseCase: UpdateUserUseCase,
    updateMyProfileUseCase: UpdateMyProfileUseCase,
    changeMyPasswordUseCase: ChangeMyPasswordUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){
    authenticate("auth-jwt-v2") {
        get("/users"){
            val authenticated = call.resolveAuthenticated(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            if (authenticated.role !in ADMIN_ROLES) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }
            val result=getUserUseCase.execute(authenticated)
            call.respond(result)
        }
        post("/users"){
            val authenticated = call.resolveAuthenticated(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (authenticated.role !in ADMIN_ROLES) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }
            val request = runCatching { call.receive<CreateUserResponse>() }.getOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    UserApiErrorResponse("INVALID_REQUEST", "Invalid request body")
                )
            try {
                val result=createUserUseCase.execute(authenticated,request)
                call.respond(HttpStatusCode.Created, result)
            } catch (error: UserLimitReachedException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    UserLimitErrorResponse(
                        message = "Your subscription user limit has been reached",
                        activeUsers = error.activeUsers,
                        allowedUsers = error.allowedUsers
                    )
                )
            } catch (_: SubscriptionInactiveException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    SubscriptionErrorResponse("SUBSCRIPTION_INACTIVE", "Subscription is not active")
                )
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (error.message ?: "Invalid request")))
            }
        }

        put("/users/me") {
            val authenticated = call.resolveAuthenticated(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val request = runCatching { call.receive<UpdateMyProfileRequest>() }.getOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    UserApiErrorResponse("INVALID_REQUEST", "Invalid request body")
                )
            try {
                if (!updateMyProfileUseCase.execute(authenticated, request)) {
                    call.respond(HttpStatusCode.NotFound, UserApiErrorResponse("USER_NOT_FOUND", "User not found"))
                } else {
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, UserApiErrorResponse("INVALID_REQUEST", error.message ?: "Invalid request"))
            }
        }

        put("/users/me/password") {
            val authenticated = call.resolveAuthenticated(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val request = runCatching { call.receive<ChangePasswordRequest>() }.getOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    UserApiErrorResponse("INVALID_REQUEST", "Invalid request body")
                )
            try {
                if (!changeMyPasswordUseCase.execute(authenticated, request)) {
                    call.respond(HttpStatusCode.NotFound, UserApiErrorResponse("USER_NOT_FOUND", "User not found"))
                } else {
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, UserApiErrorResponse("INVALID_REQUEST", error.message ?: "Invalid request"))
            }
        }

        put("/users/{id}") {
            val authenticated = call.resolveAuthenticated(contextResolver)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            if (authenticated.role !in ADMIN_ROLES) {
                return@put call.respond(HttpStatusCode.Forbidden)
            }
            val userId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, UserApiErrorResponse("INVALID_REQUEST", "Missing user id"))
            val request = runCatching { call.receive<UpdateUserRequest>() }.getOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    UserApiErrorResponse("INVALID_REQUEST", "Invalid request body")
                )
            try {
                val result = updateUserUseCase.execute(authenticated, userId, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, UserApiErrorResponse("USER_NOT_FOUND", "User not found"))
                call.respond(HttpStatusCode.OK, result)
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, UserApiErrorResponse("INVALID_REQUEST", error.message ?: "Invalid request"))
            }
        }

        patch("/users/{id}/activate") {
            call.changeStatus(call.parameters["id"], contextResolver, changeUserStatusUseCase::activate)
        }

        patch("/users/{id}/deactivate") {
            call.changeStatus(call.parameters["id"], contextResolver, changeUserStatusUseCase::deactivate)
        }
    }
}

private val ADMIN_ROLES = setOf("owner", "admin")

private fun io.ktor.server.application.ApplicationCall.resolveAuthenticated(
    contextResolver: AuthenticatedRequestContextResolver
): com.example.feature.auth.service.AuthenticatedRequestContext? {
    val principal = principal<JWTPrincipal>() ?: return null
    return contextResolver.resolve(principal)
}

private suspend fun io.ktor.server.application.ApplicationCall.changeStatus(
    tenantUserId: String?,
    contextResolver: AuthenticatedRequestContextResolver,
    operation: (com.example.feature.auth.service.AuthenticatedRequestContext, String) -> Boolean
) {
    val principal = principal<JWTPrincipal>()
        ?: return respond(HttpStatusCode.Unauthorized)
    val authenticated = contextResolver.resolve(principal)
        ?: return respond(HttpStatusCode.Unauthorized)
    if (authenticated.role !in ADMIN_ROLES) {
        return respond(HttpStatusCode.Forbidden)
    }
    if (tenantUserId.isNullOrBlank()) {
        return respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing user id"))
    }

    try {
        if (!operation(authenticated, tenantUserId)) {
            respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
        } else {
            respond(HttpStatusCode.OK, mapOf("success" to true))
        }
    } catch (error: UserLimitReachedException) {
        respond(
            HttpStatusCode.Conflict,
            UserLimitErrorResponse(
                message = "Your subscription user limit has been reached",
                activeUsers = error.activeUsers,
                allowedUsers = error.allowedUsers
            )
        )
    } catch (_: SubscriptionInactiveException) {
        respond(
            HttpStatusCode.Forbidden,
            SubscriptionErrorResponse("SUBSCRIPTION_INACTIVE", "Subscription is not active")
        )
    } catch (error: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to (error.message ?: "Invalid request")))
    }
}
