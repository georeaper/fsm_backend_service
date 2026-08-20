package com.example.feature.ticket.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.dto.TicketsResponse
import com.example.models.api.Tickets
import com.example.models.databaseModels.ticketTable
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and

class TicketRepositoryImpl(private val dbProvider: DatabaseProvider) : TicketRepository {
    override fun save(ctx: RequestContext, data: Tickets): Tickets {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            ticketTable.insert {
                it[ticketId] = data.TicketID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[active] = data.Active
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[userId] = data.UserID
                it[customerId] = data.CustomerID
                it[equipmentId] = data.EquipmentID
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<TicketsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val joinQuery = ticketTable
                .leftJoin(customerTable)
                .leftJoin(equipmentTable)
                .leftJoin(userTable)
            val map = joinQuery.select { ticketTable.isDeleted eq false }.map {
                TicketsResponse(
                    TicketID = it[ticketTable.ticketId],
                    RemoteID = it[ticketTable.remoteId],
                    Title = it[ticketTable.title],
                    TicketNumber = it[ticketTable.ticketNumber],
                    Description = it[ticketTable.description],
                    Notes = it[ticketTable.notes],
                    Urgency = it[ticketTable.urgency],
                    Active = it[ticketTable.active],
                    DateStart = it[ticketTable.dateStart],
                    DateEnd = it[ticketTable.dateEnd],
                    LastModified = it[ticketTable.lastModified],
                    DateCreated = it[ticketTable.dateCreated],
                    Version = it[ticketTable.version],
                    UserID = it[ticketTable.userId],
                    CustomerID = it[ticketTable.customerId],
                    EquipmentID = it[ticketTable.equipmentId]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditTicketResponse): EditTicketResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            ticketTable.update({
                (ticketTable.ticketId eq data.TicketID) and (ticketTable.isDeleted eq false)
            }) {
                it[ticketId] = data.TicketID
                it[remoteId] = null
                
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[active] = data.Active
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[lastModified] = data.LastModified 
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[userId] = data.UserID
                it[customerId] = data.CustomerID
                it[equipmentId] = data.EquipmentID
            }
        }
        return data
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            ticketTable.update({
                (ticketTable.ticketId eq id) and (ticketTable.isDeleted eq false)
            }) { it[isDeleted] = true } > 0
        }
    }
}
