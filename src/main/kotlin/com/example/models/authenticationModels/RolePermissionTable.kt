package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object RolePermissionTable : Table("role_permissions") {
    val roleId = integer("role_id")
        .references(RoleTable.id, onDelete = ReferenceOption.CASCADE)

    val permissionId = integer("permission_id")
        .references(PermissionTable.id, onDelete = ReferenceOption.CASCADE)

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(roleId, permissionId)
}
