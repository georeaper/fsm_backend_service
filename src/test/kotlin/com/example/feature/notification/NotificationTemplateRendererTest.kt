package com.example.feature.notification

import com.example.feature.notification.model.EntitySnapshot
import com.example.feature.notification.model.NotificationEntityType
import com.example.feature.notification.service.NotificationTemplateRenderer
import com.example.feature.notification.scheduler.NotificationJobExecutionPolicy
import kotlin.test.*

class NotificationTemplateRendererTest {
    @Test fun `valid placeholder is replaced`() {
        assertEquals("WO-42", NotificationTemplateRenderer.render("{{workOrder.reportNumber}}", workOrder(reportNumber = "WO-42")))
    }

    @Test fun `multiple placeholders and legacy alias are replaced`() {
        val result = NotificationTemplateRenderer.render(
            "{{workOrder.reportNumber}} - {{workOrder.title}} - {{entityTitle}} - {{workOrder.status}}",
            workOrder(reportNumber = "WO-42", title = "Repair", status = "OPEN")
        )
        assertEquals("WO-42 - Repair - Repair - OPEN", result)
    }

    @Test fun `supported null values render as empty strings`() {
        assertEquals("Number: ", NotificationTemplateRenderer.render("Number: {{workOrder.reportNumber}}", workOrder(reportNumber = null)))
    }

    @Test fun `unknown placeholder is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            NotificationTemplateRenderer.validate("{{workOrder.databasePassword}}", NotificationEntityType.WORK_ORDER)
        }
    }

    @Test fun `placeholder from another entity is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            NotificationTemplateRenderer.validate("{{contract.dateEnd}}", NotificationEntityType.WORK_ORDER)
        }
    }

    @Test fun `scheduled delivery uses latest entity values`() {
        val template = "Status: {{workOrder.status}}"
        val original = workOrder(status = "OPEN")
        val updated = workOrder(status = "COMPLETED")
        assertEquals("Status: OPEN", NotificationTemplateRenderer.render(template, original))
        assertEquals("Status: COMPLETED", NotificationTemplateRenderer.renderForDelivery(template, updated, null))
    }

    @Test fun `event delivery keeps event-time value when entity is missing or deleted`() {
        val template = "Status: {{workOrder.status}}"
        assertEquals("Status: OPEN", NotificationTemplateRenderer.renderForDelivery(template, null, "Status: OPEN"))
        assertEquals("Status: OPEN", NotificationTemplateRenderer.renderForDelivery(template, workOrder(status = "DELETED", deleted = true), "Status: OPEN"))
    }

    @Test fun `scheduled jobs skip missing and deleted entities`() {
        assertEquals("Entity missing or deleted", NotificationJobExecutionPolicy.missingEntityReason(null, false))
        assertEquals("Entity missing or deleted", NotificationJobExecutionPolicy.missingEntityReason(workOrder(deleted = true), false))
        assertNull(NotificationJobExecutionPolicy.missingEntityReason(null, true))
    }

    private fun workOrder(reportNumber: String? = "WO-1", title: String? = "Work", status: String? = "OPEN", deleted: Boolean = false) =
        EntitySnapshot(
            NotificationEntityType.WORK_ORDER, "id-1", title ?: "Work order", status, "user-1", emptyMap(), deleted,
            mapOf("workOrder.id" to "id-1", "workOrder.reportNumber" to reportNumber, "workOrder.title" to title, "workOrder.status" to status)
        )
}
