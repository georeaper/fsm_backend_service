package com.example.feature.fieldreport.repository

import com.example.core.DatabaseProvider
import com.example.core.DateUtils
import com.example.core.DocumentNumberGenerator
import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.*
import com.example.models.databaseModels.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FieldReportApiRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : FieldReportApiRepository {

    override fun findAll(ctx: RequestContext): List<FieldReportListDto> = transaction(dbProvider.getDatabase(ctx.dbName)) {
        fieldReportsTable.select { fieldReportsTable.isDeleted eq false }
            .orderBy(fieldReportsTable.dateCreated to SortOrder.DESC, fieldReportsTable.fieldReportId to SortOrder.ASC)
            .map { row ->
            val customerId = row[fieldReportsTable.customerID]
            val contractId = row[fieldReportsTable.contractID]
            val caseId = row[fieldReportsTable.caseID]
            val userId = row[fieldReportsTable.userID]
            FieldReportListDto(
                FieldReportID = row[fieldReportsTable.fieldReportId],
                ReportNumber = row[fieldReportsTable.reportNumber], Title = row[fieldReportsTable.title],
                Description = row[fieldReportsTable.description], StartDate = row[fieldReportsTable.startDate],
                EndDate = row[fieldReportsTable.endDate], ScheduledStart = row[fieldReportsTable.scheduledStart],
                ScheduledEnd = row[fieldReportsTable.scheduledEnd], ReportStatus = row[fieldReportsTable.reportStatus],
                Status = row[fieldReportsTable.reportStatus], Priority = row[fieldReportsTable.priority], Type = row[fieldReportsTable.workOrderType],
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
            val schedule = validatedSchedule(request.ScheduledStart, request.ScheduledEnd)
            val technicalCaseIds = (request.TechnicalCaseIDs + listOfNotNull(request.TicketID))
                .map(String::trim).filter(String::isNotEmpty).distinct()
            requireExistingTechnicalCases(technicalCaseIds)
            requireTechnicalCasesBelongToCustomer(technicalCaseIds, request.CustomerID)
            requireOpenTechnicalCases(technicalCaseIds)
            val technicians = request.Technicians.ifEmpty {
                listOfNotNull(request.UserID?.let { FieldReportTechnicianWriteDto(it, "LEAD") })
            }
            val legacyLeadId = technicians.firstOrNull { it.Role.trim().uppercase() == "LEAD" }?.UserID
            val generatedReportNumber = DocumentNumberGenerator.nextWorkOrderNumber(ctx)
            fieldReportsTable.insert { row ->
                row[fieldReportId] = id; row[reportNumber] = generatedReportNumber; row[title] = request.Title
                row[description] = request.Description; row[startDate] = request.StartDate; row[endDate] = request.EndDate
                row[scheduledStart] = schedule.first; row[scheduledEnd] = schedule.second
                row[reportStatus] = request.Status ?: request.ReportStatus; row[priority] = request.Priority; row[workOrderType] = request.Type; row[customerID] = request.CustomerID
                row[contractID] = request.ContractID; row[userID] = legacyLeadId; row[caseID] = technicalCaseIds.firstOrNull()
                row[clientName] = request.ClientName; row[clientSignature] = request.ClientSignature?.toByteArray()
                row[department] = request.Department; row[value] = request.Value
                row[lastModified] = now; row[dateCreated] = now; row[version] = "1"
            }
            replaceEquipments(id, request.Equipments, now)
            replaceInventories(id, request.Inventories, now)
            replaceTools(id, request.SpecialTools, now)
            replaceTechnicalCaseLinks(id, technicalCaseIds, now)
            replaceTechnicianAssignments(id, technicians, now)
            detail(id)!!
        }

    override fun update(ctx: RequestContext, id: String, request: FieldReportUpdateDto): FieldReportDetailDto? =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            val existing = fieldReportsTable.select {
                (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false)
            }.singleOrNull() ?: return@transaction null
            request.CustomerID?.let { newCustomerId ->
                requireTechnicalCasesBelongToCustomer(activeTechnicalCaseIds(id), newCustomerId)
            }
            val schedule = validatedSchedule(
                request.ScheduledStart ?: existing[fieldReportsTable.scheduledStart],
                request.ScheduledEnd ?: existing[fieldReportsTable.scheduledEnd]
            )
            val changed = fieldReportsTable.update({ (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false) }) { row ->
                request.Title?.let { row[title] = it }
                request.Description?.let { row[description] = it }; request.StartDate?.let { row[startDate] = it }
                request.EndDate?.let { row[endDate] = it }; (request.Status ?: request.ReportStatus)?.let { row[reportStatus] = it }
                if (request.ScheduledStart != null || request.ScheduledEnd != null) {
                    row[scheduledStart] = schedule.first; row[scheduledEnd] = schedule.second
                }
                request.Priority?.let { row[priority] = it }
                request.Type?.let { row[workOrderType] = it }
                request.CustomerID?.let { row[customerID] = it }; request.ContractID?.let { row[contractID] = it }
                (request.TicketID ?: request.CaseID)?.let { row[caseID] = it }; request.UserID?.let { row[userID] = it }
                request.ClientName?.let { row[clientName] = it }; request.ClientSignature?.let { row[clientSignature] = it.toByteArray() }
                request.Department?.let { row[department] = it }; request.Value?.let { row[value] = it }
                row[lastModified] = DateUtils.nowStorage()
            }
            if (changed == 0) null else {
                request.UserID?.let { legacyLeadId ->
                    val assignments = technicianDetails(id)
                        .filterNot { it.Role == "LEAD" || it.UserID == legacyLeadId }
                        .map { FieldReportTechnicianWriteDto(it.UserID, it.Role) } +
                        FieldReportTechnicianWriteDto(legacyLeadId, "LEAD")
                    replaceTechnicianAssignments(id, assignments, DateUtils.nowStorage())
                }
                detail(id)
            }
        }

    override fun syncEquipments(ctx: RequestContext, id: String, request: FieldReportEquipmentSyncRequest) =
        sync(ctx, id) { now -> replaceEquipments(id, request.Equipments, now) }

    override fun syncInventories(ctx: RequestContext, id: String, request: FieldReportInventorySyncRequest) =
        sync(ctx, id) { now -> replaceInventories(id, request.Inventories, now) }

    override fun syncTools(ctx: RequestContext, id: String, request: FieldReportToolsSyncRequest) =
        sync(ctx, id) { now -> replaceTools(id, request.Tools, now) }

    override fun technicalCases(ctx: RequestContext, id: String): List<FieldReportTechnicalCaseDto>? =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            if (!fieldReportExists(id)) null else technicalCaseDetails(id)
        }

    override fun replaceTechnicalCases(
        ctx: RequestContext,
        id: String,
        technicalCaseIds: List<String>
    ): List<FieldReportTechnicalCaseDto>? = transaction(dbProvider.getDatabase(ctx.dbName)) {
        if (!fieldReportExists(id)) return@transaction null
        val normalizedIds = technicalCaseIds.map(String::trim).filter(String::isNotEmpty).distinct()
        requireExistingTechnicalCases(normalizedIds)
        val existingIds = activeTechnicalCaseIds(id).toSet()
        requireOpenTechnicalCases(normalizedIds.filterNot(existingIds::contains))
        val workOrderCustomerId = fieldReportsTable.slice(fieldReportsTable.customerID)
            .select { fieldReportsTable.fieldReportId eq id }.single()[fieldReportsTable.customerID]
        requireTechnicalCasesBelongToCustomer(normalizedIds, workOrderCustomerId)
        val now = DateUtils.nowStorage()
        replaceTechnicalCaseLinks(id, normalizedIds, now)
        fieldReportsTable.update({ fieldReportsTable.fieldReportId eq id }) {
            it[caseID] = normalizedIds.firstOrNull()
            it[lastModified] = now
        }
        technicalCaseDetails(id)
    }

    override fun technicians(ctx: RequestContext, id: String): List<FieldReportTechnicianDto>? =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            if (!fieldReportExists(id)) null else technicianDetails(id)
        }

    override fun replaceTechnicians(
        ctx: RequestContext,
        id: String,
        technicians: List<FieldReportTechnicianWriteDto>
    ): List<FieldReportTechnicianDto>? = transaction(dbProvider.getDatabase(ctx.dbName)) {
        if (!fieldReportExists(id)) return@transaction null
        val now = DateUtils.nowStorage()
        replaceTechnicianAssignments(id, technicians, now)
        val leadId = technicians.firstOrNull { it.Role.trim().uppercase() == "LEAD" }?.UserID
        fieldReportsTable.update({ fieldReportsTable.fieldReportId eq id }) {
            it[userID] = leadId
            it[lastModified] = now
        }
        technicianDetails(id)
    }

    override fun scheduledWorkOrders(
        ctx: RequestContext,
        from: String,
        to: String,
        technicianId: String?,
        status: String?
    ): List<SchedulingWorkOrderDto> = transaction(dbProvider.getDatabase(ctx.dbName)) {
        val normalizedFrom = DateUtils.normalizeDateTimeStorage(from)
            ?: throw IllegalArgumentException("from must use yyyyMMddHHmmss or dd/MM/yyyy HH:mm:ss")
        val normalizedTo = DateUtils.normalizeDateTimeStorage(to)
            ?: throw IllegalArgumentException("to must use yyyyMMddHHmmss or dd/MM/yyyy HH:mm:ss")
        require(normalizedFrom <= normalizedTo) { "from must not be after to" }
        require((DateUtils.rangeDays(normalizedFrom, normalizedTo) ?: Long.MAX_VALUE) <= 366) {
            "Calendar range must not exceed 366 days"
        }
        var condition: Op<Boolean> = (fieldReportsTable.isDeleted eq false) and
            fieldReportsTable.scheduledStart.isNotNull() and fieldReportsTable.scheduledEnd.isNotNull() and
            (fieldReportsTable.scheduledStart lessEq normalizedTo) and
            (fieldReportsTable.scheduledEnd greaterEq normalizedFrom)
        status?.takeIf(String::isNotBlank)?.let { condition = condition and (fieldReportsTable.reportStatus eq it) }
        val allowedIds = technicianId?.takeIf(String::isNotBlank)?.let { selectedTechnician ->
            fieldReportTechniciansTable.slice(fieldReportTechniciansTable.fieldReportId).select {
                (fieldReportTechniciansTable.userId eq selectedTechnician) and
                    (fieldReportTechniciansTable.isDeleted eq false)
            }.map { it[fieldReportTechniciansTable.fieldReportId] }.toSet()
        }
        val typeColors = settingsTable
            .slice(settingsTable.settingsValue, settingsTable.settingsStyle)
            .select { settingsTable.settingsKey eq "WorkOrderType" }
            .mapNotNull { setting ->
                val value = setting[settingsTable.settingsValue]?.trim()?.lowercase() ?: return@mapNotNull null
                value to setting[settingsTable.settingsStyle]
            }.toMap()
        fieldReportsTable.select { condition }
            .orderBy(fieldReportsTable.scheduledStart to SortOrder.ASC, fieldReportsTable.fieldReportId to SortOrder.ASC)
            .filter { allowedIds == null || it[fieldReportsTable.fieldReportId] in allowedIds }
            .map { row ->
                val reportId = row[fieldReportsTable.fieldReportId]
                val customerId = row[fieldReportsTable.customerID]
                val type = row[fieldReportsTable.workOrderType]
                SchedulingWorkOrderDto(
                    FieldReportID = reportId,
                    ReportNumber = row[fieldReportsTable.reportNumber],
                    Title = row[fieldReportsTable.title],
                    ScheduledStart = requireNotNull(row[fieldReportsTable.scheduledStart]),
                    ScheduledEnd = requireNotNull(row[fieldReportsTable.scheduledEnd]),
                    Status = row[fieldReportsTable.reportStatus],
                    Type = type,
                    TypeColor = type?.trim()?.lowercase()?.let(typeColors::get),
                    CustomerID = customerId,
                    CustomerName = customerId?.let(::customerName),
                    Technicians = technicianDetails(reportId)
                )
            }
    }

    override fun complete(
        ctx: RequestContext,
        id: String,
        request: CompleteFieldReportRequest
    ): CompleteFieldReportResponse? = transaction(dbProvider.getDatabase(ctx.dbName)) {
        if (!fieldReportExists(id)) return@transaction null
        requireConfiguredStatus("WorkOrderStatus", request.WorkOrderStatus, "CLOSED")

        val linkedIds = activeTechnicalCaseIds(id)
        require(completionDecisionsCoverLinkedCases(linkedIds, request.TechnicalCaseUpdates)) {
            "TechnicalCaseUpdates must contain exactly one status decision for every linked technical case"
        }
        request.TechnicalCaseUpdates.forEach { requireConfiguredStatus("TechnicalCaseStatus", it.Status) }

        val now = DateUtils.nowStorage()
        request.TechnicalCaseUpdates.forEach { update ->
            val current = ticketTable.select {
                (ticketTable.ticketId eq update.TechnicalCaseID) and (ticketTable.isDeleted eq false)
            }.single()
            ticketHistoryTable.insert { history ->
                history[ticketHistoryId] = UUID.randomUUID().toString()
                history[ticketId] = current[ticketTable.ticketId]
                history[title] = current[ticketTable.title]
                history[ticketNumber] = current[ticketTable.ticketNumber]
                history[description] = current[ticketTable.description]
                history[notes] = current[ticketTable.notes]?.take(50)
                history[urgency] = current[ticketTable.urgency]
                history[status] = current[ticketTable.status]
                history[caseType] = current[ticketTable.caseType]
                history[active] = current[ticketTable.active]
                history[dateStart] = current[ticketTable.dateStart]
                history[dateEnd] = current[ticketTable.dateEnd]
                history[lastModified] = now
                history[dateCreated] = current[ticketTable.dateCreated]
                history[version] = current[ticketTable.version]
                history[userId] = current[ticketTable.userId]
                history[customerId] = current[ticketTable.customerId]
                history[equipmentId] = current[ticketTable.equipmentId]
            }
            val systemCategory = statusSystemCategory("TechnicalCaseStatus", update.Status)
            ticketTable.update({ ticketTable.ticketId eq update.TechnicalCaseID }) {
                it[status] = update.Status
                update.ResolutionNotes?.let { notes -> it[description] = notes }
                it[active] = systemCategory != "CLOSED" && systemCategory != "CANCELED"
                it[lastModified] = now
                if (systemCategory == "CLOSED") it[dateEnd] = now
            }
            fieldReportTechnicalCasesTable.update({
                (fieldReportTechnicalCasesTable.fieldReportId eq id) and
                    (fieldReportTechnicalCasesTable.technicalCaseId eq update.TechnicalCaseID) and
                    (fieldReportTechnicalCasesTable.isDeleted eq false)
            }) {
                it[resolutionStatus] = update.Status
                it[resolutionNotes] = update.ResolutionNotes
                it[lastModified] = now
            }
        }
        fieldReportsTable.update({ fieldReportsTable.fieldReportId eq id }) {
            it[reportStatus] = request.WorkOrderStatus
            it[lastModified] = now
        }
        CompleteFieldReportResponse(id, request.WorkOrderStatus, technicalCaseDetails(id))
    }

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

    private fun fieldReportExists(id: String) = fieldReportsTable.select {
        (fieldReportsTable.fieldReportId eq id) and (fieldReportsTable.isDeleted eq false)
    }.any()

    private fun requireExistingTechnicalCases(ids: List<String>) {
        if (ids.isEmpty()) return
        val existing = ticketTable.slice(ticketTable.ticketId).select {
            (ticketTable.ticketId inList ids) and (ticketTable.isDeleted eq false)
        }.map { it[ticketTable.ticketId] }.toSet()
        require(existing == ids.toSet()) { "One or more TechnicalCaseIDs do not exist" }
    }

    private fun requireTechnicalCasesBelongToCustomer(ids: List<String>, customerId: String?) {
        if (ids.isEmpty()) return
        require(!customerId.isNullOrBlank()) { "A Work Order customer is required before linking Technical Cases" }
        val compatibleIds = ticketTable.slice(ticketTable.ticketId).select {
            (ticketTable.ticketId inList ids) and
                (ticketTable.customerId eq customerId) and
                (ticketTable.isDeleted eq false)
        }.map { it[ticketTable.ticketId] }.toSet()
        require(compatibleIds == ids.toSet()) {
            "Every Technical Case must belong to the same customer as the Work Order"
        }
    }

    private fun requireOpenTechnicalCases(ids: List<String>) {
        if (ids.isEmpty()) return
        val openStatuses = settingsTable.slice(settingsTable.settingsValue).select {
            (settingsTable.settingsKey eq "TechnicalCaseStatus") and
                (settingsTable.systemCategory eq "OPEN") and
                (settingsTable.isDeleted eq false)
        }.mapNotNull { it[settingsTable.settingsValue]?.trim() }
        require(openStatuses.isNotEmpty()) { "No OPEN Technical Case status is configured" }
        val openIds = ticketTable.slice(ticketTable.ticketId).select {
            (ticketTable.ticketId inList ids) and
                (ticketTable.status inList openStatuses) and
                (ticketTable.isDeleted eq false)
        }.map { it[ticketTable.ticketId] }.toSet()
        require(openIds == ids.toSet()) { "Only OPEN Technical Cases can be added to a Work Order" }
    }

    private fun replaceTechnicalCaseLinks(fieldReportId: String, ids: List<String>, now: String) {
        requireExistingTechnicalCases(ids)
        fieldReportTechnicalCasesTable.update({
            (fieldReportTechnicalCasesTable.fieldReportId eq fieldReportId) and
                (fieldReportTechnicalCasesTable.isDeleted eq false)
        }) {
            it[isDeleted] = true
            it[lastModified] = now
        }
        ids.forEach { technicalCaseId ->
            val existing = fieldReportTechnicalCasesTable.select {
                (fieldReportTechnicalCasesTable.fieldReportId eq fieldReportId) and
                    (fieldReportTechnicalCasesTable.technicalCaseId eq technicalCaseId)
            }.singleOrNull()
            if (existing == null) {
                fieldReportTechnicalCasesTable.insert {
                    it[fieldReportTechnicalCasesTable.id] = UUID.randomUUID().toString()
                    it[fieldReportTechnicalCasesTable.fieldReportId] = fieldReportId
                    it[fieldReportTechnicalCasesTable.technicalCaseId] = technicalCaseId
                    it[dateCreated] = now
                    it[lastModified] = now
                    it[isDeleted] = false
                }
            } else {
                fieldReportTechnicalCasesTable.update({ fieldReportTechnicalCasesTable.id eq existing[fieldReportTechnicalCasesTable.id] }) {
                    it[isDeleted] = false
                    it[lastModified] = now
                }
            }
        }
    }

    private fun replaceTechnicianAssignments(fieldReportId: String, values: List<FieldReportTechnicianWriteDto>, now: String) {
        val normalized = values.map { FieldReportTechnicianWriteDto(it.UserID.trim(), it.Role.trim().uppercase()) }
        require(normalized.none { it.UserID.isBlank() }) { "Technician UserID is required" }
        require(normalized.map { it.UserID }.distinct().size == normalized.size) { "A technician can only be assigned once" }
        require(normalized.all { it.Role in setOf("LEAD", "ASSISTANT") }) { "Technician Role must be LEAD or ASSISTANT" }
        require(normalized.count { it.Role == "LEAD" } <= 1) { "Only one LEAD technician is allowed" }
        val userIds = normalized.map { it.UserID }
        if (userIds.isNotEmpty()) {
            val existingUsers = userTable.slice(userTable.userId).select {
                (userTable.userId inList userIds) and (userTable.isDeleted eq false)
            }.map { it[userTable.userId] }.toSet()
            require(existingUsers == userIds.toSet()) { "One or more technicians do not belong to this tenant" }
        }
        fieldReportTechniciansTable.update({
            (fieldReportTechniciansTable.fieldReportId eq fieldReportId) and
                (fieldReportTechniciansTable.isDeleted eq false)
        }) { it[isDeleted] = true; it[lastModified] = now }
        normalized.forEach { assignment ->
            val existing = fieldReportTechniciansTable.select {
                (fieldReportTechniciansTable.fieldReportId eq fieldReportId) and
                    (fieldReportTechniciansTable.userId eq assignment.UserID)
            }.singleOrNull()
            if (existing == null) fieldReportTechniciansTable.insert {
                it[id] = UUID.randomUUID().toString(); it[fieldReportTechniciansTable.fieldReportId] = fieldReportId
                it[userId] = assignment.UserID; it[role] = assignment.Role; it[assignedAt] = now
                it[lastModified] = now; it[isDeleted] = false
            } else fieldReportTechniciansTable.update({ fieldReportTechniciansTable.id eq existing[fieldReportTechniciansTable.id] }) {
                it[role] = assignment.Role; it[lastModified] = now; it[isDeleted] = false
            }
        }
    }

    private fun technicianDetails(fieldReportId: String): List<FieldReportTechnicianDto> =
        fieldReportTechniciansTable.innerJoin(userTable).select {
            (fieldReportTechniciansTable.fieldReportId eq fieldReportId) and
                (fieldReportTechniciansTable.isDeleted eq false) and (userTable.isDeleted eq false)
        }.map {
            FieldReportTechnicianDto(it[userTable.userId], it[userTable.name], it[fieldReportTechniciansTable.role])
        }.sortedBy { if (it.Role == "LEAD") 0 else 1 }

    private fun validatedSchedule(start: String?, end: String?): Pair<String?, String?> {
        if (start.isNullOrBlank() && end.isNullOrBlank()) return null to null
        require(!start.isNullOrBlank() && !end.isNullOrBlank()) { "ScheduledStart and ScheduledEnd are both required" }
        val normalizedStart = DateUtils.normalizeDateTimeStorage(start)
            ?: throw IllegalArgumentException("ScheduledStart must use yyyyMMddHHmmss or dd/MM/yyyy HH:mm:ss")
        val normalizedEnd = DateUtils.normalizeDateTimeStorage(end)
            ?: throw IllegalArgumentException("ScheduledEnd must use yyyyMMddHHmmss or dd/MM/yyyy HH:mm:ss")
        require(normalizedEnd > normalizedStart) { "ScheduledEnd must be after ScheduledStart" }
        return normalizedStart to normalizedEnd
    }

    private fun activeTechnicalCaseIds(fieldReportId: String) = fieldReportTechnicalCasesTable
        .slice(fieldReportTechnicalCasesTable.technicalCaseId)
        .select {
            (fieldReportTechnicalCasesTable.fieldReportId eq fieldReportId) and
                (fieldReportTechnicalCasesTable.isDeleted eq false)
        }.map { it[fieldReportTechnicalCasesTable.technicalCaseId] }

    private fun technicalCaseDetails(fieldReportId: String): List<FieldReportTechnicalCaseDto> =
        fieldReportTechnicalCasesTable.innerJoin(ticketTable).select {
            (fieldReportTechnicalCasesTable.fieldReportId eq fieldReportId) and
                (fieldReportTechnicalCasesTable.isDeleted eq false) and
                (ticketTable.isDeleted eq false)
        }.map { row ->
            val currentStatus = row[ticketTable.status]
            val customerId = row[ticketTable.customerId]
            FieldReportTechnicalCaseDto(
                TechnicalCaseID = row[ticketTable.ticketId],
                TicketNumber = row[ticketTable.ticketNumber],
                Title = row[ticketTable.title],
                CustomerID = customerId,
                CustomerName = customerId?.let(::customerName),
                Status = currentStatus,
                SystemCategory = currentStatus?.let { statusSystemCategory("TechnicalCaseStatus", it) },
                ResolutionStatus = row[fieldReportTechnicalCasesTable.resolutionStatus],
                ResolutionNotes = row[fieldReportTechnicalCasesTable.resolutionNotes]
            )
        }

    private fun statusSystemCategory(settingsKey: String, status: String): String? = settingsTable
        .slice(settingsTable.systemCategory)
        .select {
            (settingsTable.settingsKey eq settingsKey) and
                (settingsTable.settingsValue.lowerCase() eq status.trim().lowercase()) and
                (settingsTable.isDeleted eq false)
        }.singleOrNull()?.get(settingsTable.systemCategory)

    private fun requireConfiguredStatus(settingsKey: String, status: String, requiredCategory: String? = null) {
        require(status.isNotBlank()) { "$settingsKey status is required" }
        val category = statusSystemCategory(settingsKey, status)
            ?: throw IllegalArgumentException("Status '$status' is not configured as $settingsKey")
        require(requiredCategory == null || category == requiredCategory) {
            "Status '$status' must have SystemCategory $requiredCategory"
        }
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
                Manufacturer = sourceEquipment?.get(equipmentTable.manufacturer),
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
                val quantity = inventoryId?.let { sourceId ->
                    inventoryTable.select { (inventoryTable.inventoryId eq sourceId) and (inventoryTable.isDeleted eq false) }
                        .singleOrNull()?.get(inventoryTable.quantity)
                }
                FieldReportInventoryDetailDto(inventory[fieldReportInventoryTable.fieldReportInventoryId], inventoryId, inventoryName, quantity)
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
            ScheduledStart = report[fieldReportsTable.scheduledStart],
            ScheduledEnd = report[fieldReportsTable.scheduledEnd],
            ReportStatus = report[fieldReportsTable.reportStatus],
            Status = report[fieldReportsTable.reportStatus],
            Priority = report[fieldReportsTable.priority],
            Type = report[fieldReportsTable.workOrderType],
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
            SpecialTools = tools,
            TechnicalCases = technicalCaseDetails(id),
            Technicians = technicianDetails(id)
        )
    }

    private fun customerName(id: String) = customerTable.select { customerTable.customerId eq id }.singleOrNull()?.get(customerTable.name)
    private fun contractName(id: String) = contractsTable.select { contractsTable.contractId eq id }.singleOrNull()?.get(contractsTable.title)
    private fun ticketNumber(id: String) = ticketTable.select { ticketTable.ticketId eq id }.singleOrNull()?.get(ticketTable.ticketNumber)
    private fun userName(id: String) = userTable.select { userTable.userId eq id }.singleOrNull()?.get(userTable.name)
}

internal fun completionDecisionsCoverLinkedCases(
    linkedIds: List<String>,
    updates: List<TechnicalCaseCompletionUpdate>
): Boolean {
    val updateIds = updates.map { it.TechnicalCaseID }
    return updateIds.size == updateIds.distinct().size && updateIds.toSet() == linkedIds.toSet()
}
