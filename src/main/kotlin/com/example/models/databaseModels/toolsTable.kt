package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object toolsTable : Table("toolsTable") {
    val toolsId = varchar("ToolsId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val title = varchar("Title", 255).nullable()
    val description = text("Description").nullable()
    val model = varchar("Model", 100).nullable()
    val manufacturer = varchar("Manufacturer", 100).nullable()
    val serialNumber = varchar("SerialNumber", 100).nullable()
    val calibrationDate = varchar("CalibrationDate", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(toolsId)

}