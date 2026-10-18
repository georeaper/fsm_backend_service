package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object PermissionTable : Table("permissions") {
    val id = integer("id").autoIncrement()
    val code = varchar("code", 150).uniqueIndex()
    val description = varchar("description", 500).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
