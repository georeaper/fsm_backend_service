package com.example.feature.notification

import com.example.feature.notification.dto.CreateNotificationRuleRequest
import com.example.feature.notification.dto.NotificationConditionDto
import com.example.feature.notification.model.EntitySnapshot
import com.example.feature.notification.model.NotificationEntityType
import com.example.feature.notification.registry.NotificationRegistry
import java.time.ZoneId
import kotlin.test.*

class NotificationRegistryTest {
    @Test fun `work order schedule rule is accepted`() {
        NotificationRegistry.validate(CreateNotificationRuleRequest(
            name = "Two hours before", entityType = "WORK_ORDER", triggerType = "DATE",
            dateField = "SCHEDULED_START", offsetValue = 2, offsetUnit = "HOURS", offsetDirection = "BEFORE",
            conditions = listOf(NotificationConditionDto("STATUS", "NEQ", "COMPLETED")),
            recipientType = "ASSIGNED_USER", titleTemplate = "Upcoming", messageTemplate = "{{entityTitle}}"
        ))
    }

    @Test fun `arbitrary date field is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            NotificationRegistry.validate(CreateNotificationRuleRequest(
                name = "Unsafe", entityType = "WORK_ORDER", triggerType = "DATE", dateField = "DROP TABLE",
                offsetValue = 1, offsetUnit = "DAYS", offsetDirection = "BEFORE",
                recipientType = "ASSIGNED_USER", titleTemplate = "x", messageTemplate = "x"
            ))
        }
    }

    @Test fun `unsupported template placeholder is rejected when saving rule`() {
        assertFailsWith<IllegalArgumentException> {
            NotificationRegistry.validate(CreateNotificationRuleRequest(
                name = "Unsafe template", entityType = "WORK_ORDER", triggerType = "EVENT", eventType = "UPDATED",
                recipientType = "ASSIGNED_USER", titleTemplate = "{{workOrder.secret}}", messageTemplate = "Changed"
            ))
        }
    }

    @Test fun `metadata exposes labeled placeholders per entity`() {
        val workOrder = NotificationRegistry.metadata().entities.single { it.entityType == "WORK_ORDER" }
        assertTrue(workOrder.placeholders.any { it.key == "workOrder.reportNumber" && it.label == "Report number" })
        assertTrue(workOrder.placeholders.any { it.key == "entityTitle" })
    }

    @Test fun `legacy and ISO dates parse to the same UTC instant`() {
        val zone = ZoneId.of("UTC")
        assertEquals(
            NotificationRegistry.parseLegacyDate("20261025143000", zone),
            NotificationRegistry.parseLegacyDate("2026-10-25T14:30:00Z", zone)
        )
    }

    @Test fun `conditions are evaluated against controlled snapshot fields`() {
        val snapshot = EntitySnapshot(NotificationEntityType.TASK, "1", "Task", "OPEN", "u1", emptyMap(), false)
        assertTrue(NotificationRegistry.conditionsMatch(listOf(NotificationConditionDto("STATUS", "EQ", "open"), NotificationConditionDto("ASSIGNED_USER", "EXISTS")), snapshot))
        assertFalse(NotificationRegistry.conditionsMatch(listOf(NotificationConditionDto("STATUS", "EQ", "completed")), snapshot))
    }
}
