package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object settingsTable : Table("settings") {
    val settingsId = varchar("SettingsId", 36).default(UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val settingsKey = varchar("SettingsKey", 255).nullable()
    val settingsValue = varchar("SettingsValue", 255).nullable()
    val settingsStyle = varchar("SettingsStyle", 255).nullable()
    val settingsDescription = text("SettingsDescription").nullable()
    val lastModified = varchar("LastModified", 255).nullable()
    val dateCreated = varchar("DateCreated", 255).nullable()
    val version = varchar("Version", 255).nullable()

    override val primaryKey = PrimaryKey(settingsId)
}