package com.example.feature.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateModelResponse(
    val ModelID: String? = null,
    val RemoteID: Int? = null,
    val Name: String,
    val Style: String? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null,
    val Version: String = "1"
)
