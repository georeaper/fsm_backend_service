package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostFieldReportResponse(
val FieldReportID: String? = null,
val ReportNumber: String? = null,
val Description: String? = null,
val StartDate: String? = null,
val EndDate: String? = null,
val Title: String? = null,
val Department: String? = null,
val ClientName: String? = null,
val ReportStatus: String? = null,
val Value: Double? = null,
val CustomerID: String? = null,
val ContractID: String? = null,
val UserID: String? = null,
val CaseID: String? = null,
val Version: String? = "1",
val isDeleted: Boolean? = false,
val ClientSignature: String? = null,
val FieldReportEquipments: List<FieldReportEquipment>? = emptyList(),
val FieldReportInventory: List<FieldReportInventory>? = emptyList(),
val FieldReportTools: List<FieldReportTools>? = emptyList()
)

@Serializable
data class FieldReportBulkRequest(
val fieldReport: PostFieldReportResponse? = null,
val tools: List<FieldReportTools>? = null,
val inventory: List<FieldReportInventory>? = null,
val equipment: List<FieldReportEquipment>? = null,
val FieldReportTools: List<FieldReportTools>? = null,
val FieldReportInventory: List<FieldReportInventory>? = null,
val FieldReportEquipments: List<FieldReportEquipment>? = null
) {
fun toPostFieldReportResponse(): PostFieldReportResponse {
    val base = fieldReport ?: PostFieldReportResponse()
    return base.copy(
        FieldReportTools = tools ?: FieldReportTools ?: base.FieldReportTools ?: emptyList(),
        FieldReportInventory = inventory ?: FieldReportInventory ?: base.FieldReportInventory ?: emptyList(),
        FieldReportEquipments = equipment ?: FieldReportEquipments ?: base.FieldReportEquipments ?: emptyList()
    )
}
}

@Serializable
data class FieldReportEquipment(
val FieldReportEquipmentID: String? = null,
val RemoteID: Int? = null,
val CompletedStatus: Boolean? = null,
val LastModified: String? = null,
val DateCreated: String? = null,
val Version: String? = null,
val isDeleted: Boolean? = false,
val FieldReportID: String? = null,
val EquipmentID: String? = null,
val MaintenanceID: String? = null,
val FieldReportCheckforms: List<FieldReportCheckForm>? = emptyList()
)

@Serializable
data class FieldReportCheckForm(
val FieldReportCheckFormID: String? = null,
val RemoteID: Int? = null,
val FieldReportEquipmentID: String? = null,
val Description: String? = null,
val ValueExpected: String? = null,
val ValueMeasured: String? = null,
val Result: String? = null,
val LastModified: String? = null,
val DateCreated: String? = null,
val Version: String? = null,
val isDeleted: Boolean? = false
)

@Serializable
data class FieldReportInventory(
var FieldReportInventoryID: String? = null,
var RemoteID: Int? = null,
var LastModified: String? = null,
var DateCreated: String? = null,
var Version: String? = null,
var FieldReportID: String? = null,
var InventoryID: String? = null
)

@Serializable
data class FieldReportTools(
var FieldReportToolsID: String? = null,
var RemoteID: Int? = null,
var FieldReportID: String? = null,
var ToolsID: String? = null,
var LastModified: String? = null,
var DateCreated: String? = null,
var Version: String? = null
)
