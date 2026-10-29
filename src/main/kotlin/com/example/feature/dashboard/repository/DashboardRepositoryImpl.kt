package com.example.feature.dashboard.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.models.databaseModels.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DashboardRepositoryImpl(private val dbProvider: DatabaseProvider) : DashboardRepository {
    override fun workOrders(ctx: RequestContext) = transaction(dbProvider.getDatabase(ctx.dbName)) {
        fieldReportsTable.leftJoin(customerTable).slice(fieldReportsTable.fieldReportId, fieldReportsTable.reportNumber, fieldReportsTable.reportStatus, fieldReportsTable.dateCreated, fieldReportsTable.endDate, fieldReportsTable.customerID, customerTable.name)
            .select { fieldReportsTable.isDeleted eq false }.map { WorkOrderRow(it[fieldReportsTable.fieldReportId], it[fieldReportsTable.reportNumber], it[fieldReportsTable.reportStatus], it[fieldReportsTable.dateCreated], it[fieldReportsTable.endDate], it[fieldReportsTable.customerID], it[customerTable.name]) }
    }
    override fun technicalCases(ctx: RequestContext) = transaction(dbProvider.getDatabase(ctx.dbName)) {
        ticketTable.leftJoin(customerTable).leftJoin(equipmentTable)
            .slice(
                ticketTable.ticketId, ticketTable.ticketNumber, ticketTable.title, ticketTable.active,
                ticketTable.status, ticketTable.caseType, ticketTable.urgency, ticketTable.dateCreated,
                ticketTable.dateEnd, ticketTable.customerId, customerTable.name, ticketTable.equipmentId,
                equipmentTable.model, equipmentTable.manufacturer
            )
            .select { ticketTable.isDeleted eq false }
            .map {
                TechnicalCaseRow(
                    it[ticketTable.ticketId], it[ticketTable.ticketNumber], it[ticketTable.title],
                    it[ticketTable.active], it[ticketTable.status], it[ticketTable.caseType],
                    it[ticketTable.urgency], it[ticketTable.dateCreated], it[ticketTable.dateEnd],
                    it[ticketTable.customerId], it[customerTable.name], it[ticketTable.equipmentId],
                    it[equipmentTable.model], it[equipmentTable.manufacturer]
                )
            }
    }
    override fun contracts(ctx: RequestContext) = transaction(dbProvider.getDatabase(ctx.dbName)) {
        val categories = systemCategories("ContractStatus")
        contractsTable.slice(contractsTable.contractStatus, contractsTable.dateCreated, contractsTable.dateEnd).select { contractsTable.isDeleted eq false }.map { ContractRow(canonical(it[contractsTable.contractStatus], categories), it[contractsTable.dateCreated], it[contractsTable.dateEnd]) }
    }
    override fun tasks(ctx: RequestContext) = transaction(dbProvider.getDatabase(ctx.dbName)) {
        val categories = systemCategories("TaskStatus")
        tasksTable.leftJoin(ticketTable).slice(tasksTable.taskId, tasksTable.title, tasksTable.status, tasksTable.priority, tasksTable.taskType, tasksTable.dateDue, tasksTable.ticketId, ticketTable.ticketNumber)
            .select { tasksTable.isDeleted eq false }.map { TaskRow(it[tasksTable.taskId], it[tasksTable.title], canonical(it[tasksTable.status], categories), it[tasksTable.priority], it[tasksTable.dateDue], it[tasksTable.ticketId], it[ticketTable.ticketNumber], it[tasksTable.taskType]) }
    }
    override fun completeTask(ctx: RequestContext, taskId: String, completedAt: String): Boolean? = transaction(dbProvider.getDatabase(ctx.dbName)) {
        val row = tasksTable.select { (tasksTable.taskId eq taskId) and (tasksTable.isDeleted eq false) }.singleOrNull() ?: return@transaction null
        val categories = systemCategories("TaskStatus")
        val current = canonical(row[tasksTable.status], categories).normalized()
        if (current in setOf("completed", "complete", "closed")) return@transaction true
        if (current in setOf("cancelled", "canceled")) return@transaction false
        val closedValue = systemValue("TaskStatus", "CLOSED") ?: "CLOSED"
        tasksTable.update({ (tasksTable.taskId eq taskId) and (tasksTable.isDeleted eq false) }) {
            it[status] = closedValue; it[dateCompleted] = completedAt; it[lastModified] = completedAt
        } > 0
    }

    override fun statusSettings(ctx: RequestContext, key: String) = transaction(dbProvider.getDatabase(ctx.dbName)) {
        settingsTable
            .slice(settingsTable.settingsId, settingsTable.settingsValue, settingsTable.systemCategory, settingsTable.settingsStyle, settingsTable.isDeleted)
            .select { settingsTable.settingsKey eq key }
            .mapNotNull { row ->
                val value = row[settingsTable.settingsValue]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                StatusSettingRow(row[settingsTable.settingsId], value, row[settingsTable.systemCategory], row[settingsTable.settingsStyle], row[settingsTable.isDeleted])
            }
    }

    override fun workOrderTypeCounts(ctx: RequestContext, year: Int): List<WorkOrderTypeCountRow> {
        require(year in 2000..2100) { "Invalid dashboard year" }
        return transaction(dbProvider.getDatabase(ctx.dbName)) {
            val sql = """
                SELECT
                    TRIM("Type") AS type_value,
                    CASE
                        WHEN "DateCreated" ~ '^[0-9]{8}' THEN
                            SUBSTRING("DateCreated" FROM 1 FOR 4) || '-' || SUBSTRING("DateCreated" FROM 5 FOR 2)
                        WHEN "DateCreated" ~ '^[0-9]{4}-[0-9]{2}' THEN
                            SUBSTRING("DateCreated" FROM 1 FOR 7)
                        WHEN "DateCreated" ~ '^[0-9]{2}/[0-9]{2}/[0-9]{4}' THEN
                            SUBSTRING("DateCreated" FROM 7 FOR 4) || '-' || SUBSTRING("DateCreated" FROM 4 FOR 2)
                        ELSE NULL
                    END AS period_value,
                    COUNT(*) AS report_count
                FROM fieldreports
                WHERE "IsDeleted" = FALSE
                  AND "Type" IS NOT NULL
                  AND TRIM("Type") <> ''
                  AND (
                      SUBSTRING("DateCreated" FROM 1 FOR 4) = '$year'
                      OR RIGHT("DateCreated", 4) = '$year'
                  )
                  AND "DateCreated" ~ '^([0-9]{8}|[0-9]{4}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4})'
                GROUP BY type_value, period_value
                ORDER BY period_value, type_value
            """.trimIndent()

            exec(sql) { result ->
                buildList {
                    while (result.next()) {
                        add(
                            WorkOrderTypeCountRow(
                                typeValue = result.getString("type_value"),
                                period = result.getString("period_value"),
                                count = result.getInt("report_count")
                            )
                        )
                    }
                }
            } ?: emptyList()
        }
    }

    override fun workloadAssignments(ctx: RequestContext): List<WorkloadAssignmentRow> =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            val technicalCases = ticketTable
                .slice(ticketTable.ticketId, ticketTable.userId, ticketTable.status, ticketTable.urgency, ticketTable.dateStart, ticketTable.dateEnd, ticketTable.ticketNumber, ticketTable.title)
                .select { ticketTable.isDeleted eq false }
                .map { WorkloadAssignmentRow(it[ticketTable.ticketId], AssignmentType.TECHNICAL_CASE, it[ticketTable.userId], it[ticketTable.status], it[ticketTable.urgency], it[ticketTable.dateStart], it[ticketTable.dateEnd], it[ticketTable.ticketNumber], it[ticketTable.title]) }
            val reportRows = fieldReportsTable
                .slice(fieldReportsTable.fieldReportId, fieldReportsTable.userID, fieldReportsTable.reportStatus, fieldReportsTable.priority, fieldReportsTable.scheduledStart, fieldReportsTable.scheduledEnd, fieldReportsTable.startDate, fieldReportsTable.endDate, fieldReportsTable.reportNumber, fieldReportsTable.title)
                .select { fieldReportsTable.isDeleted eq false }
                .toList()
            val assignments = fieldReportTechniciansTable.select { fieldReportTechniciansTable.isDeleted eq false }
                .groupBy { it[fieldReportTechniciansTable.fieldReportId] }
            val workOrders = reportRows.flatMap { report ->
                val reportId = report[fieldReportsTable.fieldReportId]
                val assignedUsers = assignments[reportId]?.map { it[fieldReportTechniciansTable.userId] }
                    ?: listOfNotNull(report[fieldReportsTable.userID])
                assignedUsers.map { assignedUserId ->
                    WorkloadAssignmentRow(
                        reportId, AssignmentType.WORK_ORDER, assignedUserId,
                        report[fieldReportsTable.reportStatus], report[fieldReportsTable.priority],
                        report[fieldReportsTable.scheduledStart] ?: report[fieldReportsTable.startDate],
                        report[fieldReportsTable.scheduledEnd] ?: report[fieldReportsTable.endDate],
                        report[fieldReportsTable.reportNumber], report[fieldReportsTable.title]
                    )
                }
            }
            val tasks = tasksTable
                .slice(tasksTable.taskId, tasksTable.userId, tasksTable.status, tasksTable.priority, tasksTable.dateStart, tasksTable.dateDue, tasksTable.title)
                .select { tasksTable.isDeleted eq false }
                .map { WorkloadAssignmentRow(it[tasksTable.taskId], AssignmentType.TASK, it[tasksTable.userId], it[tasksTable.status], it[tasksTable.priority], it[tasksTable.dateStart], it[tasksTable.dateDue], null, it[tasksTable.title]) }
            technicalCases + workOrders + tasks
        }

    override fun workloadTechnicians(ctx: RequestContext): List<WorkloadTechnicianRow> =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            userTable.slice(userTable.userId, userTable.name)
                .select { userTable.isDeleted eq false }
                .map { WorkloadTechnicianRow(it[userTable.userId], it[userTable.name]) }
        }

    private fun systemCategories(key: String): Map<String, String> = settingsTable
        .slice(settingsTable.settingsValue, settingsTable.systemCategory)
        // Deleted settings remain part of historical classification. They are hidden
        // from selection endpoints but their old records must keep their semantics.
        .select { settingsTable.settingsKey eq key }
        .mapNotNull { row ->
            val value = row[settingsTable.settingsValue]?.trim()?.lowercase() ?: return@mapNotNull null
            val category = row[settingsTable.systemCategory] ?: return@mapNotNull null
            value to category
        }.toMap()

    private fun canonical(value: String?, categories: Map<String, String>): String? =
        value?.let { categories[it.trim().lowercase()] ?: it }

    private fun systemValue(key: String, category: String): String? = settingsTable
        .slice(settingsTable.settingsValue)
        .select {
            (settingsTable.settingsKey eq key) and
                (settingsTable.systemCategory eq category) and
                (settingsTable.isDeleted eq false)
        }
        .orderBy(settingsTable.settingsId)
        .limit(1)
        .singleOrNull()
        ?.get(settingsTable.settingsValue)
}

private fun String?.normalized() = this?.trim()?.lowercase()?.replace("_", "")?.replace("-", "")?.replace(" ", "") ?: ""
