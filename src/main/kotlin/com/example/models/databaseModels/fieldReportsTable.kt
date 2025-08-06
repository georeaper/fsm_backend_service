package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID


object fieldReportsTable : Table("fieldreports") {
    val fieldReportId= varchar("FieldReportId", 36).default(UUID.randomUUID().toString())
    val remoteID = integer("RemoteId").nullable()
    val reportNumber = varchar("ReportNumber", 255).nullable()
    val description = text("Description").nullable()
    val startDate = varchar("StartDate", 255).nullable()
    val endDate = varchar("EndDate", 255).nullable()
    val title = varchar("Title", 255).nullable()
    val department = varchar("Department", 255).nullable()
    val clientName = varchar("ClientName", 255).nullable()
    val reportStatus = varchar("ReportStatus", 255).nullable()
    val clientSignature = binary("ClientSignature").nullable()
    val value = double("Value").nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()
    val customerID = varchar("CustomerId", 36).references(customerTable.customerId).nullable()   //.references(customerTable.customerId)
    val contractID = varchar("ContractId", 36).references(contractsTable.contractId).nullable()
    val userID = varchar("UserId", 36).references(userTable.userId).nullable()
    val caseID = varchar("CaseId", 36).references(ticketTable.ticketId).nullable()

    override val primaryKey = PrimaryKey(fieldReportId)
}