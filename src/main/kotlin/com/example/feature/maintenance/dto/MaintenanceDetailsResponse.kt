package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceDetailsResponse(
    val MaintenanceID: String,
    val Name: String?,
    val Description: String?,
    val CheckForms: List<CheckFormDetailsResponse>
)

@Serializable
data class CheckFormDetailsResponse(
    val CheckFormID: String,
    val Description: String?,
    val ValueExpected: String?,
    val ValueType: String?
)
