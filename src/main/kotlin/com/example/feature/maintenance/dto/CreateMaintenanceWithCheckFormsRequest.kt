package com.example.feature.maintenance.dto

import com.example.models.api.CheckForms
import com.example.models.api.Maintenances
import kotlinx.serialization.Serializable

@Serializable
data class CreateMaintenanceWithCheckFormsRequest(
    val Name: String?,
    val Description: String?,
    val CheckForms: List<CreateCheckFormRequest> = emptyList()
)

@Serializable
data class CreateCheckFormRequest(
    val Description: String?,
    val ValueExpected: String?,
    val ValueType: String?
)

@Serializable
data class MaintenanceWithCheckFormsResponse(
    val Maintenance: Maintenances,
    val CheckForms: List<CheckForms>
)
