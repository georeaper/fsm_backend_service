package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object categoryAssetTable : Table("categoryAssetTable") {
    val categoryAssetId = varchar("CategoryAssetId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val style = varchar("Style", 255).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(categoryAssetId)
}