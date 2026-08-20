package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object CategoriesTable : Table("category_asset") {
    val categoryId = varchar("CategoryAssetId", 36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val style =varchar("Style", 255).nullable()
    //val description = text("Description").nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    val isDeleted = bool("IsDeleted").default(false)

    override val primaryKey = PrimaryKey(categoryId)
}
