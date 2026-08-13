package com.example.feature.manufacturer.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.manufacturer.dto.EditManufacturerResponse
import com.example.feature.manufacturer.dto.ManufacturerResponse
import com.example.models.api.Manufacturer
import com.example.models.databaseModels.manufacturerTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ManufacturerRepositoryImpl (
private val dbProvider: DatabaseProvider
) : ManufacturerRepository {
    override fun save(
        ctx: RequestContext,
        data: Manufacturer
    ): Manufacturer {
        val db=dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            manufacturerTable.insert {

                it[manufacturerId] = data.ManufacturerID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version

            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<ManufacturerResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            manufacturerTable.selectAll().map {
                ManufacturerResponse(
                    ManufacturerID = it[manufacturerTable.manufacturerId],
                    RemoteID = it[manufacturerTable.remoteId],
                    Name = it[manufacturerTable.name],
                    Style = it[manufacturerTable.style],
                    LastModified = it[manufacturerTable.lastModified],
                    DateCreated = it[manufacturerTable.dateCreated],
                    Version = it[manufacturerTable.version]
                )
            }
        }
    }

    override fun edit(
        ctx: RequestContext,
        data: EditManufacturerResponse
    ): EditManufacturerResponse {
        val db=dbProvider.getDatabase(ctx.dbName)

        val manufacturer = EditManufacturerResponse(
            ManufacturerID = data.ManufacturerID,
            RemoteID = data.RemoteID,
            Name = data.Name,
            Style = data.Style,
            LastModified = data.LastModified,
            DateCreated = data.DateCreated,
            Version = data.Version

        )
        transaction(db) {
            manufacturerTable.update({ manufacturerTable.manufacturerId eq manufacturer.ManufacturerID }) {

                it[manufacturerId] = manufacturer.ManufacturerID
                it[remoteId] = manufacturer.RemoteID
                it[name] = manufacturer.Name
                it[style] = manufacturer.Style
                it[lastModified] = manufacturer.LastModified
                it[dateCreated] = manufacturer.DateCreated
                it[version] = manufacturer.Version

            }
        }
        return manufacturer
    }

}
