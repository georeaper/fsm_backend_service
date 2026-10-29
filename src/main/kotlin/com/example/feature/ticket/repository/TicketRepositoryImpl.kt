package com.example.feature.ticket.repository

import org.jetbrains.exposed.sql.SortOrder

import com.example.core.DatabaseProvider
import com.example.core.DocumentNumberGenerator
import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.ticket.dto.EditTicketResponse
import com.example.feature.ticket.dto.TicketsResponse
import com.example.models.api.Tickets
import com.example.models.databaseModels.ticketTable
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import com.example.models.databaseModels.userTable
import com.example.models.databaseModels.settingsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class TicketRepositoryImpl(private val dbProvider: DatabaseProvider) : TicketRepository {
    override fun save(ctx: RequestContext, data: Tickets): Tickets {
        val db = dbProvider.getDatabase(ctx.dbName)
        var generatedTicketNumber: String? = null
        transaction(db) {
            generatedTicketNumber = DocumentNumberGenerator.nextTechnicalCaseNumber(ctx)
            ticketTable.insert {
                it[ticketId] = data.TicketID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = generatedTicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                data.Status?.let { value -> it[status] = value }
                data.Type?.let { value -> it[caseType] = value }
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
        return data.copy(TicketNumber = generatedTicketNumber)
    }

    override fun findAll(ctx: RequestContext, customerId: String?, openOnly: Boolean): List<TicketsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val joinQuery = ticketTable
                .leftJoin(customerTable)
                .leftJoin(equipmentTable)
                .leftJoin(userTable)
            val openStatusValues = if (openOnly) settingsTable
                .slice(settingsTable.settingsValue)
                .select {
                    (settingsTable.settingsKey eq "TechnicalCaseStatus") and
                        (settingsTable.systemCategory eq "OPEN") and
                        (settingsTable.isDeleted eq false)
                }.mapNotNull { it[settingsTable.settingsValue]?.trim() }
            else emptyList()
            val customerCondition = (ticketTable.isDeleted eq false).let { base ->
                customerId?.takeIf(String::isNotBlank)?.let { base and (ticketTable.customerId eq it) } ?: base
            }
            val condition = when {
                !openOnly -> customerCondition
                openStatusValues.isEmpty() -> customerCondition and (ticketTable.ticketId eq "__no_open_status_configured__")
                else -> customerCondition and (ticketTable.status inList openStatusValues)
            }
            val map = joinQuery.select { condition }
                .orderBy(ticketTable.dateCreated to SortOrder.DESC, ticketTable.ticketId to SortOrder.ASC)
                .map {
                TicketsResponse(
                    TicketID = it[ticketTable.ticketId],
                    RemoteID = it[ticketTable.remoteId],
                    Title = it[ticketTable.title],
                    TicketNumber = it[ticketTable.ticketNumber],
                    Description = it[ticketTable.description],
                    Notes = it[ticketTable.notes],
                    Urgency = it[ticketTable.urgency],
                    Status = it[ticketTable.status],
                    Type = it[ticketTable.caseType],
                    Active = it[ticketTable.active],
                    DateStart = it[ticketTable.dateStart],
                    DateEnd = it[ticketTable.dateEnd],
                    LastModified = it[ticketTable.lastModified],
                    DateCreated = it[ticketTable.dateCreated],
                    Version = it[ticketTable.version],
                    UserID = it[ticketTable.userId],
                    CustomerID = it[ticketTable.customerId],
                    CustomerName = it[customerTable.name],
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
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                data.Status?.let { value -> it[status] = value }
                data.Type?.let { value -> it[caseType] = value }
                it[active] = data.Active
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[lastModified] = DateUtils.nowStorage()
                data.DateCreated?.let { value -> it[dateCreated] = value }
                data.Version?.let { value -> it[version] = value }
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
