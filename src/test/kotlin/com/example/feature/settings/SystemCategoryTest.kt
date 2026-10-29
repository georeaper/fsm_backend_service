package com.example.feature.settings

import com.example.feature.settings.usecase.validatedSystemCategory
import com.example.feature.settings.usecase.validatedSettingStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SystemCategoryTest {
    @Test fun `status categories are normalized and validated`() {
        assertEquals("OPEN", validatedSystemCategory("TaskStatus", "open"))
        assertFailsWith<IllegalArgumentException> { validatedSystemCategory("WorkOrderStatus", "PAUSED") }
        assertFailsWith<IllegalArgumentException> { validatedSystemCategory("ContractStatus", null) }
    }

    @Test fun `priority categories are normalized and validated`() {
        assertEquals("HIGH", validatedSystemCategory("TaskPriority", "high"))
        assertEquals("MEDIUM", validatedSystemCategory("WorkOrderPriority", " medium "))
        assertEquals("LOW", validatedSystemCategory("TechnicalCasePriority", "LOW"))
        assertFailsWith<IllegalArgumentException> { validatedSystemCategory("TaskPriority", "URGENT") }
        assertFailsWith<IllegalArgumentException> { validatedSystemCategory("WorkOrderPriority", null) }
    }

    @Test fun `other setting types do not use a system category`() {
        assertNull(validatedSystemCategory("TaskType", "HIGH"))
    }

    @Test fun `setting style accepts supported hex colors`() {
        assertEquals("#abc", validatedSettingStyle(" #abc "))
        assertEquals("#2563EB", validatedSettingStyle("#2563EB"))
        assertEquals("#2563EBCC", validatedSettingStyle("#2563EBCC"))
    }

    @Test fun `setting style rejects non color values`() {
        assertFailsWith<IllegalArgumentException> { validatedSettingStyle("blue") }
        assertFailsWith<IllegalArgumentException> { validatedSettingStyle("") }
        assertFailsWith<IllegalArgumentException> { validatedSettingStyle("#12") }
    }
}
