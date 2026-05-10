package com.example.feature.tools.dto

import kotlinx.serialization.Serializable

@Serializable
data class ToolsResponse(
    val ToolID: String,
    val RemoteID: Int?,
    val Title: String?,
    val Description: String?,
    val Model: String?,
    val Manufacturer: String?,
    val SerialNumber: String?,
    val CalibrationDate: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
