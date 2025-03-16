package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object fieldReportToolsTable : Table("fieldReportToolsTable") {
    val fieldReportToolsId = varchar("FieldReportToolsId", 36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val fieldReportId = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId).nullable()
    val toolsId = varchar("ToolsId", 36).nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()

    override val primaryKey = PrimaryKey(fieldReportToolsId)
}