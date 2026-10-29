package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportTechnicianDto(
    val UserID: String,
    val UserName: String? = null,
    val Role: String
)

@Serializable
data class ReplaceFieldReportTechniciansRequest(
    val Technicians: List<FieldReportTechnicianWriteDto>
)

@Serializable
data class FieldReportTechnicianWriteDto(
    val UserID: String,
    val Role: String = "ASSISTANT"
)

@Serializable
data class SchedulingWorkOrderDto(
    val FieldReportID: String,
    val ReportNumber: String? = null,
    val Title: String? = null,
    val ScheduledStart: String,
    val ScheduledEnd: String,
    val Status: String? = null,
    val Type: String? = null,
    val TypeColor: String? = null,
    val CustomerID: String? = null,
    val CustomerName: String? = null,
    val Technicians: List<FieldReportTechnicianDto> = emptyList()
)
