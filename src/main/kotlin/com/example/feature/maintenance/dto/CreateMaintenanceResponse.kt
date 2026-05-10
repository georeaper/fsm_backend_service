package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateMaintenanceResponse(
    val Name: String?,
    val Description: String?
)
