package com.example.feature.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(
    val UserID: String,
    val RemoteID: Int?,
    val Name: String?,
    val Description: String?,
    val Email: String?,
    val Phone: String?,
    val ReportPrefix: String?,
    val TechnicalCasePrefix: String?,
    val LastReportNumber: Int?,
    val LastTCNumber: Int?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
