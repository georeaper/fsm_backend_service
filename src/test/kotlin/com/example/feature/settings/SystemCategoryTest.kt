package com.example.feature.settings

import com.example.feature.settings.usecase.validatedSystemCategory
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

    @Test fun `non status settings do not use a system category`() {
        assertNull(validatedSystemCategory("TaskPriority", "OPEN"))
    }
}
