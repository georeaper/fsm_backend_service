package com.example.feature.fieldreport

import com.example.feature.fieldreport.dto.TechnicalCaseCompletionUpdate
import com.example.feature.fieldreport.repository.completionDecisionsCoverLinkedCases
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldReportCompletionTest {
    @Test
    fun `completion requires exactly one decision for every linked technical case`() {
        val linked = listOf("tc-1", "tc-2")

        assertTrue(completionDecisionsCoverLinkedCases(linked, listOf(
            update("tc-1"), update("tc-2")
        )))
        assertFalse(completionDecisionsCoverLinkedCases(linked, listOf(update("tc-1"))))
        assertFalse(completionDecisionsCoverLinkedCases(linked, listOf(
            update("tc-1"), update("tc-1")
        )))
        assertFalse(completionDecisionsCoverLinkedCases(linked, listOf(
            update("tc-1"), update("tc-3")
        )))
    }

    @Test
    fun `work order without technical cases needs no case decisions`() {
        assertTrue(completionDecisionsCoverLinkedCases(emptyList(), emptyList()))
    }

    private fun update(id: String) = TechnicalCaseCompletionUpdate(id, "Resolved")
}
