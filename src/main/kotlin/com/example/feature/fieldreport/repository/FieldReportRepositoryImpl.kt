package com.example.feature.fieldreport.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.EditFieldReportResponse
import com.example.feature.fieldreport.dto.FieldReportsResponse
import com.example.models.api.FieldReports
import com.example.models.databaseModels.fieldReportsTable
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.contractsTable
import com.example.models.databaseModels.userTable
import com.example.models.databaseModels.ticketTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class FieldReportRepositoryImpl(private val dbProvider: DatabaseProvider) : FieldReportRepository {
    override fun save(ctx: RequestContext, data: FieldReports): FieldReports {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            fieldReportsTable.insert {
                it[fieldReportId] = data.FieldReportID
                it[remoteID] = data.RemoteID
                it[reportNumber] = data.ReportNumber
                it[description] = data.Description
                it[startDate] = data.StartDate
                it[endDate] = data.EndDate
                it[title] = data.Title
                it[department] = data.Department
                it[clientName] = data.ClientName
                it[reportStatus] = data.ReportStatus
                it[clientSignature] = data.ClientSignature
                it[value] = data.Value
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[customerID] = data.CustomerID
                it[contractID] = data.ContractID
                it[userID] = data.UserID
                it[caseID] = data.CaseID
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<FieldReportsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
//            val joinQuery = fieldReportsTable
//                .leftJoin(customerTable)
//                .leftJoin(contractsTable )
//                .leftJoin(userTable)
//                .leftJoin(ticketTable)

            val joinQuery = fieldReportsTable
                .join(
                    customerTable,
                    JoinType.LEFT,
                    additionalConstraint = {
                        fieldReportsTable.customerID eq customerTable.customerId
                    }
                )
                .join(
                    contractsTable,
                    JoinType.LEFT,
                    additionalConstraint = {
                        fieldReportsTable.contractID eq contractsTable.contractId
                    }
                )
                .join(
                    userTable,
                    JoinType.LEFT,
                    additionalConstraint = {
                        fieldReportsTable.userID eq userTable.userId
                    }
                )
                .join(
                    ticketTable,
                    JoinType.LEFT,
                    additionalConstraint = {
                        fieldReportsTable.caseID eq ticketTable.ticketId
                    }
                )
            val map = joinQuery.selectAll().map {
                FieldReportsResponse(
                    FieldReportID = it[fieldReportsTable.fieldReportId],
                    RemoteID = it[fieldReportsTable.remoteID],
                    ReportNumber = it[fieldReportsTable.reportNumber],
                    Description = it[fieldReportsTable.description],
                    StartDate = it[fieldReportsTable.startDate],
                    EndDate = it[fieldReportsTable.endDate],
                    Title = it[fieldReportsTable.title],
                    Department = it[fieldReportsTable.department],
                    ClientName = it[fieldReportsTable.clientName],
                    ReportStatus = it[fieldReportsTable.reportStatus],
                    Value = it[fieldReportsTable.value],
                    LastModified = it[fieldReportsTable.lastModified],
                    DateCreated = it[fieldReportsTable.dateCreated],
                    Version = it[fieldReportsTable.version],
                    CustomerID = it[fieldReportsTable.customerID],
                    ContractID = it[fieldReportsTable.contractID],
                    UserID = it[fieldReportsTable.userID],
                    CaseID = it[fieldReportsTable.caseID]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditFieldReportResponse): EditFieldReportResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            fieldReportsTable.update({ fieldReportsTable.fieldReportId eq data.FieldReportID }) {
                it[fieldReportId] = data.FieldReportID
                it[remoteID] = null
                it[reportNumber] = data.ReportNumber
                it[description] = data.Description
                it[startDate] = data.StartDate
                it[endDate] = data.EndDate
                it[title] = data.Title
                it[department] = data.Department
                it[clientName] = data.ClientName
                it[reportStatus] = data.ReportStatus
                it[value] = data.Value
                it[lastModified] = data.LastModified!!  
                it[dateCreated] = data.DateCreated!!  
                it[version] = data.Version!!
                it[customerID] = data.CustomerID
                it[contractID] = data.ContractID
                it[userID] = data.UserID
                it[caseID] = data.CaseID
            }
        }
        return data
    }
}
