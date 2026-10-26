package com.example.feature.fieldreport.dto

import kotlinx.serialization.Serializable


@Serializable
data class FieldReportDetailDto(
    val FieldReportID: String,
    val RemoteID: Int? = null,

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

    val UserID: String? = null,
    val UserName: String? = null,

    val TicketID: String? = null,
    val TicketNumber: String? = null,

    val ClientName: String? = null,
    val ClientSignature: String? = null,

    val Department: String? = null,
    val Value: Double? = null,

    val LastModified: String? = null,
    val DateCreated: String? = null,
    val Version: String? = null,

    val Equipments: List<FieldReportEquipmentDetailDto> = emptyList(),

    val Inventories: List<FieldReportInventoryDetailDto> = emptyList(),

    val SpecialTools: List<FieldReportToolsDetailDto> = emptyList()
)
@Serializable
data class FieldReportEquipmentDetailDto(
    val FieldReportEquipmentID: String,
    val EquipmentID: String?,

    val EquipmentName: String? = null,
    val EquipmentModel: String? = null,
    val Manufacturer: String? = null,
    val SerialNumber: String? = null,
    val MaintenanceID: String?,

    val MaintenanceName: String? = null,
    val CompletedStatus: Boolean?,

    val FieldReportCheckforms:
    List<FieldReportCheckformDetailDto> = emptyList()
)

@Serializable
data class FieldReportCheckformDetailDto(
    val FieldReportCheckFormID: String,
    val Description: String?,
    val ValueExpected: String?,
    val ValueMeasured: String?,
    val Result: String?
)
@Serializable
data class FieldReportInventoryDetailDto(
    val FieldReportInventoryID: String,
    val InventoryID: String?,
    val InventoryName: String? = null,
    val Quantity: Long? = null
)
@Serializable
data class FieldReportToolsDetailDto(
    val FieldReportToolsID: String,
    val ToolsID: String?,
    val ToolName: String? = null,
    val Title: String? = null,
    val Model: String? = null,
    val Manufacturer: String? = null,
    val SerialNumber: String? = null,
    val CalibrationDate: String? = null
)
