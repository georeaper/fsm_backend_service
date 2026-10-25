package com.example.feature.settings.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.settings.dto.EditSettingsResponse
import com.example.feature.settings.dto.SettingsResponse
import com.example.models.api.Settings
import com.example.models.databaseModels.settingsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SettingsRepositoryImpl(private val dbProvider: DatabaseProvider) : SettingsRepository {
    override fun save(ctx: RequestContext, data: Settings): Settings {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            settingsTable.insert {
                it[settingsId] = data.SettingsID
                it[remoteId] = data.RemoteID
                it[settingsKey] = data.SettingsKey
                it[settingsValue] = data.SettingsValue
                it[settingsStyle] = data.SettingsStyle
                it[settingsDescription] = data.SettingsDescription
                it[systemCategory] = data.SystemCategory
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<SettingsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = settingsTable.select { settingsTable.isDeleted eq false }.map {
                SettingsResponse(
                    SettingsID = it[settingsTable.settingsId],
                    RemoteID = it[settingsTable.remoteId],
                    SettingsKey = it[settingsTable.settingsKey],
                    SettingsValue = it[settingsTable.settingsValue],
                    SettingsStyle = it[settingsTable.settingsStyle],
                    SettingsDescription = it[settingsTable.settingsDescription],
                    SystemCategory = it[settingsTable.systemCategory],
                    LastModified = it[settingsTable.lastModified],
                    DateCreated = it[settingsTable.dateCreated],
                    Version = it[settingsTable.version]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditSettingsResponse): EditSettingsResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            settingsTable.update({
                (settingsTable.settingsId eq data.SettingsID) and
                    (settingsTable.isDeleted eq false)
            }) {
                it[settingsId] = data.SettingsID
                it[remoteId] = null
                it[settingsKey] = data.SettingsKey
                it[settingsValue] = data.SettingsValue
                it[settingsStyle] = data.SettingsStyle
                it[settingsDescription] = data.SettingsDescription
                it[systemCategory] = data.SystemCategory
                it[lastModified] = data.LastModified!!
                it[dateCreated] = data.DateCreated!!
                it[version] = data.Version!!
            }
        }
        return data
    }

    override fun getById(
        ctx: RequestContext,
        settingsId: String
    ): List<SettingsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            val query = settingsTable.select {
                (settingsTable.settingsKey eq settingsId) and
                    (settingsTable.isDeleted eq false)
            }
            println("getById : $query")
            val map = query.map {
                SettingsResponse(
                    SettingsID = it[settingsTable.settingsId],
                    RemoteID = it[settingsTable.remoteId],
                    SettingsKey = it[settingsTable.settingsKey],
                    SettingsValue = it[settingsTable.settingsValue],
                    SettingsStyle = it[settingsTable.settingsStyle],
                    SettingsDescription = it[settingsTable.settingsDescription],
                    SystemCategory = it[settingsTable.systemCategory],
                    LastModified = it[settingsTable.lastModified],
                    DateCreated = it[settingsTable.dateCreated],
                    Version = it[settingsTable.version]
                )
            }
            map
        }
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            settingsTable.update({
                (settingsTable.settingsId eq id) and
                    (settingsTable.isDeleted eq false)
            }) {
                it[isDeleted] = true
            } > 0
        }
    }
}
