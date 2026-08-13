package com.example.feature.inventory.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.inventory.dto.EditInventoryResponse
import com.example.feature.inventory.dto.InventoryResponse
import com.example.models.api.Inventory
import com.example.models.databaseModels.inventoryTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class InventoryRepositoryImpl (
private val dbProvider: DatabaseProvider
) : InventoryRepository {
    override fun save(
        ctx: RequestContext,
        data: Inventory
    ): Inventory {
        val db=dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            inventoryTable.insert {

                it[inventoryId] = data.InventoryID
                it[remoteID] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[quantity] = data.Quantity
                it[value] = data.Value
                it[type] = data.Type
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version

            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<InventoryResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            inventoryTable.selectAll().map {
                InventoryResponse(
                    InventoryID = it[inventoryTable.inventoryId],
                    RemoteID = it[inventoryTable.remoteID],
                    Title = it[inventoryTable.title],
                    Description = it[inventoryTable.description],
                    Quantity = it[inventoryTable.quantity],
                    Value = it[inventoryTable.value],
                    Type = it[inventoryTable.type],
                    LastModified = it[inventoryTable.lastModified],
                    DateCreated = it[inventoryTable.dateCreated],
                    Version = it[inventoryTable.version]
                )
            }
        }
    }

    override fun edit(
        ctx: RequestContext,
        data: EditInventoryResponse
    ): EditInventoryResponse {
        val db=dbProvider.getDatabase(ctx.dbName)

        val inventory = EditInventoryResponse(
            InventoryID = data.InventoryID,
            RemoteID = data.RemoteID,
            Title = data.Title,
            Description = data.Description,
            Quantity = data.Quantity,
            Value = data.Value,
            Type = data.Type,
            LastModified = data.LastModified,
            DateCreated = data.DateCreated,
            Version = data.Version

        )
        transaction(db) {
            inventoryTable.update({ inventoryTable.inventoryId eq inventory.InventoryID }) {

                it[inventoryId] = inventory.InventoryID
                it[remoteID] = inventory.RemoteID
                it[title] = inventory.Title
                it[description] = inventory.Description
                it[quantity] = inventory.Quantity
                it[value] = inventory.Value
                it[type] = inventory.Type
                it[lastModified] = inventory.LastModified
                it[dateCreated] = inventory.DateCreated
                it[version] = inventory.Version

            }
        }
        return inventory
    }

}
