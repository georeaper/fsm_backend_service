package com.example.models.databaseModels

import com.example.models.databaseModels.ticketHistoryTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object ticketTable : Table("ticket") {
    val ticketId = varchar("TicketId",36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val title = varchar("Title", 255).nullable()
    val ticketNumber = varchar("TicketNumber", 50).nullable()
    val description = text("Description").nullable()
    val notes = text("Notes").nullable()
    val urgency = varchar("Urgency", 50).nullable()
    val active = bool("Active").default(true).nullable()
    val dateStart = varchar("DateStart", 50).nullable()
    val dateEnd = varchar("DateEnd", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    val userId = varchar("UserId",36).references(userTable.userId).nullable()
    val customerId = varchar("CustomerId",36).references(customerTable.customerId).nullable()
    val equipmentId = varchar("EquipmentId",36).references(equipmentTable.equipmentId).nullable()
    override val primaryKey= PrimaryKey(ticketId)

}
