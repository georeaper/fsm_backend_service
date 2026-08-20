package com.example.feature.inventory.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateInventoryResponse(
    val InventoryID: String? = null,
    val RemoteID: Int? = null,
    val Title: String,
    val Description: String? = null,
    val Quantity: Long? = null,
    val Value: Double? = null,
    val Type: String? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null,
    val Version: String = "1",
    val isDeleted: Boolean? = false
)
