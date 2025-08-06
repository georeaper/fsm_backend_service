package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object fieldReportInventoryTable : Table("fieldreport_inventory") {
    val fieldReportInventoryId = varchar("FieldReportInventoryId", 36).default(UUID.randomUUID().toString())
    val remoteID = integer("RemoteId").nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()
    val fieldReportID = varchar("FieldReportId", 36).nullable()
    val inventoryID = varchar("InventoryId", 36).references(inventoryTable.inventoryId).nullable()


    override val primaryKey = PrimaryKey(fieldReportInventoryId)
}