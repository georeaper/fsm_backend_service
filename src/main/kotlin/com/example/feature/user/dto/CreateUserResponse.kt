package com.example.feature.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponse(
    val name: String? = null,
    val description: String? = null,
    val email: String,
    val phone: String? = null,
    val reportPrefix: String? = null,
    val technicalCasePrefix: String? = null,
    val username: String,
    val password: String,
    val role: String = "user"
)
