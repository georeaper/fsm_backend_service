package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditMaintenanceResponse(
    val MaintenanceID: String,
    val Name: String?,
    val Description: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
