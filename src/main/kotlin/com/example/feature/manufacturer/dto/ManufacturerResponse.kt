package com.example.feature.manufacturer.dto

import kotlinx.serialization.Serializable

@Serializable
data class ManufacturerResponse(
    val ManufacturerID: String,
    val RemoteID: Int?,
    val Name: String?,
    val Style: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
