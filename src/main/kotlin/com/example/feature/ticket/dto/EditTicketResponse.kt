package com.example.feature.ticket.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditTicketResponse(
    val TicketID: String,
    val Title: String?,
    val TicketNumber: String?,
    val Description: String?,
    val Notes: String?,
    val Urgency: String?,
    val Active: Boolean?,
    val DateStart: String?,
    val DateEnd: String?,
    val UserID: String?,
    val CustomerID: String?,
    val EquipmentID: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
