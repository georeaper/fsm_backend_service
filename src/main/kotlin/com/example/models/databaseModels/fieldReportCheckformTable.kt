package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object fieldReportCheckformTable : Table("fieldReportCheckform") {
    val fieldReportCheckFormId = varchar("FieldReportCheckFormId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val fieldReportEquipmentId = varchar("FieldReportEquipmentId", 36).references(fieldReportEquipmentTable.fieldReportEquipmentID).nullable()
    val description = text("Description").nullable()
    val valueExpected = text("ValueExpected").nullable()
    val valueMeasured = text("ValueMeasured").nullable()
    val result = text("Result").nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(fieldReportCheckFormId)
}