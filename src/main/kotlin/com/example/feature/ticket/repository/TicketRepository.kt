package com.example.feature.ticket.repository

import com.example.core.RequestContext
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.dto.TicketsResponse
import com.example.models.api.Tickets

interface TicketRepository {
    fun save(ctx: RequestContext, data: Tickets): Tickets
    fun findAll(ctx: RequestContext): List<TicketsResponse>
    fun edit(ctx: RequestContext, data: EditTicketResponse): EditTicketResponse
}
