package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object userTable : Table("Users") {
    val userId = varchar("UserId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val name = varchar("Name", 255).nullable()
    val description = text("Description").nullable()
    val email = varchar("Email", 255).nullable()
    val phone = varchar("Phone", 20).nullable()
    val signature = binary("Signature").nullable()
    val reportPrefix = varchar("PrefixWO", 50).nullable()
    val technicalCasePrefix = varchar("PrefixTC", 50).nullable()
    val lastReportNumber = integer("LastReportNumber").nullable()
    val lastTCNumber = integer("LastTCNumber").nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(userId)
}