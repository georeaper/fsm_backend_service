package com.example.feature.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditUserResponse(
    val UserID: String,
    val Name: String?,
    val Description: String?,
    val Email: String?,
    val Phone: String?,
    val ReportPrefix: String?,
    val TechnicalCasePrefix: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
