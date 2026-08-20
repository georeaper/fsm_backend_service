package com.example.feature.model.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.model.dto.EditModelResponse
import com.example.feature.model.dto.ModelResponse
import com.example.models.api.ModelAsset
import com.example.models.databaseModels.modelAssetTable
import com.example.models.databaseModels.settingsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ModelRepositoryImpl (
private val dbProvider: DatabaseProvider
) : ModelRepository {
    override fun save(
        ctx: RequestContext,
        data: ModelAsset
    ): ModelAsset {
        val db=dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            modelAssetTable.insert {

                it[modelId] = data.ModelID
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

    override fun findAll(ctx: RequestContext): List<ModelResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            modelAssetTable.selectAll().map {
                ModelResponse(
                    ModelID = it[modelAssetTable.modelId],
                    RemoteID = it[modelAssetTable.remoteId],
                    Name = it[modelAssetTable.name],
                    Style = it[modelAssetTable.style],
                    LastModified = it[modelAssetTable.lastModified],
                    DateCreated = it[modelAssetTable.dateCreated],
                    Version = it[modelAssetTable.version]
                )
            }
        }
    }

    override fun edit(
        ctx: RequestContext,
        data: EditModelResponse
    ): EditModelResponse {
        val db=dbProvider.getDatabase(ctx.dbName)

        val model = EditModelResponse(
            ModelID = data.ModelID,
            RemoteID = data.RemoteID,
            Name = data.Name,
            Style = data.Style,
            LastModified = data.LastModified,
            DateCreated = data.DateCreated,
            Version = data.Version

        )
        transaction(db) {
            modelAssetTable.update({ modelAssetTable.modelId eq model.ModelID }) {

                it[modelId] = model.ModelID
                it[remoteId] = model.RemoteID
                it[name] = model.Name
                it[style] = model.Style
                it[lastModified] = model.LastModified
                it[dateCreated] = model.DateCreated
                it[version] = model.Version

            }
        }
        return model
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val deleted = modelAssetTable.deleteWhere { modelAssetTable.modelId eq id }
            deleted > 0
        }
    }

}
