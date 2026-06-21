package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserDto(
	val companyName: String,
	val companyEmail: String,
	val adminUsername: String,
	val adminEmail: String,
	val adminPassword: String
)


