package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportTechnicalCaseDto(
    val TechnicalCaseID: String,
    val TicketNumber: String? = null,
    val Title: String? = null,
    val CustomerID: String? = null,
    val CustomerName: String? = null,
    val Status: String? = null,
    val SystemCategory: String? = null,
    val ResolutionStatus: String? = null,
    val ResolutionNotes: String? = null
)

@Serializable
data class ReplaceFieldReportTechnicalCasesRequest(
    val TechnicalCaseIDs: List<String>
)

@Serializable
data class CompleteFieldReportRequest(
    val WorkOrderStatus: String,
    val TechnicalCaseUpdates: List<TechnicalCaseCompletionUpdate> = emptyList()
)

@Serializable
data class TechnicalCaseCompletionUpdate(
    val TechnicalCaseID: String,
    val Status: String,
    val ResolutionNotes: String? = null
)

@Serializable
data class CompleteFieldReportResponse(
    val FieldReportID: String,
    val WorkOrderStatus: String,
    val UpdatedTechnicalCases: List<FieldReportTechnicalCaseDto>
)

@Serializable
data class TechnicalCaseConfirmationRequiredResponse(
    val code: String = "CONFIRM_TECHNICAL_CASE_STATUS_CHANGE",
    val message: String = "Select the new status for each linked technical case before completing the work order.",
    val TechnicalCases: List<FieldReportTechnicalCaseDto>
)
