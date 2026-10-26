package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportUpdateDto(
    val ReportNumber: String? = null,
    val Title: String? = null,
    val Description: String? = null,
    val StartDate: String? = null,
    val EndDate: String? = null,
    val ScheduledStart: String? = null,
    val ScheduledEnd: String? = null,
    val ReportStatus: String? = null,
    val Status: String? = null,
    val Priority: String? = null,
    val Type: String? = null,
    val CustomerID: String? = null,
    val ContractID: String? = null,
    val CaseID: String? = null,
    val TicketID: String? = null,
    val UserID: String? = null,
    val ClientName: String? = null,
    val ClientSignature: String? = null,
    val Department: String? = null,
    val Value: Double? = null
)
