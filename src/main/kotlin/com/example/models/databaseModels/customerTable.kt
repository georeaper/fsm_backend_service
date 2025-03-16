package com.example.models.databaseModels

import com.example.models.databaseModels.fieldReportsTable.default
import com.example.models.databaseModels.ticketTable.default
import org.jetbrains.exposed.sql.Table
import java.util.*

object customerTable : Table("customerTable") {
    val customerId = varchar("CustomerId", 36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val phone = varchar("Phone", 20).nullable()
    val email = varchar("Email", 255).nullable()
    val address = varchar("Address", 255).nullable()
    val zipCode = varchar("ZipCode", 20).nullable()
    val city = varchar("City", 100).nullable()
    val notes = text("Notes").nullable()
    val description = text("Description").nullable()
    val customerStatus = bool("CustomerStatus").nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(customerTable.customerId)
}
