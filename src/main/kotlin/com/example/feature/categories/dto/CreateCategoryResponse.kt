package com.example.feature.categories.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryResponse(
    val Name: String?,
    val Style: String?
)
