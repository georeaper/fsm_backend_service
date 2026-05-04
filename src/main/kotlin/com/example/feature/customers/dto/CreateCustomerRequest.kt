package com.example.feature.customers.dto

data class CreateCustomerRequest(
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val zipCode: String?,
    val notes: String?,
    val description: String?
)
