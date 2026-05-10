package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditFieldReportResponse(
    val FieldReportID: String,
    val ReportNumber: String?,
    val Description: String?,
    val StartDate: String?,
    val EndDate: String?,
    val Title: String?,
    val Department: String?,
    val ClientName: String?,
    val ReportStatus: String?,
    val Value: Double?,
    val CustomerID: String?,
    val ContractID: String?,
    val UserID: String?,
    val CaseID: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
