package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object tasksTable : Table("tasks") {
    val taskId = varchar("TaskId",36).default(java.util.UUID.randomUUID().toString())
    val title = varchar("Title", 255).nullable()
    val description = text("Description").nullable()
    val status = varchar("Status", 50).nullable()
    val priority = varchar("Priority", 50).nullable()
    val dateStart = varchar("DateStart", 50).nullable()
    val dateDue = varchar("DateDue", 50).nullable()
    val dateCompleted = varchar("DateCompleted", 50).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val ticketId = varchar("TicketId",36).references(ticketTable.ticketId).nullable()
    val userId = varchar("UserId",36).references(userTable.userId).nullable()
    override val primaryKey =PrimaryKey(taskId)
}