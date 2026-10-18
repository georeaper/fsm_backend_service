package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UserRoleTable : Table("user_roles") {
    val userId = integer("user_id")
        .references(UserTable.id, onDelete = ReferenceOption.CASCADE)

    val roleId = integer("role_id")
        .references(RoleTable.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(userId, roleId)
}
