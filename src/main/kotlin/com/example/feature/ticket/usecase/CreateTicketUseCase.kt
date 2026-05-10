package com.example.feature.ticket.usecase

import com.example.core.RequestContext
import com.example.feature.ticket.repository.TicketRepository
import com.example.feature.ticket.dto.CreateTicketResponse
import com.example.models.api.Tickets
import com.example.core.DateUtils
import java.util.UUID

class CreateTicketUseCase (private val repository: TicketRepository){
    fun execute(ctx: RequestContext, request: CreateTicketResponse) : Tickets {
        val storageDate = DateUtils.nowStorage()
        val tickets = Tickets(
            TicketID = UUID.randomUUID().toString(),
            RemoteID = null,
            Title = request.Title,
            TicketNumber = request.TicketNumber,
            Description = request.Description,
            Notes = request.Notes,
            Urgency = request.Urgency,
            Active = request.Active,
            DateStart = DateUtils.uiToStorage(request.DateStart!!),
            DateEnd = DateUtils.uiToStorage(request.DateEnd!!),
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1",
            UserID = request.UserID,
            CustomerID = request.CustomerID,
            EquipmentID = request.EquipmentID
        )
        return repository.save(ctx, tickets)
    }
}
