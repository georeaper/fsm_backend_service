package com.example.feature.customers.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    val id: String?,
    val name :String? ,
    val phone :String? ,
    val email :String ?,
    val address :String? ,
    val zipCode :String ?,
    val city :String? ,
    val notes :String ?,
    val description :String? ,
    val status :Boolean? ,
    val lastModified :String? ,
    val dateCreated :String?
)
