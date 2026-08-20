package com.example.feature.categories.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val CategoryID: String,
    val RemoteID: Int?,
    val Name: String?,
    val Style: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
