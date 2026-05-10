package com.example.feature.tasks.dto

import kotlinx.serialization.Serializable

@Serializable
data class TasksResponse(
    val TaskID: String,
    val Title: String?,
    val Description: String?,
    val Status: String?,
    val Priority: String?,
    val DateStart: String?,
    val DateDue: String?,
    val DateCompleted: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val TicketID: String?,
    val UserID: String?
)
