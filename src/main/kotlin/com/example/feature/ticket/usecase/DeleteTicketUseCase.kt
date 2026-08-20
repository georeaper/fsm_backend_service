package com.example.feature.ticket.usecase

import com.example.core.RequestContext
import com.example.feature.ticket.repository.TicketRepository

class DeleteTicketUseCase(private val repository: TicketRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
