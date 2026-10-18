package com.example.feature.auth.repository

data class AuthenticatedUser(
    val id: Int,
    val username: String,
    val companyId: Int,
    val role: String,
    val passwordHash: String
)

interface AuthRepository {
    fun findActiveUser(username: String): AuthenticatedUser?
    fun createSession(userId: Int, refreshTokenHash: String)
    fun rotateSession(currentTokenHash: String, newTokenHash: String): AuthenticatedUser?
    fun revokeSession(refreshTokenHash: String): Boolean
}
