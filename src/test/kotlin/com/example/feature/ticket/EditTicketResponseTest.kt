package com.example.feature.ticket

import com.example.feature.ticket.dto.EditTicketResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EditTicketResponseTest {
    @Test
    fun `optional update fields may be omitted from request`() {
        val request = Json.decodeFromString<EditTicketResponse>(
            """{"Title":"Updated","Status":"Open"}"""
        )

        assertEquals("Updated", request.Title)
        assertEquals("Open", request.Status)
        assertEquals("", request.TicketID)
        assertNull(request.TicketNumber)
        assertNull(request.LastModified)
        assertNull(request.DateCreated)
    }
}
