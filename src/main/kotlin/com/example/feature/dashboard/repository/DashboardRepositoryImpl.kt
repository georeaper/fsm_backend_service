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
        ticketTable.leftJoin(customerTable).slice(ticketTable.ticketId, ticketTable.ticketNumber, ticketTable.title, ticketTable.active, ticketTable.status, ticketTable.caseType, ticketTable.urgency, ticketTable.dateCreated, ticketTable.dateEnd, ticketTable.customerId, customerTable.name)
            .select { ticketTable.isDeleted eq false }.map { TechnicalCaseRow(it[ticketTable.ticketId], it[ticketTable.ticketNumber], it[ticketTable.title], it[ticketTable.active], it[ticketTable.status], it[ticketTable.caseType], it[ticketTable.urgency], it[ticketTable.dateCreated], it[ticketTable.dateEnd], it[ticketTable.customerId], it[customerTable.name]) }
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
