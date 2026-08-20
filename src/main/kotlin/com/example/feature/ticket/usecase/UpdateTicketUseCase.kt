package com.example.feature.ticket.usecase

import com.example.core.RequestContext
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.repository.TicketRepository

class UpdateTicketUseCase(private val repository: TicketRepository) {
    fun execute(ctx: RequestContext, input: EditTicketResponse): EditTicketResponse {
        return repository.edit(ctx, input)
    }
}
