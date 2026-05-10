package com.example.feature.tools.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateToolResponse(
    val Title: String?,
    val Description: String?,
    val Model: String?,
    val Manufacturer: String?,
    val SerialNumber: String?,
    val CalibrationDate: String?
)
