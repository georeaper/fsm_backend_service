package com.example.feature.onboarding.repository

import com.example.loadDotenv
import com.example.models.api.CategoryAsset
import com.example.models.databaseModels.contractsTable
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import com.example.models.databaseModels.fieldReportsTable
import com.example.models.databaseModels.maintenancesTable
import com.example.models.databaseModels.manufacturerTable
import com.example.models.databaseModels.modelAssetTable
import com.example.models.databaseModels.tasksTable
import com.example.models.databaseModels.ticketTable
import com.example.models.databaseModels.toolsTable
import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class PopulateDemoRepositoryImpl: PopulateDemoRepository {

    override fun populateDemoData(databaseName: String) {
        // Implement the logic to populate demo data for the specified database.
        // This could involve inserting sample records into various tables.
        // For example:
        // 1. Connect to the specified database.
        // 2. Insert sample data into relevant tables.
        // 3. Handle any exceptions or errors that may occur during the process.


        // Load environment variables from .env file

        val dotenv = loadDotenv()
        // Connect to the central database using the loaded environment variables

        val tenantDB = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${databaseName}"
        val db = Database.connect(
            url = tenantDB,
            driver = "org.postgresql.Driver",
            user = dotenv["DB_USER"],
            password = dotenv["DB_PASSWORD"]
        )

        println("Populating demo data for database: $databaseName")
        generateDemoData(db)
    }
    private fun generateDemoData(db: Database) {
        // Implement the logic to generate demo data for the specified database.
        // This could involve creating sample records in various tables.
        // For example:
        // 1. Connect to the specified database.
        // 2. Generate sample data for relevant tables.
        // 3. Handle any exceptions or errors that may occur during the process.

        println("Generating demo data for database: $db")

        try{
            transaction(db) {
                // Create tables if they don't exist


                    val now = Instant.now().toString()

                    // Users
                    val userIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        userTable.insert {
                            it[userTable.userId] = id
                            it[userTable.name] = "Demo User $i"
                            it[userTable.email] = "user${i}@example.com"
                            it[userTable.phone] = "+100000000${i}"
                            it[userTable.reportPrefix] = "RPT$i"
                            it[userTable.technicalCasePrefix] = "TC$i"
                            it[userTable.lastModified] = now
                            it[userTable.dateCreated] = now
                            it[userTable.version] = "1"
                        }
                        id
                    }

                    // Customers
                    val customerIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        customerTable.insert {
                            it[customerTable.customerId] = id
                            it[customerTable.name] = "Demo Customer $i"
                            it[customerTable.phone] = "+200000000${i}"
                            it[customerTable.email] = "customer${i}@example.com"
                            it[customerTable.address] = "${i} Demo Street"
                            it[customerTable.city] = "DemoCity$i"
                            it[customerTable.customerStatus] = true
                            it[customerTable.lastModified] = now
                            it[customerTable.dateCreated] = now
                            it[customerTable.version] = "1"
                        }
                        id
                    }

                    // Manufacturers
                    val manufacturerIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        manufacturerTable.insert {
                            it[manufacturerTable.manufacturerId] = id
                            it[manufacturerTable.name] = "Acme Manufacturer $i"
                            it[manufacturerTable.style] = "style-$i"
                            it[manufacturerTable.lastModified] = now
                            it[manufacturerTable.dateCreated] = now
                            it[manufacturerTable.version] = "1"
                        }
                        id
                    }

                    // Models (model_asset)
                    val modelIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        modelAssetTable.insert {
                            it[modelAssetTable.modelId] = id
                            it[modelAssetTable.name] = "Model-$i"
                            it[modelAssetTable.style] = "m-style-$i"
                            it[modelAssetTable.lastModified] = now
                            it[modelAssetTable.dateCreated] = now
                            it[modelAssetTable.version] = "1"
                        }
                        id
                    }

                    // Maintenances
                    val maintenanceIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        maintenancesTable.insert {
                            it[maintenancesTable.maintenanceId] = id
                            it[maintenancesTable.name] = "Maintenance Task $i"
                            it[maintenancesTable.description] = "Periodic maintenance $i"
                            it[maintenancesTable.lastModified] = now
                            it[maintenancesTable.dateCreated] = now
                            it[maintenancesTable.version] = "1"
                        }
                        id
                    }

                    // Equipment
                    val equipmentIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        equipmentTable.insert {
                            it[equipmentTable.equipmentId] = id
                            it[equipmentTable.name] = "Demo Equipment $i"
                            it[equipmentTable.serialNumber] = "SN-1000${i}"
                            it[equipmentTable.model] = modelIds[(i - 1) % modelIds.size]
                            it[equipmentTable.manufacturer] = "Manufacturer-${i}"
                            it[equipmentTable.notes] = "Installed as demo"
                            it[equipmentTable.equipmentVersion] = "v1.0"
                            it[equipmentTable.equipmentCategory] = "General"
                            it[equipmentTable.warranty] = "12 months"
                            it[equipmentTable.equipmentStatus] = true
                            it[equipmentTable.installationDate] = now
                            it[equipmentTable.lastModified] = now
                            it[equipmentTable.dateCreated] = now
                            it[equipmentTable.version] = "1"
                            it[equipmentTable.customerId] = customerIds[(i - 1) % customerIds.size]
                        }
                        id
                    }

                    // Contracts
                    val contractIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        contractsTable.insert {
                            it[contractsTable.contractId] = id
                            it[contractsTable.title] = "Service Contract $i"
                            it[contractsTable.dateStart] = now
                            it[contractsTable.dateEnd] = null
                            it[contractsTable.value] = 1000.0 * i
                            it[contractsTable.notes] = "Demo contract for customer"
                            it[contractsTable.contractType] = "Standard"
                            it[contractsTable.contractStatus] = true
                            it[contractsTable.contactName] = "Contact $i"
                            it[contractsTable.lastModified] = now
                            it[contractsTable.dateCreated] = now
                            it[contractsTable.version] = "1"
                            it[contractsTable.customerId] = customerIds[(i - 1) % customerIds.size]
                        }
                        id
                    }

                    // Tickets
                    val ticketIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        ticketTable.insert {
                            it[ticketTable.ticketId] = id
                            it[ticketTable.title] = "Demo Ticket $i"
                            it[ticketTable.ticketNumber] = "TCK-${1000 + i}"
                            it[ticketTable.description] = "Issue reported #$i"
                            it[ticketTable.notes] = "Follow up"
                            it[ticketTable.urgency] = if (i == 1) "High" else "Normal"
                            it[ticketTable.active] = true
                            it[ticketTable.dateStart] = now
                            it[ticketTable.lastModified] = now
                            it[ticketTable.dateCreated] = now
                            it[ticketTable.version] = "1"
                            it[ticketTable.userId] = userIds[(i - 1) % userIds.size]
                            it[ticketTable.customerId] = customerIds[(i - 1) % customerIds.size]
                            it[ticketTable.equipmentId] = equipmentIds[(i - 1) % equipmentIds.size]
                        }
                        id
                    }

                    // Tasks
                    val taskIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        tasksTable.insert {
                            it[tasksTable.taskId] = id
                            it[tasksTable.title] = "Task for ticket ${i}"
                            it[tasksTable.description] = "Complete follow-up step $i"
                            it[tasksTable.status] = "OPEN"
                            it[tasksTable.priority] = if (i == 1) "HIGH" else "MEDIUM"
                            it[tasksTable.dateStart] = now
                            it[tasksTable.dateCreated] = now
                            it[tasksTable.lastModified] = now
                            it[tasksTable.ticketId] = ticketIds[(i - 1) % ticketIds.size]
                            it[tasksTable.userId] = userIds[(i - 1) % userIds.size]
                        }
                        id
                    }

                    // Tools
                    val toolIds = (1..3).map { i ->
                        val id = UUID.randomUUID().toString()
                        toolsTable.insert {
                            it[toolsTable.toolsId] = id
                            it[toolsTable.title] = "Multimeter $i"
                            it[toolsTable.description] = "Calibration required"
                            it[toolsTable.model] = "TM-${i}"
                            it[toolsTable.manufacturer] = "ToolCorp"
                            it[toolsTable.serialNumber] = "TL-${i}SER"
                            it[toolsTable.calibrationDate] = now
                            it[toolsTable.lastModified] = now
                            it[toolsTable.dateCreated] = now
                            it[toolsTable.version] = "1"
                        }
                        id
                    }

                    // Field Reports (reference a contract, customer, user, ticket)
                    (1..3).forEach { i ->
                        val id = UUID.randomUUID().toString()
                        fieldReportsTable.insert {
                            it[fieldReportsTable.fieldReportId] = id
                            it[fieldReportsTable.reportNumber] = "FR-${100 + i}"
                            it[fieldReportsTable.title] = "Field Report $i"
                            it[fieldReportsTable.description] = "Inspection performed"
                            it[fieldReportsTable.startDate] = now
                            it[fieldReportsTable.endDate] = now
                            it[fieldReportsTable.department] = "Dept-${i}"
                            it[fieldReportsTable.clientName] = "Client ${i}"
                            it[fieldReportsTable.reportStatus] = "COMPLETED"
                            it[fieldReportsTable.value] = 250.0 * i
                            it[fieldReportsTable.lastModified] = now
                            it[fieldReportsTable.dateCreated] = now
                            it[fieldReportsTable.version] = "1"
                            it[fieldReportsTable.customerID] = customerIds[(i - 1) % customerIds.size]
                            it[fieldReportsTable.contractID] = contractIds[(i - 1) % contractIds.size]
                            it[fieldReportsTable.userID] = userIds[(i - 1) % userIds.size]
                            it[fieldReportsTable.caseID] = ticketIds[(i - 1) % ticketIds.size]
                        }
                    }
                println("✅ Demo data created: users=${userIds.size}, customers=${customerIds.size}, manufacturers=${manufacturerIds.size}, models=${modelIds.size}, maintenances=${maintenanceIds.size}, equipment=${equipmentIds.size}, contracts=${contractIds.size}, tickets=${ticketIds.size}, tasks=${taskIds.size}, tools=${toolIds.size}, fieldReports=3")
            }
        } catch (e: Exception) {
            println("Error creating tables: ${e.message}")
            throw e
        }






        }



}