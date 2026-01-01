package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

object JwtConfig {
    //This is the secrets of JWT
    //
    //private const val secret = "super_secret_key"
    //private const val issuer = "ktor-backend"
    //private const val audience = "ktor-users"
    val dotenv = loadDotenv()
    private val secret =dotenv["JWT_SECRET"]
    private val issuer =dotenv["WT_ISSUER"]
    private val audience=dotenv["JWT_AUDIENCE"]

    private const val expirationTime = 3600_000 // 1 hour
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    private val tokenFile = File("tokens.json")

    fun generateToken(username: String, companyDatabase: Map<String, Any>): String {
        val token = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withClaim("databaseName", companyDatabase["databaseName"].toString())
            .withClaim("databaseHost", companyDatabase["databaseHost"].toString())
            .withClaim("databasePort", companyDatabase["databasePort"].toString().toInt())
            .withClaim("databaseUser", companyDatabase["databaseUser"].toString())
            .withClaim("databasePassword", companyDatabase["databasePassword"].toString())
            .withExpiresAt(Date(System.currentTimeMillis() + expirationTime))
            .sign(algorithm)

        saveToken(username, token)
        return token
    }
    private fun saveToken(username: String, token: String) {
        val expiresAt = System.currentTimeMillis() + expirationTime
        val tokenEntry = TokenEntry(username, token, expiresAt)

        val tokenList = loadTokens().filter { it.expiresAt > System.currentTimeMillis() }.toMutableList()
        tokenList.add(tokenEntry)

        tokenFile.writeText(Json.encodeToString(tokenList))
    }
    private fun loadTokens(): List<TokenEntry> {
        return if (tokenFile.exists()) {
            Json.decodeFromString(tokenFile.readText())
        } else {
            emptyList()
        }
    }

    fun validateToken(token: String): Boolean {
        val tokenList = loadTokens()
        return tokenList.any { it.token == token && it.expiresAt > System.currentTimeMillis() }
    }

    fun cleanExpiredTokens() {
        val validTokens = loadTokens().filter { it.expiresAt > System.currentTimeMillis() }
        tokenFile.writeText(Json.encodeToString(validTokens))
    }
}


@Serializable
data class TokenEntry(val username: String, val token: String, val expiresAt: Long)