package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMaintenanceWithCheckFormsRequest(
    val MaintenanceID: String = "",
    val Name: String?,
    val Description: String?,
    val Version: String? = null,
    val CheckForms: List<UpdateCheckFormRequest> = emptyList()
)

@Serializable
data class UpdateCheckFormRequest(
    val CheckFormID: String? = null,
    val Description: String?,
    val ValueExpected: String?,
    val ValueType: String?,
    val Version: String? = null
)
