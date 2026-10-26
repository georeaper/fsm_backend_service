package com.example.feature.notification.repository

import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.Table

object NotificationRulesTable : Table("notification_rules") {
    val ruleId = varchar("RuleId", 36)
    val name = varchar("Name", 150)
    val entityType = varchar("EntityType", 40)
    val triggerType = varchar("TriggerType", 20)
    val eventType = varchar("EventType", 40).nullable()
    val dateField = varchar("DateField", 50).nullable()
    val offsetValue = integer("OffsetValue").nullable()
    val offsetUnit = varchar("OffsetUnit", 20).nullable()
    val offsetDirection = varchar("OffsetDirection", 10).nullable()
    val conditionsJson = text("ConditionsJson").default("[]")
    val recipientType = varchar("RecipientType", 30)
    val specificUserId = varchar("SpecificUserId", 36).references(userTable.userId).nullable()
    val titleTemplate = text("TitleTemplate")
    val messageTemplate = text("MessageTemplate")
    val isEnabled = bool("IsEnabled").default(true)
    val createdByUserId = varchar("CreatedByUserId", 36).references(userTable.userId).nullable()
    val dateCreatedEpochMs = long("DateCreatedEpochMs")
    val lastModifiedEpochMs = long("LastModifiedEpochMs")
    override val primaryKey = PrimaryKey(ruleId)
}

object NotificationJobsTable : Table("notification_jobs") {
    val jobId = varchar("JobId", 36)
    val ruleId = varchar("RuleId", 36).references(NotificationRulesTable.ruleId).nullable()
    val entityType = varchar("EntityType", 40)
    val entityId = varchar("EntityId", 36)
    val recipientUserId = varchar("RecipientUserId", 36).references(userTable.userId)
    val scheduledAtEpochMs = long("ScheduledAtEpochMs")
    val status = varchar("Status", 20)
    val attempts = integer("Attempts").default(0)
    val maxAttempts = integer("MaxAttempts").default(5)
    val nextAttemptAtEpochMs = long("NextAttemptAtEpochMs").nullable()
    val claimedAtEpochMs = long("ClaimedAtEpochMs").nullable()
    val claimedBy = varchar("ClaimedBy", 100).nullable()
    val leaseUntilEpochMs = long("LeaseUntilEpochMs").nullable()
    val lastError = text("LastError").nullable()
    val sourceValue = varchar("SourceValue", 255).nullable()
    val renderedTitle = text("RenderedTitle").nullable()
    val renderedMessage = text("RenderedMessage").nullable()
    val idempotencyKey = varchar("IdempotencyKey", 255).uniqueIndex()
    val dateCreatedEpochMs = long("DateCreatedEpochMs")
    val processedAtEpochMs = long("ProcessedAtEpochMs").nullable()
    val cancelledAtEpochMs = long("CancelledAtEpochMs").nullable()
    val cancelReason = text("CancelReason").nullable()
    override val primaryKey = PrimaryKey(jobId)
}

object UserDeviceTokensTable : Table("user_device_tokens") {
    val tokenId = varchar("TokenId", 36)
    val userId = varchar("UserId", 36).references(userTable.userId)
    val deviceToken = text("DeviceToken")
    val platform = varchar("Platform", 20)
    val isActive = bool("IsActive").default(true)
    val lastSeenAtEpochMs = long("LastSeenAtEpochMs")
    val dateCreatedEpochMs = long("DateCreatedEpochMs")
    override val primaryKey = PrimaryKey(tokenId)
}
