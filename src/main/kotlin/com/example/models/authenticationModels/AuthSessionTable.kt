package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import java.util.UUID

object AuthSessionTable : Table("auth_sessions") {

    val id = uuid("id")
        .clientDefault { UUID.randomUUID() }

    val userId = integer("user_id")
        .references(UserTable.id)

    val refreshTokenHash = varchar("refresh_token_hash", 64)
        .uniqueIndex()

    val createdAt = datetime("created_at")
        .defaultExpression(CurrentDateTime)

    val expiresAt = datetime("expires_at")

    val revokedAt = datetime("revoked_at").nullable()

    val lastUsedAt = datetime("last_used_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
