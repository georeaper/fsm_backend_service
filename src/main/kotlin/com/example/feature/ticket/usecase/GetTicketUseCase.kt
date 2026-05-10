package com.example.feature.ticket.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.ticket.repository.TicketRepository
import com.example.feature.ticket.dto.TicketsResponse

class GetTicketUseCase (private val repository: TicketRepository){
    fun execute(ctx: RequestContext) :List<TicketsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            TicketsResponse(
                TicketID = it.TicketID,
                RemoteID = it.RemoteID,
                Title = it.Title,
                TicketNumber = it.TicketNumber,
                Description = it.Description,
                Notes = it.Notes,
                Urgency = it.Urgency,
                Active = it.Active,
                DateStart = DateUtils.storageToUi(it.DateStart),
                DateEnd = DateUtils.storageToUi(it.DateEnd),
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version,
                UserID = it.UserID,
                CustomerID = it.CustomerID,
                EquipmentID = it.EquipmentID
            )
        }
    }
}
