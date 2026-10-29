package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object fieldReportTechniciansTable : Table("fieldreport_technicians") {
    val id = varchar("Id", 36).default(UUID.randomUUID().toString())
    val fieldReportId = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId)
    val userId = varchar("UserId", 36).references(userTable.userId)
    val role = varchar("Role", 20).default("ASSISTANT")
    val assignedAt = varchar("AssignedAt", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val isDeleted = bool("IsDeleted").default(false)

    override val primaryKey = PrimaryKey(id)
}
