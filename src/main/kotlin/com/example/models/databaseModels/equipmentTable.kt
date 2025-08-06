package com.example.models.databaseModels
import com.example.models.databaseModels.equipmentTable.default
import com.example.models.databaseModels.equipmentTable.nullable
import com.example.models.databaseModels.equipmentTable.references
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object equipmentTable : Table("equipment") {
    val equipmentId = varchar("EquipmentId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val serialNumber = varchar("SerialNumber", 100).nullable()
    val model = varchar("Model", 100).nullable()
    val manufacturer = varchar("Manufacturer", 100).nullable()
    val notes = text("Notes").nullable()
    val description = text("Description").nullable()
    val equipmentVersion = varchar("EquipmentVersion", 50).nullable()
    val equipmentCategory = varchar("EquipmentCategory", 100).nullable()
    val warranty = varchar("Warranty", 100).nullable()
    val equipmentStatus = bool("EquipmentStatus").nullable()
    val installationDate = varchar("InstallationDate", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    val customerId = varchar("CustomerId",36).references(customerTable.customerId).nullable()
    override val primaryKey= PrimaryKey(equipmentId)
}

