package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object fieldReportEquipmentTable : Table("fieldReportEquipmentTable") {
    val fieldReportEquipmentID= varchar("FieldReportEquipmentId", 36).default(UUID.randomUUID().toString())
    val remoteID = integer("RemoteId").nullable()
    val completedStatus = bool("CompletedStatus").nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()
    val fieldReportID = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId).nullable()
    val equipmentID = varchar("EquipmentId", 36).references(equipmentTable.equipmentId).nullable()
    val maintenanceID = varchar("MaintenanceId", 36).references(maintenancesTable.maintenanceId).nullable()

    override val primaryKey = PrimaryKey(fieldReportEquipmentID)
}