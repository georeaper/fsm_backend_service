package com.example.feature.customers.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomersDto(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val zipCode: String?,
    val notes: String?,
    val description: String?,
    val dateCreated: String?,
    val lastModified: String?,
    val status: String?
)