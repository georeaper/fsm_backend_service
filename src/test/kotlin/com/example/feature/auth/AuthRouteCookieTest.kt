package com.example.feature.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.feature.auth.repository.AuthRepository
import com.example.feature.auth.repository.AuthenticatedUser
import com.example.feature.auth.usecase.AuthUseCase
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRouteCookieTest {
    @Test
    fun `login refresh and logout keep refresh token in HttpOnly cookie`() = testApplication {
        val repository = FakeAuthRepository()
        application {
            install(ContentNegotiation) { json() }
            routing {
                authV2Routes(
                    authUseCase = AuthUseCase(
                        repository = repository,
                        accessTokenGenerator = { _, _, _ -> "access-token" }
                    ),
                    cookieConfig = AuthCookieConfig(secure = false)
                )
            }
        }

        val login = client.post("/auth/v2/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"username":"admin","password":"password"}""")
        }

        assertEquals(HttpStatusCode.OK, login.status)
        val loginBody = login.bodyAsText()
        assertTrue(loginBody.contains("\"accessToken\""))
        assertFalse(loginBody.contains("refreshToken"))

        val loginCookie = login.headers[HttpHeaders.SetCookie]
        assertNotNull(loginCookie)
        assertTrue(loginCookie.contains("fsm_refresh_token="))
        assertTrue(loginCookie.contains("HttpOnly", ignoreCase = true))
        assertTrue(loginCookie.contains("SameSite=Strict", ignoreCase = true))
        assertTrue(loginCookie.contains("Path=/auth/v2", ignoreCase = true))
        assertFalse(loginCookie.contains("Secure", ignoreCase = true))

        val cookieHeader = loginCookie.substringBefore(';')
        val refresh = client.post("/auth/v2/refresh") {
            header(HttpHeaders.Cookie, cookieHeader)
        }

        assertEquals(HttpStatusCode.OK, refresh.status)
        assertFalse(refresh.bodyAsText().contains("refreshToken"))
        val rotatedCookie = refresh.headers[HttpHeaders.SetCookie]
        assertNotNull(rotatedCookie)
        assertTrue(rotatedCookie.contains("HttpOnly", ignoreCase = true))

        val logout = client.post("/auth/v2/logout") {
            header(HttpHeaders.Cookie, rotatedCookie.substringBefore(';'))
        }

        assertEquals(HttpStatusCode.OK, logout.status)
        assertTrue(repository.revoked)
        assertTrue(logout.headers[HttpHeaders.SetCookie]?.contains("Max-Age=0") == true)
    }

    private class FakeAuthRepository : AuthRepository {
        private val user = AuthenticatedUser(
            id = 7,
            username = "admin",
            companyId = 11,
            role = "owner",
            passwordHash = BCrypt.withDefaults().hashToString(4, "password".toCharArray())
        )

        private var currentHash: String? = null
        var revoked: Boolean = false
            private set

        override fun findActiveUser(username: String): AuthenticatedUser? =
            user.takeIf { username == it.username }

        override fun createSession(userId: Int, refreshTokenHash: String) {
            currentHash = refreshTokenHash
        }

        override fun rotateSession(currentTokenHash: String, newTokenHash: String): AuthenticatedUser? {
            if (currentTokenHash != currentHash) return null
            currentHash = newTokenHash
            return user
        }

        override fun revokeSession(refreshTokenHash: String): Boolean {
            revoked = refreshTokenHash == currentHash
            return revoked
        }
    }
}
