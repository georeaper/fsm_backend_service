package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object ticketTable : Table("ticketTable") {
    val ticketId = varchar("TicketId",36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val title = varchar("Title", 255)
    val ticketNumber = varchar("TicketNumber", 50)
    val description = text("Description").nullable()
    val notes = text("Notes").nullable()
    val urgency = varchar("Urgency", 50)
    val active = bool("Active").default(true)
    val dateStart = datetime("DateStart").nullable()
    val dateEnd = datetime("DateEnd").nullable()
    val lastModified = datetime("LastModified").nullable()
    val dateCreated = datetime("DateCreated").nullable()
    val version = varchar("Version", 20).nullable()
    val userID = varchar("UserId",36).references(userTable.userId)
    val customerID = varchar("CustomerId",36).references(customerTable.customerId).nullable()
    val equipmentID = varchar("EquipmentId",36).references(equipmentTable.equipmentId).nullable()
    override val primaryKey= PrimaryKey(ticketId)

}
