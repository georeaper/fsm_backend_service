package com.example.feature.user.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.user.dto.EditUserResponse
import com.example.feature.user.dto.UsersResponse
import com.example.models.api.Users
import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl(private val dbProvider: DatabaseProvider) : UserRepository {
    override fun save(ctx: RequestContext, data: Users): Users {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            userTable.insert {
                it[userId] = data.UserID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description
                it[email] = data.Email
                it[phone] = data.Phone
                it[signature] = data.Signature
                it[reportPrefix] = data.ReportPrefix
                it[technicalCasePrefix] = data.TechnicalCasePrefix
                it[lastReportNumber] = data.LastReportNumber
                it[lastTCNumber] = data.LastTCNumber
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<UsersResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = userTable.selectAll().map {
                UsersResponse(
                    UserID = it[userTable.userId],
                    RemoteID = it[userTable.remoteId],
                    Name = it[userTable.name],
                    Description = it[userTable.description],
                    Email = it[userTable.email],
                    Phone = it[userTable.phone],
                    ReportPrefix = it[userTable.reportPrefix],
                    TechnicalCasePrefix = it[userTable.technicalCasePrefix],
                    LastReportNumber = it[userTable.lastReportNumber],
                    LastTCNumber = it[userTable.lastTCNumber],
                    LastModified = it[userTable.lastModified],
                    DateCreated = it[userTable.dateCreated],
                    Version = it[userTable.version]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditUserResponse): EditUserResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            userTable.update({ userTable.userId eq data.UserID }) {
                it[userId] = data.UserID
                it[remoteId] = null
                it[name] = data.Name
                it[description] = data.Description
                it[email] = data.Email
                it[phone] = data.Phone
                it[reportPrefix] = data.ReportPrefix
                it[technicalCasePrefix] = data.TechnicalCasePrefix
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated 
                it[version] = data.Version
            }
        }
        return data
    }
}
