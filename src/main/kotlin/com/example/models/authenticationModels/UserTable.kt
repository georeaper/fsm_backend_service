package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255) // Hashed
    val companyId = integer("company_id").references(CompanyTable.id)
    val role = varchar("role", 50).default("user")
    val status = varchar("status", 50).default("active")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}