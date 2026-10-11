package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportEquipmentSyncRequest(
    val Equipments: List<FieldReportEquipmentWriteDto>
)

@Serializable
data class FieldReportEquipmentWriteDto(
    val FieldReportEquipmentID: String? = null,
    val EquipmentID: String,
    val MaintenanceID: String? = null,
    val CompletedStatus: Boolean? = null,
    val FieldReportCheckforms: List<FieldReportCheckformWriteDto> = emptyList()
)

@Serializable
data class FieldReportCheckformWriteDto(
    val FieldReportCheckFormID: String? = null,
    val Description: String? = null,
    val ValueExpected: String? = null,
    val ValueMeasured: String? = null,
    val Result: String? = null
)