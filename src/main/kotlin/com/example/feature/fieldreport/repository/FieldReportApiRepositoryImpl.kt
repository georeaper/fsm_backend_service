package com.example.feature.fieldreport.repository

import com.example.core.DatabaseProvider
import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.*
import com.example.models.databaseModels.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FieldReportApiRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : FieldReportApiRepository {

    override fun findAll(ctx: RequestContext): List<FieldReportListDto> = transaction(dbProvider.getDatabase(ctx.dbName)) {
        fieldReportsTable.select { fieldReportsTable.isDeleted eq false }.map { row ->
            val customerId = row[fieldReportsTable.customerID]
            val contractId = row[fieldReportsTable.contractID]
            val caseId = row[fieldReportsTable.caseID]
            val userId = row[fieldReportsTable.userID]
            FieldReportListDto(
                FieldReportID = row[fieldReportsTable.fieldReportId],
                ReportNumber = row[fieldReportsTable.reportNumber], Title = row[fieldReportsTable.title],
                Description = row[fieldReportsTable.description], StartDate = row[fieldReportsTable.startDate],
                EndDate = row[fieldReportsTable.endDate], ReportStatus = row[fieldReportsTable.reportStatus],
                CustomerID = customerId, CustomerName = customerId?.let { customerName(it) },
                ContractID = contractId, ContractName = contractId?.let { contractName(it) },
                CaseID = caseId, TicketNumber = caseId?.let { ticketNumber(it) },
                UserID = userId, UserName = userId?.let { userName(it) },
                Department = row[fieldReportsTable.department], Value = row[fieldReportsTable.value],
                LastModified = row[fieldReportsTable.lastModified], DateCreated = row[fieldReportsTable.dateCreated]
            )
        }
    }

    override fun findById(ctx: RequestContext, id: String): FieldReportDetailDto? =
        transaction(dbProvider.getDatabase(ctx.dbName)) { detail(id) }

    override fun create(ctx: RequestContext, request: FieldReportCreateDto): FieldReportDetailDto =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            val id = UUID.randomUUID().toString()
            val now = DateUtils.nowStorage()
            fieldReportsTable.insert { row ->
                row[fieldReportId] = id; row[reportNumber] = request.ReportNumber; row[title] = request.Title
                row[description] = request.Description; row[startDate] = request.StartDate; row[endDate] = request.EndDate
                row[reportStatus] = request.ReportStatus; row[customerID] = request.CustomerID
                row[contractID] = request.ContractID; row[userID] = request.UserID; row[caseID] = request.TicketID
                row[clientName] = request.ClientName; row[clientSignature] = request.ClientSignature?.toByteArray()
                row[department] = request.Department; row[value] = request.Value
                row[lastModified] = now; row[dateCreated] = now; row[version] = "1"
            }
            replaceEquipments(id, request.Equipments, now)
            replaceInventories(id, request.Inventories, now)
            replaceTools(id, request.SpecialTools, now)
            detail(id)!!
        }

    override fun update(ctx: RequestContext, id: String, request: FieldReportUpdateDto): FieldReportDetailDto? =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            val changed = fieldReportsTable.update({ (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false) }) { row ->
                request.ReportNumber?.let { row[reportNumber] = it }; request.Title?.let { row[title] = it }
                request.Description?.let { row[description] = it }; request.StartDate?.let { row[startDate] = it }
                request.EndDate?.let { row[endDate] = it }; request.ReportStatus?.let { row[reportStatus] = it }
                request.CustomerID?.let { row[customerID] = it }; request.ContractID?.let { row[contractID] = it }
                (request.TicketID ?: request.CaseID)?.let { row[caseID] = it }; request.UserID?.let { row[userID] = it }
                request.ClientName?.let { row[clientName] = it }; request.ClientSignature?.let { row[clientSignature] = it.toByteArray() }
                request.Department?.let { row[department] = it }; request.Value?.let { row[value] = it }
                row[lastModified] = DateUtils.nowStorage()
            }
            if (changed == 0) null else detail(id)
        }

    override fun syncEquipments(ctx: RequestContext, id: String, request: FieldReportEquipmentSyncRequest) =
        sync(ctx, id) { now -> replaceEquipments(id, request.Equipments, now) }

    override fun syncInventories(ctx: RequestContext, id: String, request: FieldReportInventorySyncRequest) =
        sync(ctx, id) { now -> replaceInventories(id, request.Inventories, now) }

    override fun syncTools(ctx: RequestContext, id: String, request: FieldReportToolsSyncRequest) =
        sync(ctx, id) { now -> replaceTools(id, request.Tools, now) }

    override fun delete(ctx: RequestContext, id: String): Boolean = transaction(dbProvider.getDatabase(ctx.dbName)) {
        fieldReportsTable.update({ (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false) }) {
            it[isDeleted] = true; it[lastModified] = DateUtils.nowStorage()
        } > 0
    }

    private fun sync(ctx: RequestContext, id: String, action: (String) -> Unit): FieldReportDetailDto? =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            if (!fieldReportsTable.select { (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false) }.any()) return@transaction null
            val now = DateUtils.nowStorage(); action(now)
            fieldReportsTable.update({ fieldReportsTable.fieldReportId eq id }) { it[lastModified] = now }
            detail(id)
        }

    private fun replaceEquipments(fieldReportId: String, values: List<FieldReportEquipmentWriteDto>, now: String) {
        val oldIds = fieldReportEquipmentTable.select { fieldReportEquipmentTable.fieldReportID eq fieldReportId }
            .map { it[fieldReportEquipmentTable.fieldReportEquipmentID] }
        oldIds.forEach { equipmentId ->
            fieldReportCheckformTable.deleteWhere { fieldReportCheckformTable.fieldReportEquipmentId eq equipmentId }
        }
        fieldReportEquipmentTable.deleteWhere { fieldReportEquipmentTable.fieldReportID eq fieldReportId }
        values.forEach { value ->
            val equipmentId = value.FieldReportEquipmentID?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
            fieldReportEquipmentTable.insert { row ->
                row[fieldReportEquipmentID] = equipmentId; row[fieldReportID] = fieldReportId; row[equipmentID] = value.EquipmentID
                row[maintenanceID] = value.MaintenanceID; row[completedStatus] = value.CompletedStatus
                row[lastModified] = now; row[dateCreated] = now; row[version] = "1"; row[isDeleted] = false
            }
            value.FieldReportCheckforms.forEach { check ->
                fieldReportCheckformTable.insert { row ->
                    row[fieldReportCheckFormId] = check.FieldReportCheckFormID?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
                    row[fieldReportEquipmentId] = equipmentId; row[description] = check.Description
                    row[valueExpected] = check.ValueExpected; row[valueMeasured] = check.ValueMeasured; row[result] = check.Result
                    row[lastModified] = now; row[dateCreated] = now; row[version] = "1"; row[isDeleted] = false
                }
            }
        }
    }

    private fun replaceInventories(fieldReportId: String, values: List<FieldReportInventoryWriteDto>, now: String) {
        fieldReportInventoryTable.deleteWhere { fieldReportInventoryTable.fieldReportID eq fieldReportId }
        values.forEach { value -> fieldReportInventoryTable.insert { row ->
            row[fieldReportInventoryId] = value.FieldReportInventoryID?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
            row[fieldReportID] = fieldReportId; row[inventoryID] = value.InventoryID; row[lastModified] = now
            row[dateCreated] = now; row[version] = "1"; row[isDeleted] = false
        } }
    }

    private fun replaceTools(fieldReportId: String, values: List<FieldReportToolsWriteDto>, now: String) {
        fieldReportToolsTable.deleteWhere { fieldReportToolsTable.fieldReportId eq fieldReportId }
        values.forEach { value -> fieldReportToolsTable.insert { row ->
            row[fieldReportToolsId] = value.FieldReportToolsID?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
            row[fieldReportToolsTable.fieldReportId] = fieldReportId; row[toolsId] = value.ToolsID; row[lastModified] = now
            row[dateCreated] = now; row[version] = "1"; row[isDeleted] = false
        } }
    }

    private fun detail(id: String): FieldReportDetailDto? {
        val report = fieldReportsTable.select { (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false) }.singleOrNull() ?: return null
        val equipments = fieldReportEquipmentTable.select { (fieldReportEquipmentTable.fieldReportID eq id) and (fieldReportEquipmentTable.isDeleted eq false) }.map { equipment ->
            val equipmentId = equipment[fieldReportEquipmentTable.fieldReportEquipmentID]
            val sourceEquipmentId = equipment[fieldReportEquipmentTable.equipmentID]
            val sourceMaintenanceId = equipment[fieldReportEquipmentTable.maintenanceID]
            val sourceEquipment = sourceEquipmentId?.let { sourceId ->
                equipmentTable.select { (equipmentTable.equipmentId eq sourceId) and (equipmentTable.isDeleted eq false) }.singleOrNull()
            }
            val sourceMaintenance = sourceMaintenanceId?.let { sourceId ->
                maintenancesTable.select { (maintenancesTable.maintenanceId eq sourceId) and (maintenancesTable.isDeleted eq false) }.singleOrNull()
            }
            FieldReportEquipmentDetailDto(
                FieldReportEquipmentID = equipmentId,
                EquipmentID = sourceEquipmentId,
                EquipmentName = sourceEquipment?.get(equipmentTable.name),
                EquipmentModel = sourceEquipment?.get(equipmentTable.model),
                SerialNumber = sourceEquipment?.get(equipmentTable.serialNumber),
                MaintenanceID = sourceMaintenanceId,
                MaintenanceName = sourceMaintenance?.get(maintenancesTable.name),
                CompletedStatus = equipment[fieldReportEquipmentTable.completedStatus],
                FieldReportCheckforms = fieldReportCheckformTable.select { (fieldReportCheckformTable.fieldReportEquipmentId eq equipmentId) and (fieldReportCheckformTable.isDeleted eq false) }.map { check ->
                    FieldReportCheckformDetailDto(check[fieldReportCheckformTable.fieldReportCheckFormId], check[fieldReportCheckformTable.description], check[fieldReportCheckformTable.valueExpected], check[fieldReportCheckformTable.valueMeasured], check[fieldReportCheckformTable.result])
                }
            )
        }
        val inventories = fieldReportInventoryTable.select { (fieldReportInventoryTable.fieldReportID eq id) and (fieldReportInventoryTable.isDeleted eq false) }
            .map { inventory ->
                val inventoryId = inventory[fieldReportInventoryTable.inventoryID]
                val inventoryName = inventoryId?.let { sourceId ->
                    inventoryTable.select { (inventoryTable.inventoryId eq sourceId) and (inventoryTable.isDeleted eq false) }
                        .singleOrNull()?.get(inventoryTable.title)
                }
                FieldReportInventoryDetailDto(inventory[fieldReportInventoryTable.fieldReportInventoryId], inventoryId, inventoryName)
            }
        val tools = fieldReportToolsTable.select { (fieldReportToolsTable.fieldReportId eq id) and (fieldReportToolsTable.isDeleted eq false) }
            .map { tool ->
                val toolId = tool[fieldReportToolsTable.toolsId]
                val sourceTool = toolId?.let { sourceId ->
                    toolsTable.select { (toolsTable.toolsId eq sourceId) and (toolsTable.isDeleted eq false) }.singleOrNull()
                }
                FieldReportToolsDetailDto(
                    FieldReportToolsID = tool[fieldReportToolsTable.fieldReportToolsId],
                    ToolsID = toolId,
                    ToolName = sourceTool?.get(toolsTable.title),
                    Title = sourceTool?.get(toolsTable.title),
                    Model = sourceTool?.get(toolsTable.model),
                    Manufacturer = sourceTool?.get(toolsTable.manufacturer),
                    SerialNumber = sourceTool?.get(toolsTable.serialNumber),
                    CalibrationDate = sourceTool?.get(toolsTable.calibrationDate)
                )
            }
        val customerId = report[fieldReportsTable.customerID]
        val contractId = report[fieldReportsTable.contractID]
        val userId = report[fieldReportsTable.userID]
        val ticketId = report[fieldReportsTable.caseID]
        return FieldReportDetailDto(
            FieldReportID = id,
            RemoteID = report[fieldReportsTable.remoteID],
            ReportNumber = report[fieldReportsTable.reportNumber],
            Title = report[fieldReportsTable.title],
            Description = report[fieldReportsTable.description],
            StartDate = report[fieldReportsTable.startDate],
            EndDate = report[fieldReportsTable.endDate],
            ReportStatus = report[fieldReportsTable.reportStatus],
            CustomerID = customerId,
            CustomerName = customerId?.let { customerName(it) },
            ContractID = contractId,
            ContractName = contractId?.let { contractName(it) },
            UserID = userId,
            UserName = userId?.let { userName(it) },
            TicketID = ticketId,
            TicketNumber = ticketId?.let { ticketNumber(it) },
            ClientName = report[fieldReportsTable.clientName],
            ClientSignature = report[fieldReportsTable.clientSignature]?.toString(Charsets.UTF_8),
            Department = report[fieldReportsTable.department],
            Value = report[fieldReportsTable.value],
            LastModified = report[fieldReportsTable.lastModified],
            DateCreated = report[fieldReportsTable.dateCreated],
            Version = report[fieldReportsTable.version],
            Equipments = equipments,
            Inventories = inventories,
            SpecialTools = tools
        )
    }

    private fun customerName(id: String) = customerTable.select { customerTable.customerId eq id }.singleOrNull()?.get(customerTable.name)
    private fun contractName(id: String) = contractsTable.select { contractsTable.contractId eq id }.singleOrNull()?.get(contractsTable.title)
    private fun ticketNumber(id: String) = ticketTable.select { ticketTable.ticketId eq id }.singleOrNull()?.get(ticketTable.ticketNumber)
    private fun userName(id: String) = userTable.select { userTable.userId eq id }.singleOrNull()?.get(userTable.name)
}
