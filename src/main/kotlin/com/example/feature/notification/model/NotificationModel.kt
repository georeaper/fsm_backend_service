package com.example.feature.notification.model

import com.example.feature.notification.dto.NotificationConditionDto

enum class NotificationEntityType { CONTRACT, WORK_ORDER, TECHNICAL_CASE, TASK }
enum class NotificationTriggerType { EVENT, DATE }
enum class NotificationEventType { CREATED, UPDATED, DELETED, STATUS_CHANGED, ASSIGNED }
enum class NotificationRecipientType { ASSIGNED_USER, ADMINISTRATORS, SPECIFIC_USER }
enum class NotificationOffsetUnit { MINUTES, HOURS, DAYS }
enum class NotificationOffsetDirection { BEFORE, AFTER }
enum class NotificationJobStatus { PENDING, PROCESSING, COMPLETED, RETRY, FAILED, CANCELLED, SKIPPED }

data class EntitySnapshot(
    val entityType: NotificationEntityType,
    val entityId: String,
    val title: String,
    val status: String?,
    val assignedUserId: String?,
    val dates: Map<String, String?>,
    val isDeleted: Boolean,
    val templateValues: Map<String, String?> = emptyMap()
)

data class NotificationRule(
    val ruleId: String, val name: String, val entityType: NotificationEntityType,
    val triggerType: NotificationTriggerType, val eventType: NotificationEventType?, val dateField: String?,
    val offsetValue: Int?, val offsetUnit: NotificationOffsetUnit?, val offsetDirection: NotificationOffsetDirection?,
    val conditions: List<NotificationConditionDto>, val recipientType: NotificationRecipientType,
    val specificUserId: String?, val titleTemplate: String, val messageTemplate: String,
    val isEnabled: Boolean, val dateCreatedEpochMs: Long, val lastModifiedEpochMs: Long
)
