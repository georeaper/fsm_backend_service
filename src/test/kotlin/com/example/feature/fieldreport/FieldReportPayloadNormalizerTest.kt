package com.example.feature.fieldreport

import com.example.feature.fieldreport.dto.PostFieldReportResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class FieldReportPayloadNormalizerTest {
    @Test
    fun `Status aliases ReportStatus and priority is normalized`() {
        val result = FieldReportPayloadNormalizer.normalizeFieldReportRequest(
            PostFieldReportResponse(
                ReportStatus = "legacy",
                Status = "IN_PROGRESS",
                Priority = "HIGH",
                Type = "MAINTENANCE"
            )
        )

        assertEquals("IN_PROGRESS", result.Status)
        assertEquals("IN_PROGRESS", result.ReportStatus)
        assertEquals("HIGH", result.Priority)
        assertEquals("MAINTENANCE", result.Type)
    }
}
