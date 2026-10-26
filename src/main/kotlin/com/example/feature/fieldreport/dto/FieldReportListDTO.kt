package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportListDto(
    val FieldReportID: String,
    val ReportNumber: String? = null,
    val Title: String? = null,
    val Description: String? = null,
    val StartDate: String? = null,
    val EndDate: String? = null,
    val ScheduledStart: String? = null,
    val ScheduledEnd: String? = null,
    val ReportStatus: String? = null,
    val Status: String? = ReportStatus,
    val Priority: String? = null,
    val Type: String? = null,

    val CustomerID: String? = null,
    val CustomerName: String? = null,

    val ContractID: String? = null,
    val ContractName: String? = null,

    val CaseID: String? = null,
    val TicketNumber: String? = null,

    val UserID: String? = null,
    val UserName: String? = null,

    val Department: String? = null,
    val Value: Double? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null
)
