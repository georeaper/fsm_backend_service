package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object manufacturerTable : Table("manufacturerTable") {
    val manufacturerId = varchar("ManufacturerId", 36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val style = varchar("Style", 255).nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()

    override val primaryKey = PrimaryKey(manufacturerId)
}
