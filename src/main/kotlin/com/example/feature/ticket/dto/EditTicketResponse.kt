package com.example.feature.ticket.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditTicketResponse(
    val TicketID: String = "",
    val Title: String? = null,
    val TicketNumber: String? = null,
    val Description: String? = null,
    val Notes: String? = null,
    val Urgency: String? = null,
    val Status: String? = null,
    val Type: String? = null,
    val Active: Boolean? = null,
    val DateStart: String? = null,
    val DateEnd: String? = null,
    val UserID: String? = null,
    val CustomerID: String? = null,
    val EquipmentID: String? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null,
    val Version: String? = null
)
