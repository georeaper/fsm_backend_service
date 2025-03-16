package com.example.models.databaseModels
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object checkFormsTable : Table("checkFormsTable") {
    val checkFormId = varchar("CheckFormId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val maintenancesId = varchar("MaintenancesId", 255).nullable()
    val description = text("Description").nullable()
    val valueExpected = text("ValueExpected").nullable()
    val valueType = varchar("ValueType", 50).nullable() // checkbox, Textview, Edittext, etc.
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(checkFormId)
}