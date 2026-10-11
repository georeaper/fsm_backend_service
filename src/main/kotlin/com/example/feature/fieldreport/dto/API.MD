
GET /fieldreports	FieldReportListDto
GET /fieldreports/{id}	FieldReportDetailDto
POST /fieldreports/bulk	FieldReportCreateDto
PUT /fieldreports/{id}	FieldReportUpdateDto
PUT /fieldreports/{id}/equipments	FieldReportEquipmentSyncRequest
PUT /fieldreports/{id}/inventories	FieldReportInventorySyncRequest
PUT /fieldreports/{id}/special-tools	FieldReportToolsSyncRequest
Delete /fieldreports/{id}  ->use parameter id to delete a field report

@Serializable
data class FieldReportCreateDto(
val ReportNumber: String? = null,
val Title: String? = null,
val Description: String? = null,
val StartDate: String? = null,
val EndDate: String? = null,
val ReportStatus: String? = null,

    val CustomerID: String? = null,
    val ClientName: String? = null,
    val ClientSignature: String? = null,

    val Department: String? = null,
    val Value: Double? = null,

    val Equipments: List<FieldReportEquipmentWriteDto> = emptyList(),
    val Inventories: List<FieldReportInventoryWriteDto> = emptyList(),
    val SpecialTools: List<FieldReportToolsWriteDto> = emptyList()
)

@Serializable
data class FieldReportDetailDto(
val FieldReportID: String,
val RemoteID: Int? = null,

    val ReportNumber: String? = null,
    val Title: String? = null,
    val Description: String? = null,
    val StartDate: String? = null,
    val EndDate: String? = null,
    val ReportStatus: String? = null,

    val CustomerID: String? = null,
    val CustomerName: String? = null,

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
val MaintenanceID: String?,
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
val InventoryID: String?
)
@Serializable
data class FieldReportToolsDetailDto(
val FieldReportToolsID: String,
val ToolsID: String?
)

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
@Serializable
data class FieldReportListDto(
val FieldReportID: String,
val ReportNumber: String? = null,
val Title: String? = null,
val Description: String? = null,
val StartDate: String? = null,
val EndDate: String? = null,
val ReportStatus: String? = null,

    val CustomerID: String? = null,
    val CustomerName: String? = null,

    val ContractID: String? = null,
    val ContractName: String? = null,

    val CaseID: String? = null,
    val TicketNumber: String? = null,

    val UserID: String? = null,
    val UserName: String? = null,

    val Department: String? = null,
    val Value: Double? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null
)