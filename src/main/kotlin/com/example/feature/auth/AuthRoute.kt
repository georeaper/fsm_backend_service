package com.example.feature.auth

import com.example.feature.auth.dto.AuthErrorResponse
import com.example.feature.auth.dto.LoginV2Request
import com.example.feature.auth.dto.LogoutResponse
import com.example.feature.auth.dto.MobileLogoutRequest
import com.example.feature.auth.dto.MobileRefreshRequest
import com.example.feature.auth.dto.MobileTokenResponse
import com.example.feature.auth.usecase.AuthUseCase
import com.example.infrastructure.security.LoginAttemptLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.http.CookieEncoding
import io.ktor.server.request.receive
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.util.date.GMTDate

fun Route.authV2Routes(
    authUseCase: AuthUseCase,
    loginAttemptLimiter: LoginAttemptLimiter = LoginAttemptLimiter(),
    cookieConfig: AuthCookieConfig = AuthCookieConfig.fromEnvironment()
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
            call.response.cookies.appendRefreshToken(tokens.refreshToken, cookieConfig)
            call.respond(HttpStatusCode.OK, tokens.response)
        }

        post("/refresh") {
            val refreshToken = call.request.cookies[REFRESH_COOKIE_NAME]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse("Missing refresh token")
                )

            val tokens = authUseCase.refresh(refreshToken)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse("Invalid refresh token")
                )

            call.response.cookies.appendRefreshToken(tokens.refreshToken, cookieConfig)
            call.respond(HttpStatusCode.OK, tokens.response)
        }

        post("/logout") {
            call.request.cookies[REFRESH_COOKIE_NAME]?.let(authUseCase::logout)
            call.response.cookies.expireRefreshToken(cookieConfig)
            call.respond(HttpStatusCode.OK, LogoutResponse(success = true))
        }

        route("/mobile") {
            post("/login") {
                val request = runCatching { call.receive<LoginV2Request>() }.getOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, AuthErrorResponse("Invalid request"))
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
                    return@post call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse("Invalid credentials"))
                }
                loginAttemptLimiter.reset(attemptKey)
                call.respond(tokens.toMobileResponse())
            }

            post("/refresh") {
                val request = runCatching { call.receive<MobileRefreshRequest>() }.getOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, AuthErrorResponse("Invalid request"))
                val tokens = authUseCase.refresh(request.refreshToken)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, AuthErrorResponse("Invalid refresh token"))
                call.respond(tokens.toMobileResponse())
            }

            post("/logout") {
                val request = runCatching { call.receive<MobileLogoutRequest>() }.getOrNull()
                request?.refreshToken?.let(authUseCase::logout)
                call.respond(HttpStatusCode.OK, LogoutResponse(success = true))
            }
        }
    }
}

private fun com.example.feature.auth.dto.IssuedTokenPair.toMobileResponse() = MobileTokenResponse(
    accessToken = response.accessToken,
    refreshToken = refreshToken,
    username = response.username,
    tokenType = response.tokenType,
    expiresIn = response.expiresIn
)

private const val REFRESH_COOKIE_NAME = "fsm_refresh_token"
private const val REFRESH_COOKIE_PATH = "/auth/v2"
private const val REFRESH_COOKIE_MAX_AGE_SECONDS = 30L * 24 * 60 * 60

private fun io.ktor.server.response.ResponseCookies.appendRefreshToken(
    token: String,
    config: AuthCookieConfig
) {
    append(
        name = REFRESH_COOKIE_NAME,
        value = token,
        encoding = CookieEncoding.URI_ENCODING,
        maxAge = REFRESH_COOKIE_MAX_AGE_SECONDS,
        path = REFRESH_COOKIE_PATH,
        secure = config.secure,
        httpOnly = true,
        extensions = mapOf("SameSite" to config.sameSite)
    )
}

private fun io.ktor.server.response.ResponseCookies.expireRefreshToken(
    config: AuthCookieConfig
) {
    append(
        name = REFRESH_COOKIE_NAME,
        value = "",
        encoding = CookieEncoding.URI_ENCODING,
        maxAge = 0,
        expires = GMTDate(0),
        path = REFRESH_COOKIE_PATH,
        secure = config.secure,
        httpOnly = true,
        extensions = mapOf("SameSite" to config.sameSite)
    )
}
