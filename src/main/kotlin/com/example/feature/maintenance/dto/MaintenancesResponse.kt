package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenancesResponse(
    val MaintenanceID: String,
    val RemoteID: Int?,
    val Name: String?,
    val Description: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
