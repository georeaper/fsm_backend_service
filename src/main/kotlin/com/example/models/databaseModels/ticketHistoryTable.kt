package com.example.models.databaseModels

import com.example.models.databaseModels.customerTable.nullable
import com.example.models.databaseModels.tasksTable.default
import com.example.models.databaseModels.tasksTable.nullable
import com.example.models.databaseModels.tasksTable.references
import com.example.models.databaseModels.ticketTable.default
import com.example.models.databaseModels.ticketTable.nullable
import com.example.models.databaseModels.ticketTable.references
import org.jetbrains.exposed.sql.Table

object ticketHistoryTable : Table("ticketHistoryTable") {
    val ticketHistoryId = varchar("TicketHistoryId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val title = varchar("Title", 255).nullable()
    val ticketNumber = varchar("TicketNumber", 255).nullable()
    val description = text("Description").nullable()
    val notes = varchar("Notes", 50).nullable()
    val urgency = varchar("Urgency", 50).nullable()
    val active = bool("Active").nullable()
    val dateStart = varchar("DateStart", 50).nullable()
    val dateEnd = varchar("DateEnd", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 50).nullable()

    val userId = varchar("UserId",36).references(userTable.userId).nullable()
    val customerId = varchar("CustomerId",36).references(customerTable.customerId).nullable()
    val equipmentId = varchar("EquipmentId",36).references(equipmentTable.equipmentId).nullable()

    override val primaryKey =PrimaryKey(ticketHistoryId)
}