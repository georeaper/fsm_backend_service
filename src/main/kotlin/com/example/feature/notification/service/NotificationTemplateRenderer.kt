package com.example.feature.notification.service

import com.example.feature.notification.dto.NotificationPlaceholderMetadata
import com.example.feature.notification.model.EntitySnapshot
import com.example.feature.notification.model.NotificationEntityType

/** Renders only explicitly registered values; it never traverses entity properties. */
object NotificationTemplateRenderer {
    const val NULL_FALLBACK = ""
    private val placeholderPattern = Regex("""\{\{\s*([A-Za-z][A-Za-z0-9.]*)\s*}}""")

    private val legacy = listOf(
        NotificationPlaceholderMetadata("entityTitle", "Entity title"),
        NotificationPlaceholderMetadata("entityId", "Entity ID"),
        NotificationPlaceholderMetadata("status", "Status")
    )

    private val perEntity = mapOf(
        NotificationEntityType.CONTRACT to listOf(
            placeholder("contract.id", "Contract ID"), placeholder("contract.title", "Contract title"),
            placeholder("contract.status", "Contract status"), placeholder("contract.dateStart", "Contract start date"),
            placeholder("contract.dateEnd", "Contract end date"), placeholder("contract.dateCreated", "Contract creation date")
        ),
        NotificationEntityType.WORK_ORDER to listOf(
            placeholder("workOrder.id", "Work order ID"), placeholder("workOrder.reportNumber", "Report number"),
            placeholder("workOrder.title", "Work order title"), placeholder("workOrder.status", "Work order status"),
            placeholder("workOrder.scheduledStart", "Scheduled start"), placeholder("workOrder.scheduledEnd", "Scheduled end"),
            placeholder("workOrder.dateCreated", "Work order creation date")
        ),
        NotificationEntityType.TECHNICAL_CASE to listOf(
            placeholder("technicalCase.id", "Technical case ID"), placeholder("technicalCase.ticketNumber", "Ticket number"),
            placeholder("technicalCase.title", "Technical case title"), placeholder("technicalCase.status", "Technical case status"),
            placeholder("technicalCase.dateStart", "Technical case start date"), placeholder("technicalCase.dateEnd", "Technical case end date"),
            placeholder("technicalCase.dateCreated", "Technical case creation date")
        ),
        NotificationEntityType.TASK to listOf(
            placeholder("task.id", "Task ID"), placeholder("task.title", "Task title"), placeholder("task.status", "Task status"),
            placeholder("task.dateStart", "Task start date"), placeholder("task.dueDate", "Task due date"),
            placeholder("task.dateCompleted", "Task completion date"), placeholder("task.dateCreated", "Task creation date")
        )
    )

    fun placeholders(entityType: NotificationEntityType): List<NotificationPlaceholderMetadata> = legacy + perEntity.getValue(entityType)

    fun validate(template: String, entityType: NotificationEntityType) {
        val supported = placeholders(entityType).mapTo(mutableSetOf()) { it.key }
        val unknown = placeholderPattern.findAll(template).map { it.groupValues[1] }.filterNot { it in supported }.distinct().toList()
        require(unknown.isEmpty()) { "Unsupported notification placeholder(s) for ${entityType.name}: ${unknown.joinToString()}" }
        val withoutValidPlaceholders = placeholderPattern.replace(template, "")
        require("{{" !in withoutValidPlaceholders && "}}" !in withoutValidPlaceholders) { "Invalid notification placeholder syntax" }
    }

    fun render(template: String, snapshot: EntitySnapshot): String {
        val values = snapshot.templateValues + mapOf(
            "entityTitle" to snapshot.title,
            "entityId" to snapshot.entityId,
            "status" to snapshot.status
        )
        return placeholderPattern.replace(template) { match -> values[match.groupValues[1]] ?: NULL_FALLBACK }
    }

    fun renderForDelivery(template: String, latestSnapshot: EntitySnapshot?, eventRenderedValue: String?): String =
        eventRenderedValue ?: latestSnapshot?.let { render(template, it) } ?: NULL_FALLBACK

    private fun placeholder(key: String, label: String) = NotificationPlaceholderMetadata(key, label)
}
