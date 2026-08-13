package com.example.feature.inventory.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditInventoryResponse(
    val InventoryID: String,
    val RemoteID: Int?,
    val Title: String?,
    val Description: String?,
    val Quantity: Long?,
    val Value: Double?,
    val Type: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
