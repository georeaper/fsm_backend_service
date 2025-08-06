package com.example.services

import com.example.models.api.*
import com.example.models.databaseModels.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.Base64

import org.jetbrains.exposed.sql.*


// Define the entity handler map
val entityHandlers: Map<String, suspend (Connection, String) -> List<SynCable>> = mapOf(
    "Customer" to { connection, _ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        // Use the connection directly without creating a new Database.connect
        fetchAsList(db, customerTable.selectAll()) {
            Customers(
                it[customerTable.customerId],
                it[customerTable.remoteId],
                it[customerTable.name],
                it[customerTable.phone],
                it[customerTable.email],
                it[customerTable.address],
                it[customerTable.zipCode],
                it[customerTable.city],
                it[customerTable.notes],
                it[customerTable.description],
                it[customerTable.customerStatus],
                it[customerTable.lastModified],
                it[customerTable.dateCreated],
                it[customerTable.version],

            )
        }
    },
    "Equipment" to { connection, _ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        // Use the connection directly without creating a new Database.connect
        fetchAsList(db, equipmentTable.selectAll()) {
            Equipments(
                it[equipmentTable.equipmentId],
                it[equipmentTable.remoteId],
                it[equipmentTable.name],
                it[equipmentTable.serialNumber],
                it[equipmentTable.model],
                it[equipmentTable.manufacturer],
                it[equipmentTable.notes],
                it[equipmentTable.description],
                it[equipmentTable.equipmentVersion],
                it[equipmentTable.equipmentCategory],
                it[equipmentTable.warranty],
                it[equipmentTable.equipmentStatus],
                it[equipmentTable.installationDate],
                it[equipmentTable.lastModified],
                it[equipmentTable.dateCreated],
                it[equipmentTable.version],
                it[equipmentTable.customerId]
            )
        }
    },
    "Users" to { connection, _ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        // Use the connection directly without creating a new Database.connect
        fetchAsList(db, userTable.selectAll()) {
            Users(
                it[userTable.userId],
                it[userTable.remoteId],
                it[userTable.name],
                it[userTable.description],
                it[userTable.email],
                it[userTable.phone],
                it[userTable.signature],
                it[userTable.reportPrefix],
                it[userTable.technicalCasePrefix],
                it[userTable.lastReportNumber],
                it[userTable.lastTCNumber],
                it[userTable.lastModified],
                it[userTable.dateCreated],
                it[userTable.version]
            )
        }
    },
    "Contracts" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,contractsTable.selectAll()){
            Contracts(
                it[contractsTable.contractId],
                it[contractsTable.remoteId],
                it[contractsTable.title],
                it[contractsTable.dateStart],
                it[contractsTable.dateEnd],
                it[contractsTable.value],
                it[contractsTable.notes],
                it[contractsTable.description],
                it[contractsTable.contractType],
                it[contractsTable.contractStatus],
                it[contractsTable.contactName],
                it[contractsTable.lastModified],
                it[contractsTable.dateCreated],
                it[contractsTable.version],
                it[contractsTable.customerId]
            )
        }


    },
    "CategoryAsset" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,categoryAssetTable.selectAll()){
            CategoryAsset(
                it[categoryAssetTable.categoryAssetId],
                it[categoryAssetTable.remoteId],
                it[categoryAssetTable.name],
                it[categoryAssetTable.style],
                it[categoryAssetTable.lastModified],
                it[categoryAssetTable.dateCreated],
                it[categoryAssetTable.version]
            )
        }
    },
    "CheckForms" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,checkFormsTable.selectAll()){
            CheckForms(
                it[checkFormsTable.checkFormId],
                it[checkFormsTable.remoteId],
                it[checkFormsTable.maintenancesId],
                it[checkFormsTable.description],
                it[checkFormsTable.valueExpected],
                it[checkFormsTable.valueType],
                it[checkFormsTable.lastModified],
                it[checkFormsTable.dateCreated],
                it[checkFormsTable.version]
            )
        }
    },
    "ContractEquipments" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,contractEquipmentsTable.selectAll()){
            ContractEquipments(
                it[contractEquipmentsTable.contractEquipmentId],
                it[contractEquipmentsTable.remoteId],
                it[contractEquipmentsTable.value],
                it[contractEquipmentsTable.visits],
                it[contractEquipmentsTable.contractId],
                it[contractEquipmentsTable.equipmentId],
                it[contractEquipmentsTable.lastModified],
                it[contractEquipmentsTable.dateCreated],
                it[contractEquipmentsTable.version]
            )
        }
    },
    "Departments" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,departmentTable.selectAll()){
            Departments(
                it[departmentTable.departmentId],
                it[departmentTable.remoteId],
                it[departmentTable.name],
                it[departmentTable.phone],
                it[departmentTable.email],
                it[departmentTable.contactPerson],
                it[departmentTable.notes],
                it[departmentTable.description],
                it[departmentTable.departmentStatus],
                it[departmentTable.lastModified],
                it[departmentTable.dateCreated],
                it[departmentTable.version],
                it[departmentTable.customerId]
            )
        }
    },
    "FieldReportEquipment" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,fieldReportEquipmentTable.selectAll()){
            FieldReportEquipment(
                it[fieldReportEquipmentTable.fieldReportEquipmentID],
                it[fieldReportEquipmentTable.remoteID],
                it[fieldReportEquipmentTable.completedStatus],
                it[fieldReportEquipmentTable.lastModified],
                it[fieldReportEquipmentTable.dateCreated],
                it[fieldReportEquipmentTable.version],
                it[fieldReportEquipmentTable.fieldReportID],
                it[fieldReportEquipmentTable.equipmentID],
                it[fieldReportEquipmentTable.maintenanceID]
            )
        }
    },
    "FieldReportInventory" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,fieldReportInventoryTable.selectAll()){
            FieldReportInventory(
                it[fieldReportInventoryTable.fieldReportInventoryId],
                it[fieldReportInventoryTable.remoteID],
                it[fieldReportInventoryTable.lastModified],
                it[fieldReportInventoryTable.dateCreated],
                it[fieldReportInventoryTable.version],
                it[fieldReportInventoryTable.fieldReportID],
                it[fieldReportInventoryTable.inventoryID]
            )
        }
    },
    "FieldReports" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,fieldReportsTable.selectAll()){
            FieldReports(
                it[fieldReportsTable.fieldReportId],
                it[fieldReportsTable.remoteID],
                it[fieldReportsTable.reportNumber],
                it[fieldReportsTable.description],
                it[fieldReportsTable.startDate],
                it[fieldReportsTable.endDate],
                it[fieldReportsTable.title],
                it[fieldReportsTable.department],
                it[fieldReportsTable.clientName],
                it[fieldReportsTable.reportStatus],
                it[fieldReportsTable.clientSignature],
                it[fieldReportsTable.value],
                it[fieldReportsTable.lastModified],
                it[fieldReportsTable.dateCreated],
                it[fieldReportsTable.version],
                it[fieldReportsTable.customerID],
                it[fieldReportsTable.contractID],
                it[fieldReportsTable.userID],
                it[fieldReportsTable.caseID],

            )
        }
    },
    "FieldReportTools" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")
        fetchAsList(db ,fieldReportToolsTable.selectAll()){
            FieldReportTools(
                it[fieldReportToolsTable.fieldReportToolsId],
                it[fieldReportToolsTable.remoteId],
                it[fieldReportToolsTable.fieldReportId],
                it[fieldReportToolsTable.toolsId],
                it[fieldReportToolsTable.lastModified],
                it[fieldReportToolsTable.dateCreated],
                it[fieldReportToolsTable.version]
            )
        }
    },
    "Inventory" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = inventoryTable

        fetchAsList(db ,table.selectAll()){
            Inventory(
                it[table.inventoryId],
                it[table.remoteID],
                it[table.title],
                it[table.description],
                it[table.quantity],
                it[table.value],
                it[table.type],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]
            )
        }
    },
    "Maintenances" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = maintenancesTable

        fetchAsList(db ,table.selectAll()){
            Maintenances(
                it[table.maintenanceId],
                it[table.remoteID],
                it[table.name],
                it[table.description],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]
            )
        }
    },
    "Manufacturer" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = manufacturerTable

        fetchAsList(db ,table.selectAll()){
            Manufacturer(
                it[table.manufacturerId],
                it[table.remoteId],
                it[table.name],
                it[table.style],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]
            )
        }
    },
    "ModelAsset" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = modelAssetTable

        fetchAsList(db ,table.selectAll()){
            ModelAsset(
                it[table.modelId],
                it[table.remoteId],
                it[table.name],
                it[table.style],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]
            )
        }
    },
    "Settings" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = settingsTable

        fetchAsList(db ,table.selectAll()){
            Settings(
                it[table.settingsId],
                it[table.remoteId],
                it[table.settingsKey],
                it[table.settingsValue],
                it[table.settingsStyle],
                it[table.settingsDescription],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]
            )
        }
    },
    "Tasks" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = tasksTable

        fetchAsList(db ,table.selectAll()){
            Tasks(
                it[table.taskId],
                //it[table.remoteID],
                it[table.title],
                it[table.description],
                it[table.status],
                it[table.priority],
                it[table.dateStart],
                it[table.dateDue],
                it[table.dateCompleted],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.ticketId],
                it[table.userId]
            )
        }
    },
    "TicketHistory" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = ticketHistoryTable

        fetchAsList(db ,table.selectAll()){
            TicketHistory(
                it[table.ticketHistoryId],
                it[table.remoteId],
                it[table.title],
                it[table.ticketNumber],
                it[table.description],
                it[table.notes],
                it[table.urgency],
                it[table.active],
                it[table.dateStart],
                it[table.dateEnd],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version],
                it[table.userId],
                it[table.customerId],
                it[table.equipmentId],
            )
        }
    },
    "Tickets" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = ticketTable

        fetchAsList(db ,table.selectAll()){
            Tickets(
                it[table.ticketId],
                it[table.remoteId],
                it[table.title],
                it[table.ticketNumber],
                it[table.description],
                it[table.notes],
                it[table.urgency],
                it[table.active],
                it[table.dateStart],
                it[table.dateEnd],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version],
                it[table.userId],
                it[table.customerId],
                it[table.equipmentId],
            )
        }
    },
    "Tools" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = toolsTable

        fetchAsList(db ,table.selectAll()){
            Tools(
                it[table.toolsId],
                it[table.remoteId],
                it[table.title],
                it[table.description],
                it[table.model],
                it[table.manufacturer],
                it[table.serialNumber],
                it[table.calibrationDate],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version],

            )
        }
    },
    "FieldReportCheckForm" to {connection ,_ ->
        val db = createDatabaseFromConnection(connection)
        println("$db")

        val table = fieldReportCheckformTable

        fetchAsList(db ,table.selectAll()){
            FieldReportCheckForm(
                it[table.fieldReportCheckFormId],
                it[table.remoteId],
                it[table.fieldReportEquipmentId],
                it[table.description],
                it[table.valueExpected],
                it[table.valueMeasured],
                it[table.result],
                it[table.lastModified],
                it[table.dateCreated],
                it[table.version]

                )
        }
    }
)

private fun createDatabaseFromConnection(connection: Connection): Database {
    // Extract the connection details (url, user, password)
    val url = connection.metaData.url
    val user = connection.metaData.userName
    val password = "Giorgos13"  // You may need to handle this depending on your use case

    // Create and return the Database object
    return Database.connect(url, driver = "org.postgresql.Driver", user = user, password = password)
}

// Generalized function to fetch data from the database based on the query and handler
suspend fun <T : SynCable> fetchAsList(db: Database, query: Query, mapper: (ResultRow) -> T): List<T> {
    return try {
        // Using the provided database object in the transaction block
        transaction(db) {
            query.map { mapper(it) }
        }
    } catch (e: Exception) {
        println("Error fetching data: ${e.message}")
        emptyList()
    }
}

// Helper function to convert ByteArray to Base64
fun ByteArray.toBase64(): String =
    Base64.getEncoder().encodeToString(this)
