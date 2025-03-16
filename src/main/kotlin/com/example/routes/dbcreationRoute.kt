package com.example.routes

import com.example.models.databaseModels.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.dbCreation(){
    route("/sign-up"){
        get("/init-db"){
            try{
                val db = Database.connect(
                    url = "jdbc:postgresql://localhost:5432/CompanyA",
                    driver = "org.postgresql.Driver",
                    user = "postgres",
                    password = "Giorgos13"
                )

                transaction(db) {
                    SchemaUtils.createMissingTablesAndColumns(
                        customerTable,
                        categoryAssetTable,
                        checkFormsTable,
                        contractEquipmentsTable,
                        contractsTable,
                        departmentTable,
                        equipmentTable,
                        fieldReportEquipmentTable,
                        fieldReportCheckformTable,
                        fieldReportInventoryTable,
                        fieldReportToolsTable,
                        fieldReportsTable,
                        inventoryTable,
                        maintenancesTable,
                        manufacturerTable,
                        modelAssetTable,
                        settingsTable,
                        tasksTable,
                        ticketTable,
                        toolsTable,
                        userTable                        )
                }

            }catch (e: Exception){
                println("Error: ${e.message}")  // Log error message
            }
        }
    }

}