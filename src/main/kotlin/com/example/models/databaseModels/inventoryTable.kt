package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object inventoryTable : Table("inventory_table") {
    val inventoryId = varchar("InventoryId", 36).default(UUID.randomUUID().toString())
    val remoteID = integer("RemoteId").nullable()
    val title = varchar("Title", 255).nullable()
    val description = text("Description").nullable()
    val quantity = long("Quantity").nullable()
    val value = double("Value").nullable()
    val type = varchar("Type", 255).nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()

    override val primaryKey = PrimaryKey(inventoryId)
}