package com.example.feature.notification.registry

import com.example.feature.notification.dto.*
import com.example.feature.notification.model.*
import com.example.feature.notification.service.NotificationTemplateRenderer
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object NotificationRegistry {
    private val entities = listOf(
        entityMetadata(NotificationEntityType.CONTRACT, listOf("CREATED", "UPDATED", "DELETED", "STATUS_CHANGED"), listOf("DATE_START", "DATE_END", "DATE_CREATED"), false, listOf("ADMINISTRATORS", "SPECIFIC_USER")),
        entityMetadata(NotificationEntityType.WORK_ORDER, listOf("CREATED", "UPDATED", "DELETED", "STATUS_CHANGED", "ASSIGNED"), listOf("SCHEDULED_START", "SCHEDULED_END", "DATE_CREATED"), true, recipients()),
        entityMetadata(NotificationEntityType.TECHNICAL_CASE, listOf("CREATED", "UPDATED", "DELETED", "STATUS_CHANGED", "ASSIGNED"), listOf("DATE_START", "DATE_END", "DATE_CREATED"), true, recipients()),
        entityMetadata(NotificationEntityType.TASK, listOf("CREATED", "UPDATED", "DELETED", "STATUS_CHANGED", "ASSIGNED"), listOf("DATE_START", "DUE_DATE", "DATE_COMPLETED", "DATE_CREATED"), true, recipients())
    )

    fun metadata() = NotificationMetadataResponse(entities, NotificationOffsetUnit.entries.map { it.name }, NotificationOffsetDirection.entries.map { it.name })
    fun descriptor(type: NotificationEntityType) = entities.first { it.entityType == type.name }

    fun validate(request: CreateNotificationRuleRequest) {
        val entity = enumValue<NotificationEntityType>(request.entityType, "entityType")
        val trigger = enumValue<NotificationTriggerType>(request.triggerType, "triggerType")
        val descriptor = descriptor(entity)
        require(request.name.isNotBlank() && request.name.length <= 150) { "Invalid rule name" }
        require(request.titleTemplate.isNotBlank() && request.messageTemplate.isNotBlank()) { "Title and message templates are required" }
        NotificationTemplateRenderer.validate(request.titleTemplate, entity)
        NotificationTemplateRenderer.validate(request.messageTemplate, entity)
        if (trigger == NotificationTriggerType.EVENT) {
            require(request.eventType != null && request.eventType.uppercase() in descriptor.events) { "Unsupported eventType for ${entity.name}" }
            require(request.dateField == null && request.offsetValue == null && request.offsetUnit == null && request.offsetDirection == null) { "EVENT rules cannot contain date fields" }
        } else {
            require(request.eventType == null) { "DATE rules cannot contain eventType" }
            require(request.dateField != null && request.dateField.uppercase() in descriptor.dateFields) { "Unsupported dateField for ${entity.name}" }
            require(request.offsetValue != null && request.offsetValue in 0..3650) { "offsetValue must be between 0 and 3650" }
            enumValue<NotificationOffsetUnit>(request.offsetUnit, "offsetUnit")
            enumValue<NotificationOffsetDirection>(request.offsetDirection, "offsetDirection")
        }
        val recipient = enumValue<NotificationRecipientType>(request.recipientType, "recipientType")
        require(recipient.name in descriptor.recipientTypes) { "Unsupported recipientType for ${entity.name}" }
        require((recipient == NotificationRecipientType.SPECIFIC_USER) == !request.specificUserId.isNullOrBlank()) { "specificUserId is required only for SPECIFIC_USER" }
        request.conditions.forEach { condition ->
            val field = descriptor.conditionFields.firstOrNull { it.key == condition.field.uppercase() }
                ?: throw IllegalArgumentException("Unsupported condition field: ${condition.field}")
            require(condition.operator.uppercase() in field.operators) { "Unsupported operator for ${condition.field}" }
            if (condition.operator.uppercase() in setOf("EQ", "NEQ")) require(!condition.value.isNullOrBlank()) { "Condition value is required" }
        }
    }

    fun parseLegacyDate(value: String, zoneId: ZoneId): Instant? {
        val input = value.trim()
        if (input.isEmpty()) return null
        try { return Instant.parse(input) } catch (_: DateTimeParseException) { }
        listOf("yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss").forEach { pattern ->
            try { return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(pattern)).atZone(zoneId).toInstant() }
            catch (_: DateTimeParseException) { }
        }
        listOf("dd/MM/yyyy", "yyyy-MM-dd").forEach { pattern ->
            try { return LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern)).atStartOfDay(zoneId).toInstant() }
            catch (_: DateTimeParseException) { }
        }
        return null
    }

    fun conditionsMatch(values: List<NotificationConditionDto>, snapshot: EntitySnapshot) = values.all { condition ->
        val actual = when (condition.field.uppercase()) {
            "STATUS" -> snapshot.status
            "ASSIGNED_USER" -> snapshot.assignedUserId
            else -> null
        }
        when (condition.operator.uppercase()) {
            "EQ" -> actual?.equals(condition.value, true) == true
            "NEQ" -> actual?.equals(condition.value, true) != true
            "EXISTS" -> !actual.isNullOrBlank()
            "NOT_EXISTS" -> actual.isNullOrBlank()
            else -> false
        }
    }

    private fun conditions(assigned: Boolean) = buildList {
        add(NotificationFieldMetadata("STATUS", listOf("EQ", "NEQ")))
        if (assigned) add(NotificationFieldMetadata("ASSIGNED_USER", listOf("EXISTS", "NOT_EXISTS")))
    }
    private fun recipients() = NotificationRecipientType.entries.map { it.name }
    private fun entityMetadata(type: NotificationEntityType, events: List<String>, dateFields: List<String>, assigned: Boolean, recipientTypes: List<String>) =
        NotificationEntityMetadata(type.name, events, dateFields, conditions(assigned), recipientTypes, NotificationTemplateRenderer.placeholders(type))
    private inline fun <reified T : Enum<T>> enumValue(value: String?, field: String): T =
        requireNotNull(value?.trim()?.uppercase()?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }) { "Invalid $field" }
}
