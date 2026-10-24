package com.example.feature.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginV2Request(
    val username: String,
    val password: String
)

@Serializable
data class AccessTokenResponse(
    val accessToken: String,
    val username: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 900
)

data class IssuedTokenPair(
    val response: AccessTokenResponse,
    val refreshToken: String
)

@Serializable
data class AuthErrorResponse(val error: String)

@Serializable
data class LogoutResponse(val success: Boolean)
