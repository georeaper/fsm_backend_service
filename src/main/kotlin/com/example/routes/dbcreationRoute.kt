package com.example.routes

import com.example.models.databaseModels.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.loadDotenv

fun Route.dbCreation(){
    route("/sign-up"){
        get("/init-db"){          

        
            try{
                val dotenv = loadDotenv()
                val url = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/CompanyA"
                val db = Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = "postgres",
                    password = "Giorgos13"
                )

                transaction(db) {
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

            }catch (e: Exception){
                println("Error: ${e.message}")  // Log error message
            }
        }
    }

}