package com.example.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateUtilsTest {
    @Test fun `ui date-time keeps its time in storage format`() {
        assertEquals("20261028060000", DateUtils.uiToStorage("28/10/2026 06:00:00"))
    }

    @Test fun `get conversion reads storage and legacy put formats`() {
        assertEquals("28/10/2026", DateUtils.storageToUi("20261028060000"))
        assertEquals("28/10/2026", DateUtils.storageToUi("28/10/2026 06:00:00"))
    }

    @Test fun `invalid date is rejected`() {
        assertNull(DateUtils.uiToStorage("31/02/2026 06:00:00"))
    }

    @Test fun `schedule date-time accepts storage and UI date-time formats`() {
        assertEquals("20261028060000", DateUtils.normalizeDateTimeStorage("20261028060000"))
        assertEquals("20261028060000", DateUtils.normalizeDateTimeStorage("28/10/2026 06:00:00"))
        assertNull(DateUtils.normalizeDateTimeStorage("28/10/2026"))
        assertNull(DateUtils.normalizeDateTimeStorage("20260231060000"))
    }

    @Test fun `scheduled duration is calculated in hours`() {
        assertEquals(4.0, DateUtils.durationHours("20261028060000", "20261028100000"))
        assertEquals(0.0, DateUtils.durationHours("20261028100000", "20261028060000"))
    }

    @Test fun `calendar range is calculated in days`() {
        assertEquals(31L, DateUtils.rangeDays("20261001000000", "20261101000000"))
        assertNull(DateUtils.rangeDays("invalid", "20261101000000"))
    }
}
