package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class FieldReportInventorySyncRequest(
    val Inventories: List<FieldReportInventoryWriteDto>
)

@Serializable
data class FieldReportInventoryWriteDto(
    val FieldReportInventoryID: String? = null,
    val InventoryID: String
)

@Serializable
data class FieldReportToolsSyncRequest(
    val Tools: List<FieldReportToolsWriteDto>
)

@Serializable
data class FieldReportToolsWriteDto(
    val FieldReportToolsID: String? = null,
    val ToolsID: String
)