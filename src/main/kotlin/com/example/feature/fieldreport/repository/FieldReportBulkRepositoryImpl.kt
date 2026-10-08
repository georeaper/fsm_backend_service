package com.example.feature.fieldreport.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.FieldReportCheckForm
import com.example.feature.fieldreport.dto.FieldReportEquipment
import com.example.feature.fieldreport.dto.FieldReportInventory
import com.example.feature.fieldreport.dto.FieldReportTools
import com.example.feature.fieldreport.dto.PostFieldReportResponse
import com.example.models.databaseModels.fieldReportCheckformTable
import com.example.models.databaseModels.fieldReportEquipmentTable
import com.example.models.databaseModels.fieldReportInventoryTable
import com.example.models.databaseModels.fieldReportToolsTable
import com.example.models.databaseModels.fieldReportsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FieldReportBulkRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : FieldReportBulkRepository {

    private fun normalizeNullableString(value: String?): String? = value?.takeIf { it.isNotBlank() }

    override fun saveBulk(ctx: RequestContext, request: PostFieldReportResponse): PostFieldReportResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        val normalizedRequest = request.copy(
            FieldReportID = normalizeNullableString(request.FieldReportID),
            ReportNumber = normalizeNullableString(request.ReportNumber),
            Description = normalizeNullableString(request.Description),
            StartDate = normalizeNullableString(request.StartDate),
            EndDate = normalizeNullableString(request.EndDate),
            Title = normalizeNullableString(request.Title),
            Department = normalizeNullableString(request.Department),
            ClientName = normalizeNullableString(request.ClientName),
            ReportStatus = normalizeNullableString(request.ReportStatus),
            CustomerID = normalizeNullableString(request.CustomerID),
            ContractID = normalizeNullableString(request.ContractID),
            UserID = normalizeNullableString(request.UserID),
            CaseID = normalizeNullableString(request.CaseID),
            ClientSignature = normalizeNullableString(request.ClientSignature),
            FieldReportTools = request.FieldReportTools?.map { tool ->
                tool.copy(
                    FieldReportToolsID = normalizeNullableString(tool.FieldReportToolsID),
                    FieldReportID = normalizeNullableString(tool.FieldReportID),
                    ToolsID = normalizeNullableString(tool.ToolsID),
                    LastModified = normalizeNullableString(tool.LastModified),
                    DateCreated = normalizeNullableString(tool.DateCreated),
                    Version = normalizeNullableString(tool.Version)
                )
            },
            FieldReportInventory = request.FieldReportInventory?.map { item ->
                item.copy(
                    FieldReportInventoryID = normalizeNullableString(item.FieldReportInventoryID),
                    FieldReportID = normalizeNullableString(item.FieldReportID),
                    InventoryID = normalizeNullableString(item.InventoryID),
                    LastModified = normalizeNullableString(item.LastModified),
                    DateCreated = normalizeNullableString(item.DateCreated),
                    Version = normalizeNullableString(item.Version)
                )
            },
            FieldReportEquipments = request.FieldReportEquipments?.map { equipment ->
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

        val fieldReportId = normalizedRequest.FieldReportID ?: UUID.randomUUID().toString()
        val now = java.time.LocalDateTime.now().toString()

        val insertedTools = mutableListOf<FieldReportTools>()
        val insertedInventory = mutableListOf<FieldReportInventory>()
        val insertedEquipments = mutableListOf<FieldReportEquipment>()

        transaction(db) {
            fieldReportsTable.insert { row ->
                row[fieldReportsTable.fieldReportId] = fieldReportId
                row[fieldReportsTable.reportNumber] = normalizedRequest.ReportNumber
                row[fieldReportsTable.description] = normalizedRequest.Description
                row[fieldReportsTable.startDate] = normalizedRequest.StartDate
                row[fieldReportsTable.endDate] = normalizedRequest.EndDate
                row[fieldReportsTable.title] = normalizedRequest.Title
                row[fieldReportsTable.department] = normalizedRequest.Department
                row[fieldReportsTable.clientName] = normalizedRequest.ClientName
                row[fieldReportsTable.reportStatus] = normalizedRequest.ReportStatus
                row[fieldReportsTable.value] = normalizedRequest.Value
                row[fieldReportsTable.lastModified] = now
                row[fieldReportsTable.dateCreated] = now
                row[fieldReportsTable.version] = normalizedRequest.Version ?: "1"
                row[fieldReportsTable.customerID] = normalizedRequest.CustomerID
                row[fieldReportsTable.contractID] = normalizedRequest.ContractID
                row[fieldReportsTable.userID] = normalizedRequest.UserID
                row[fieldReportsTable.caseID] = normalizedRequest.CaseID
            }

            normalizedRequest.FieldReportTools.orEmpty().forEach { tool ->
                val toolId = tool.FieldReportToolsID ?: UUID.randomUUID().toString()
                fieldReportToolsTable.insert { row ->
                    row[fieldReportToolsTable.fieldReportToolsId] = toolId
                    row[fieldReportToolsTable.fieldReportId] = fieldReportId
                    row[fieldReportToolsTable.toolsId] = tool.ToolsID
                    row[fieldReportToolsTable.lastModified] = tool.LastModified ?: now
                    row[fieldReportToolsTable.dateCreated] = tool.DateCreated ?: now
                    row[fieldReportToolsTable.version] = tool.Version ?: "1"
                }
                insertedTools += tool.copy(
                    FieldReportToolsID = toolId,
                    FieldReportID = fieldReportId
                )
            }

            normalizedRequest.FieldReportInventory.orEmpty().forEach { item ->
                val inventoryId = item.FieldReportInventoryID ?: UUID.randomUUID().toString()
                fieldReportInventoryTable.insert { row ->
                    row[fieldReportInventoryTable.fieldReportInventoryId] = inventoryId
                    row[fieldReportInventoryTable.fieldReportID] = fieldReportId
                    row[fieldReportInventoryTable.inventoryID] = item.InventoryID
                    row[fieldReportInventoryTable.lastModified] = item.LastModified ?: now
                    row[fieldReportInventoryTable.dateCreated] = item.DateCreated ?: now
                    row[fieldReportInventoryTable.version] = item.Version ?: "1"
                }
                insertedInventory += item.copy(
                    FieldReportInventoryID = inventoryId,
                    FieldReportID = fieldReportId
                )
            }

            normalizedRequest.FieldReportEquipments.orEmpty().forEach { equipment ->
                val equipmentId = equipment.FieldReportEquipmentID ?: UUID.randomUUID().toString()
                fieldReportEquipmentTable.insert { row ->
                    row[fieldReportEquipmentTable.fieldReportEquipmentID] = equipmentId
                    row[fieldReportEquipmentTable.fieldReportID] = fieldReportId
                    row[fieldReportEquipmentTable.equipmentID] = equipment.EquipmentID
                    row[fieldReportEquipmentTable.maintenanceID] = equipment.MaintenanceID
                    row[fieldReportEquipmentTable.completedStatus] = equipment.CompletedStatus
                    row[fieldReportEquipmentTable.lastModified] = equipment.LastModified ?: now
                    row[fieldReportEquipmentTable.dateCreated] = equipment.DateCreated ?: now
                    row[fieldReportEquipmentTable.version] = equipment.Version ?: "1"
                }

                val insertedCheckForms = mutableListOf<FieldReportCheckForm>()
                equipment.FieldReportCheckforms.orEmpty().forEach { checkForm ->
                    val checkFormId = checkForm.FieldReportCheckFormID ?: UUID.randomUUID().toString()
                    fieldReportCheckformTable.insert { row ->
                        row[fieldReportCheckformTable.fieldReportCheckFormId] = checkFormId
                        row[fieldReportCheckformTable.fieldReportEquipmentId] = equipmentId
                        row[fieldReportCheckformTable.description] = checkForm.Description
                        row[fieldReportCheckformTable.valueExpected] = checkForm.ValueExpected
                        row[fieldReportCheckformTable.valueMeasured] = checkForm.ValueMeasured
                        row[fieldReportCheckformTable.result] = checkForm.Result
                        row[fieldReportCheckformTable.lastModified] = checkForm.LastModified ?: now
                        row[fieldReportCheckformTable.dateCreated] = checkForm.DateCreated ?: now
                        row[fieldReportCheckformTable.version] = checkForm.Version ?: "1"
                    }
                    insertedCheckForms += checkForm.copy(
                        FieldReportCheckFormID = checkFormId,
                        FieldReportEquipmentID = equipmentId
                    )
                }

                insertedEquipments += equipment.copy(
                    FieldReportEquipmentID = equipmentId,
                    FieldReportID = fieldReportId,
                    FieldReportCheckforms = insertedCheckForms
                )
            }
        }

        return normalizedRequest.copy(
            FieldReportID = fieldReportId,
            FieldReportTools = insertedTools,
            FieldReportInventory = insertedInventory,
            FieldReportEquipments = insertedEquipments
        )
    }
}
