package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RoleTable : Table("roles") {
    val id = integer("id").autoIncrement()

    val companyId = integer("company_id")
        .references(CompanyTable.id, onDelete = ReferenceOption.CASCADE)

    val name = varchar("name", 100)
    val description = varchar("description", 500).nullable()
    val isSystem = bool("is_system").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(companyId, name)
    }

    override val primaryKey = PrimaryKey(id)
}
