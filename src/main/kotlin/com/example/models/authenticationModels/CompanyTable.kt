package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

object CompanyTable : Table("companies") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val contactEmail = varchar("contact_email", 255)
    val databaseName = varchar("database_name", 255)
    val databaseHost = varchar("database_host", 255).default("localhost")
    val databasePort = integer("database_port").default(5432)
    val databaseUser = varchar("database_user", 255)
    val databasePassword = varchar("database_password", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}