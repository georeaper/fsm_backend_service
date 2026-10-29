package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object fieldReportTechnicalCasesTable : Table("fieldreport_technical_cases") {
    val id = varchar("Id", 36).default(UUID.randomUUID().toString())
    val fieldReportId = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId)
    val technicalCaseId = varchar("TechnicalCaseId", 36).references(ticketTable.ticketId)
    val resolutionStatus = varchar("ResolutionStatus", 50).nullable()
    val resolutionNotes = text("ResolutionNotes").nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val isDeleted = bool("IsDeleted").default(false)

    override val primaryKey = PrimaryKey(id)
}
