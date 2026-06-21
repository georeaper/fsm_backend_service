package com.example.feature.onboarding.dto

data class RegisterTenantDto(
    val name: String,
    val contactEmail: String,
    val adminUsername: String,
    val adminEmail: String,
    val adminPassword: String
)
