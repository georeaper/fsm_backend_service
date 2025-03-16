package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object departmentTable : Table("departmentTable") {
    val departmentId = varchar("DepartmentId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val phone = varchar("Phone", 100).nullable()
    val email = varchar("Email", 100).nullable()
    val contactPerson = varchar("ContactPerson", 255).nullable()
    val notes = text("Notes").nullable()
    val description = text("Description").nullable()
    val departmentStatus = bool("DepartmentStatus").nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    val customerId = varchar("CustomerId", 36).references(customerTable.customerId).nullable()
    override val primaryKey = PrimaryKey(departmentId)
}