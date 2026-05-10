package com.example.feature.maintenance.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.maintenance.dto.EditMaintenanceResponse
import com.example.feature.maintenance.dto.MaintenancesResponse
import com.example.models.api.Maintenances
import com.example.models.databaseModels.maintenancesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class MaintenanceRepositoryImpl(private val dbProvider: DatabaseProvider) : MaintenanceRepository {
    override fun save(ctx: RequestContext, data: Maintenances): Maintenances {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            maintenancesTable.insert {
                it[maintenanceId] = data.MaintenanceID
                it[remoteID] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<MaintenancesResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = maintenancesTable.selectAll().map {
                MaintenancesResponse(
                    MaintenanceID = it[maintenancesTable.maintenanceId],
                    RemoteID = it[maintenancesTable.remoteID],
                    Name = it[maintenancesTable.name],
                    Description = it[maintenancesTable.description],
                    LastModified = it[maintenancesTable.lastModified],
                    DateCreated = it[maintenancesTable.dateCreated],
                    Version = it[maintenancesTable.version]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditMaintenanceResponse): EditMaintenanceResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            maintenancesTable.update({ maintenancesTable.maintenanceId eq data.MaintenanceID }) {
                it[maintenanceId] = data.MaintenanceID
                it[remoteID] = null
                it[name] = data.Name
                it[description] = data.Description
                it[lastModified] = data.LastModified 
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }
}
