package com.example.feature.tools.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.tools.dto.EditToolResponse
import com.example.feature.tools.dto.ToolsResponse
import com.example.models.api.Tools
import com.example.models.databaseModels.toolsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ToolRepositoryImpl(private val dbProvider: DatabaseProvider) : ToolRepository {
    override fun save(ctx: RequestContext, data: Tools): Tools {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            toolsTable.insert {
                it[toolsId] = data.ToolsID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[serialNumber] = data.SerialNumber
                it[calibrationDate] = data.CalibrationDate
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<ToolsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = toolsTable.selectAll().map {
                ToolsResponse(
                    ToolID = it[toolsTable.toolsId],
                    RemoteID = it[toolsTable.remoteId],
                    Title = it[toolsTable.title],
                    Description = it[toolsTable.description],
                    Model = it[toolsTable.model],
                    Manufacturer = it[toolsTable.manufacturer],
                    SerialNumber = it[toolsTable.serialNumber],
                    CalibrationDate = it[toolsTable.calibrationDate],
                    LastModified = it[toolsTable.lastModified],
                    DateCreated = it[toolsTable.dateCreated],
                    Version = it[toolsTable.version]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditToolResponse): EditToolResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            toolsTable.update({ toolsTable.toolsId eq data.ToolID }) {
                it[toolsId] = data.ToolID
                it[remoteId] = null
                it[title] = data.Title
                it[description] = data.Description
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[serialNumber] = data.SerialNumber
                it[calibrationDate] = data.CalibrationDate
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated 
                it[version] = data.Version
            }
        }
        return data
    }
}
