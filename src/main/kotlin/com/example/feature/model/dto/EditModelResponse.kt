package com.example.feature.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditModelResponse(
    val ModelID: String,
    val RemoteID: Int?,
    val Name: String?,
    val Style: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
