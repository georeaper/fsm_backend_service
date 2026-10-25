package com.example.feature.fieldreport

import com.example.feature.fieldreport.dto.FieldReportBulkRequest
import com.example.feature.fieldreport.dto.PostFieldReportResponse

object FieldReportPayloadNormalizer {
    fun normalizeNullableString(value: String?): String? = value?.takeIf { it.isNotBlank() }

    fun normalizeFieldReportRequest(input: PostFieldReportResponse): PostFieldReportResponse {
        return input.copy(
            FieldReportID = normalizeNullableString(input.FieldReportID),
            ReportNumber = normalizeNullableString(input.ReportNumber),
            Description = normalizeNullableString(input.Description),
            StartDate = normalizeNullableString(input.StartDate),
            EndDate = normalizeNullableString(input.EndDate),
            Title = normalizeNullableString(input.Title),
            Department = normalizeNullableString(input.Department),
            ClientName = normalizeNullableString(input.ClientName),
            ReportStatus = normalizeNullableString(input.Status ?: input.ReportStatus),
            Status = normalizeNullableString(input.Status ?: input.ReportStatus),
            Priority = normalizeNullableString(input.Priority),
            Type = normalizeNullableString(input.Type),
            CustomerID = normalizeNullableString(input.CustomerID),
            ContractID = normalizeNullableString(input.ContractID),
            UserID = normalizeNullableString(input.UserID),
            CaseID = normalizeNullableString(input.CaseID),
            ClientSignature = normalizeNullableString(input.ClientSignature),
            FieldReportTools = input.FieldReportTools?.map { tool ->
                tool.copy(
                    FieldReportToolsID = normalizeNullableString(tool.FieldReportToolsID),
                    FieldReportID = normalizeNullableString(tool.FieldReportID),
                    ToolsID = normalizeNullableString(tool.ToolsID),
                    LastModified = normalizeNullableString(tool.LastModified),
                    DateCreated = normalizeNullableString(tool.DateCreated),
                    Version = normalizeNullableString(tool.Version)
                )
            },
            FieldReportInventory = input.FieldReportInventory?.map { item ->
                item.copy(
                    FieldReportInventoryID = normalizeNullableString(item.FieldReportInventoryID),
                    FieldReportID = normalizeNullableString(item.FieldReportID),
                    InventoryID = normalizeNullableString(item.InventoryID),
                    LastModified = normalizeNullableString(item.LastModified),
                    DateCreated = normalizeNullableString(item.DateCreated),
                    Version = normalizeNullableString(item.Version)
                )
            },
            FieldReportEquipments = input.FieldReportEquipments?.map { equipment ->
                equipment.copy(
                    FieldReportEquipmentID = normalizeNullableString(equipment.FieldReportEquipmentID),
                    FieldReportID = normalizeNullableString(equipment.FieldReportID),
                    EquipmentID = normalizeNullableString(equipment.EquipmentID),
                    MaintenanceID = normalizeNullableString(equipment.MaintenanceID),
                    LastModified = normalizeNullableString(equipment.LastModified),
                    DateCreated = normalizeNullableString(equipment.DateCreated),
                    Version = normalizeNullableString(equipment.Version),
                    FieldReportCheckforms = equipment.FieldReportCheckforms?.map { checkForm ->
                        checkForm.copy(
                            FieldReportCheckFormID = normalizeNullableString(checkForm.FieldReportCheckFormID),
                            FieldReportEquipmentID = normalizeNullableString(checkForm.FieldReportEquipmentID),
                            Description = normalizeNullableString(checkForm.Description),
                            ValueExpected = normalizeNullableString(checkForm.ValueExpected),
                            ValueMeasured = normalizeNullableString(checkForm.ValueMeasured),
                            Result = normalizeNullableString(checkForm.Result),
                            LastModified = normalizeNullableString(checkForm.LastModified),
                            DateCreated = normalizeNullableString(checkForm.DateCreated),
                            Version = normalizeNullableString(checkForm.Version)
                        )
                    }
                )
            }
        )
    }

    fun normalizeBulkRequest(input: FieldReportBulkRequest): PostFieldReportResponse {
        return normalizeFieldReportRequest(input.toPostFieldReportResponse())
    }
}
