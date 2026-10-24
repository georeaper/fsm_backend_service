package com.example.feature.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val userId: String,
    val authUserId: Int?,
    val username: String?,
    val name: String?,
    val email: String?,
    val phone: String?,
    val description: String?,
    val reportPrefix: String?,
    val technicalCasePrefix: String?,
    val role: String?,
    val status: String?
)

@Serializable
data class UpdateUserRequest(
    val username: String,
    val email: String,
    val password: String? = null,
    val role: String,
    val name: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val reportPrefix: String? = null,
    val technicalCasePrefix: String? = null
)

@Serializable
data class UpdateMyProfileRequest(
    val name: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val reportPrefix: String? = null,
    val technicalCasePrefix: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UserApiErrorResponse(val code: String, val message: String)
