package com.example.feature.contracts.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.contracts.dto.EditContractResponse
import com.example.feature.contracts.dto.ContractsResponse
import com.example.models.api.Contracts
import com.example.models.databaseModels.contractsTable
import com.example.models.databaseModels.customerTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and

class ContractRepositoryImpl ( private val dbProvider: DatabaseProvider ): ContractRepository {
    override fun save(ctx: RequestContext, data: Contracts): Contracts {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            contractsTable.insert {
                it[contractId] = data.ContractID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[value] = data.Value
                it[notes] = data.Notes
                it[description] = data.Description
                it[contractType] = data.ContractType
                it[contractStatus] = data.ContractStatus
                it[contactName] = data.ContactName
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[customerId] = data.CustomerID
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<ContractsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
//            val joinQuery = contractsTable.leftJoin(customerTable)
            val joinQuery = contractsTable.join(
                customerTable,
                JoinType.LEFT,
                additionalConstraint = {
                    contractsTable.customerId eq customerTable.customerId
                }
            )
            val map = joinQuery.select { contractsTable.isDeleted eq false }.map {
                ContractsResponse(
                    ContractID = it[contractsTable.contractId],
                    RemoteID = it[contractsTable.remoteId],
                    Title = it[contractsTable.title],
                    DateStart = it[contractsTable.dateStart],
                    DateEnd = it[contractsTable.dateEnd],
                    Value = it[contractsTable.value],
                    Notes = it[contractsTable.notes],
                    Description = it[contractsTable.description],
                    ContractType = it[contractsTable.contractType],
                    ContractStatus = it[contractsTable.contractStatus],
                    ContactName = it[contractsTable.contactName],
                    LastModified = it[contractsTable.lastModified],
                    DateCreated = it[contractsTable.dateCreated],
                    Version = it[contractsTable.version],
                    CustomerID = it[contractsTable.customerId],
                    CustomerName = it[customerTable.name]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditContractResponse): EditContractResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            contractsTable.update({
                (contractsTable.contractId eq data.ContractID) and (contractsTable.isDeleted eq false)
            }) {
                it[contractId] = data.ContractID
                it[remoteId] = null
                it[title] = data.Title
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[value] = data.Value
                it[notes] = data.Notes
                it[description] = data.Description
                it[contractType] = data.ContractType
                it[contractStatus] = data.ContractStatus
                it[contactName] = data.ContactName
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[customerId] = data.CustomerID
            }
        }
        return data
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            contractsTable.update({
                (contractsTable.contractId eq id) and (contractsTable.isDeleted eq false)
            }) { it[isDeleted] = true } > 0
        }
    }
}
