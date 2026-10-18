package com.example.feature.auth

import com.example.feature.auth.dto.AuthErrorResponse
import com.example.feature.auth.dto.LoginV2Request
import com.example.feature.auth.dto.LogoutRequest
import com.example.feature.auth.dto.LogoutResponse
import com.example.feature.auth.dto.RefreshTokenRequest
import com.example.feature.auth.usecase.AuthUseCase
import com.example.infrastructure.security.LoginAttemptLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authV2Routes(
    authUseCase: AuthUseCase,
    loginAttemptLimiter: LoginAttemptLimiter = LoginAttemptLimiter()
) {
    route("/auth/v2") {
        post("/login") {
            val request = runCatching { call.receive<LoginV2Request>() }.getOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorResponse("Invalid request")
                )

            val attemptKey = "${call.request.origin.remoteHost}:${request.username.trim().lowercase()}"
            if (!loginAttemptLimiter.isAllowed(attemptKey)) {
                return@post call.respond(
                    HttpStatusCode.TooManyRequests,
                    AuthErrorResponse("Too many login attempts. Try again later")
                )
            }

            val tokens = authUseCase.login(request.username, request.password)
            if (tokens == null) {
                loginAttemptLimiter.recordFailure(attemptKey)
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse("Invalid credentials")
                )
            }

            loginAttemptLimiter.reset(attemptKey)
            call.respond(HttpStatusCode.OK, tokens)
        }

        post("/refresh") {
            val request = runCatching { call.receive<RefreshTokenRequest>() }.getOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorResponse("Invalid request")
                )

            val tokens = authUseCase.refresh(request.refreshToken)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse("Invalid refresh token")
                )

            call.respond(HttpStatusCode.OK, tokens)
        }

        post("/logout") {
            val request = runCatching { call.receive<LogoutRequest>() }.getOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorResponse("Invalid request")
                )

            authUseCase.logout(request.refreshToken)
            call.respond(HttpStatusCode.OK, LogoutResponse(success = true))
        }
    }
}
