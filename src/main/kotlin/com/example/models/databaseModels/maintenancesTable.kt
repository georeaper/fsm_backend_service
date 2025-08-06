package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object maintenancesTable : Table("maintenances") {
    val maintenanceId = varchar("MaintenanceId", 36).default(UUID.randomUUID().toString())
    val remoteID = integer("d").nullable()
    val name = varchar("Name", 255).nullable()
    val description = text("Description").nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()

    override val primaryKey = PrimaryKey(maintenanceId)
}