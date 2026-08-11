package com.example.feature.onboarding.dto

import kotlinx.serialization.Serializable

data class RegisterTenantUserDto (

    val name :String ,
    val password :String ,
    val email :String ,

)

@Serializable
data class RegisterUserDto(
	val companyName: String,
	val companyEmail: String,
	val adminUsername: String,
	val adminEmail: String,
	val adminPassword: String,
    val populateDemo: Boolean
)