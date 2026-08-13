package com.example.feature.tasks.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskResponse(
    val Title: String?,
    val Description: String?,
    val Status: String?,
    val Priority: String?,
    val DateStart: String?,
    val DateDue: String?,
    val DateCompleted: String? = null,
    val TicketID: String?,
    val UserID: String?
)
