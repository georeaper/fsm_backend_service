package com.example.feature.notification.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class NotificationConditionDto(val field: String, val operator: String, val value: String? = null)

@Serializable
data class CreateNotificationRuleRequest(
    val name: String,
    val entityType: String,
    val triggerType: String,
    val eventType: String? = null,
    val dateField: String? = null,
    val offsetValue: Int? = null,
    val offsetUnit: String? = null,
    val offsetDirection: String? = null,
    val conditions: List<NotificationConditionDto> = emptyList(),
    val recipientType: String,
    val specificUserId: String? = null,
    val titleTemplate: String,
    val messageTemplate: String,
    val isEnabled: Boolean = true
)

@Serializable
data class UpdateNotificationRuleRequest(
    val name: String,
    val entityType: String,
    val triggerType: String,
    val eventType: String? = null,
    val dateField: String? = null,
    val offsetValue: Int? = null,
    val offsetUnit: String? = null,
    val offsetDirection: String? = null,
    val conditions: List<NotificationConditionDto> = emptyList(),
    val recipientType: String,
    val specificUserId: String? = null,
    val titleTemplate: String,
    val messageTemplate: String,
    val isEnabled: Boolean
)

@Serializable
data class NotificationRuleResponse(
    val ruleId: String, val name: String, val entityType: String, val triggerType: String,
    val eventType: String?, val dateField: String?, val offsetValue: Int?, val offsetUnit: String?,
    val offsetDirection: String?, val conditions: List<NotificationConditionDto>, val recipientType: String,
    val specificUserId: String?, val titleTemplate: String, val messageTemplate: String, val isEnabled: Boolean,
    val dateCreatedEpochMs: Long, val lastModifiedEpochMs: Long
)

@Serializable
data class SetNotificationRuleEnabledRequest(val isEnabled: Boolean)

@Serializable
data class UserNotificationResponse(
    val notificationId: String, val title: String?, val message: String?, val type: String?,
    val entityType: String?, val entityId: String?, val isRead: Boolean, val createdAtEpochMs: Long?
)

@Serializable
data class NotificationJobResponse(
    val jobId: String, val ruleId: String?, val entityType: String, val entityId: String,
    val recipientUserId: String, val scheduledAtEpochMs: Long, val status: String,
    val attempts: Int, val lastError: String?,
    @Transient val renderedTitle: String? = null,
    @Transient val renderedMessage: String? = null
)

@Serializable
data class RegisterDeviceTokenRequest(val deviceToken: String, val platform: String)

@Serializable
data class NotificationFieldMetadata(val key: String, val operators: List<String> = emptyList())

@Serializable
data class NotificationPlaceholderMetadata(val key: String, val label: String)

@Serializable
data class NotificationEntityMetadata(
    val entityType: String, val events: List<String>, val dateFields: List<String>,
    val conditionFields: List<NotificationFieldMetadata>, val recipientTypes: List<String>,
    val placeholders: List<NotificationPlaceholderMetadata> = emptyList()
)

@Serializable
data class NotificationMetadataResponse(
    val entities: List<NotificationEntityMetadata>,
    val offsetUnits: List<String>,
    val offsetDirections: List<String>
)
