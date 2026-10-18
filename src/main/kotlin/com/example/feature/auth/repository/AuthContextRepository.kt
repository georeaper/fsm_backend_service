package com.example.feature.auth.repository

data class AuthContextUser(
    val userId: Int,
    val companyId: Int,
    val username: String,
    val role: String,
    val databaseName: String
)

interface AuthContextRepository {
    fun findActiveUser(userId: Int, companyId: Int): AuthContextUser?
}
