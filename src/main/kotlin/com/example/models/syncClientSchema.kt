package com.example.models

import com.example.models.databaseModels.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun syncClientSchema() {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            userTable,
            customerTable,
            equipmentTable,
            ticketHistoryTable,
            categoryAssetTable,
            checkFormsTable,
            contractEquipmentsTable,
            contractsTable,
            fieldReportCheckformTable,
            fieldReportToolsTable,
            fieldReportsTable,
            fieldReportInventoryTable,
            fieldReportEquipmentTable,
            inventoryTable,
            manufacturerTable,
            maintenancesTable,
            modelAssetTable,
            settingsTable,
            tasksTable,
            ticketTable,
            toolsTable
        )
    }
}