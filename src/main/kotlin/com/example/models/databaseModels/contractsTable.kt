package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object contractsTable : Table("Contracts") {
    val contractId = varchar("ContractId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val title = varchar("Title", 255).nullable()
    val dateStart = varchar("DateStart", 50).nullable()
    val dateEnd = varchar("DateEnd", 50).nullable()
    val value = double("Value").nullable()
    val notes = text("Notes").nullable()
    val description = text("Description").nullable()
    val contractType = varchar("ContractType", 100).nullable()
    val contractStatus = bool("ContractStatus").nullable()
    val contactName = varchar("ContactName", 255).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    val customerId = varchar("CustomerId", 36).references(customerTable.customerId).nullable()
    override val primaryKey = PrimaryKey(contractId)
}