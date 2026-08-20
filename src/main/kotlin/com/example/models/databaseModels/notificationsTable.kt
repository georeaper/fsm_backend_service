package com.example.models.databaseModels

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object notificationsTable : Table(name="notifications") {
    val notificationId = varchar("NotificationId",length=36).default(UUID.randomUUID().toString())
    val userId = varchar("UserId", length = 36).references(userTable.userId)
    val title =varchar("Title", length = 255).nullable()
    val message = text("Message").nullable()
    val type =varchar("Type", length = 50).nullable()
    val entityType=varchar("EntityType", length = 50).nullable()
    val entityId = varchar("EntityId", length = 36).nullable()
    val isRead = bool("IsRead").default(false)
    val readAt=varchar("ReadAt", length = 50).nullable()
    val dateCreated = varchar("DateCreated", length = 50).nullable()
    val isDeleted = bool("IsDeleted").default(false)
    override val primaryKey = PrimaryKey(notificationId)
}
