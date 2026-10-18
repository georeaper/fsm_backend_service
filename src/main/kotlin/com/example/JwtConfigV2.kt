package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64


import java.util.Date
import java.util.UUID

object JwtConfigV2
{

    private const val ACCESS_TOKEN_MINUTES = 15L

    private fun requiredEnv(name: String): String =
        System.getenv(name)
            ?.takeIf { it.isNotBlank() }
            ?: loadDotenv()[name]?.takeIf { it.isNotBlank() }
            ?: error("Missing environment variable: $name")

    private val secret: String by lazy {
        requiredEnv("JWT_SECRET").also {
            require(it.toByteArray(Charsets.UTF_8).size >= 32) {
                "JWT_SECRET must contain at least 32 bytes"
            }
        }
    }

    private val issuer: String by lazy {
        requiredEnv("JWT_ISSUER")
    }

    private val audience: String by lazy {
        requiredEnv("JWT_AUDIENCE")
    }

    private val algorithm: Algorithm by lazy {
        Algorithm.HMAC256(secret)
    }

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    fun generateAccessToken(
        userId: Int,
        companyId: Int,
        role: String
    ): String {
        require(userId > 0)
        require(companyId > 0)
        require(role.isNotBlank())

        val now = Instant.now()

        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("tenant_id", companyId)
            .withClaim("role", role)
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(ACCESS_TOKEN_MINUTES * 60)))
            .sign(algorithm)
    }

    fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    fun verifyToken(token: String): Boolean =
        try {
            verifier.verify(token)
            true
        } catch (_: com.auth0.jwt.exceptions.JWTVerificationException) {
            false
        }
}
