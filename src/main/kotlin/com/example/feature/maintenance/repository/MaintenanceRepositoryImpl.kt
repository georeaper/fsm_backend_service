package com.example.feature.maintenance.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.maintenance.dto.MaintenanceWithCheckFormsResponse
import com.example.feature.maintenance.dto.MaintenanceDetailsResponse
import com.example.feature.maintenance.dto.CheckFormDetailsResponse
import com.example.feature.maintenance.dto.MaintenancesResponse
import com.example.models.api.CheckForms
import com.example.models.api.Maintenances
import com.example.models.databaseModels.checkFormsTable
import com.example.models.databaseModels.maintenancesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and

class MaintenanceRepositoryImpl(private val dbProvider: DatabaseProvider) : MaintenanceRepository {
    override fun saveWithCheckForms(
        ctx: RequestContext,
        maintenance: Maintenances,
        checkForms: List<CheckForms>
    ): MaintenanceWithCheckFormsResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            maintenancesTable.insert {
                it[maintenanceId] = maintenance.MaintenanceID
                it[remoteID] = maintenance.RemoteID
                it[name] = maintenance.Name
                it[description] = maintenance.Description
                it[lastModified] = maintenance.LastModified
                it[dateCreated] = maintenance.DateCreated
                it[version] = maintenance.Version
            }

            checkForms.forEach { checkForm ->
                checkFormsTable.insert {
                    it[checkFormId] = checkForm.CheckFormID
                    it[remoteId] = checkForm.RemoteID
                    it[maintenancesId] = maintenance.MaintenanceID
                    it[description] = checkForm.Description
                    it[valueExpected] = checkForm.ValueExpected
                    it[valueType] = checkForm.ValueType
                    it[lastModified] = checkForm.LastModified
                    it[dateCreated] = checkForm.DateCreated
                    it[version] = checkForm.Version
                }
            }
        }
        return MaintenanceWithCheckFormsResponse(
            Maintenance = maintenance,
            CheckForms = checkForms
        )
    }

    override fun findAll(ctx: RequestContext): List<MaintenancesResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = maintenancesTable.select { maintenancesTable.isDeleted eq false }.map {
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

    override fun findByIdWithCheckForms(
        ctx: RequestContext,
        maintenanceId: String
    ): MaintenanceDetailsResponse? {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val maintenance = maintenancesTable
                .select {
                    (maintenancesTable.maintenanceId eq maintenanceId) and
                        (maintenancesTable.isDeleted eq false)
                }
                .singleOrNull()
                ?: return@transaction null

            val checkForms = checkFormsTable
                .select {
                    (checkFormsTable.maintenancesId eq maintenanceId) and
                        (checkFormsTable.isDeleted eq false)
                }
                .map {
                    CheckFormDetailsResponse(
                        CheckFormID = it[checkFormsTable.checkFormId],
                        Description = it[checkFormsTable.description],
                        ValueExpected = it[checkFormsTable.valueExpected],
                        ValueType = it[checkFormsTable.valueType]
                    )
                }

            MaintenanceDetailsResponse(
                MaintenanceID = maintenance[maintenancesTable.maintenanceId],
                Name = maintenance[maintenancesTable.name],
                Description = maintenance[maintenancesTable.description],
                CheckForms = checkForms
            )
        }
    }

    override fun updateWithCheckForms(
        ctx: RequestContext,
        maintenance: Maintenances,
        checkForms: List<CheckForms>
    ): MaintenanceWithCheckFormsResponse? {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val updated = maintenancesTable.update({
                (maintenancesTable.maintenanceId eq maintenance.MaintenanceID) and
                    (maintenancesTable.isDeleted eq false)
            }) {
                it[name] = maintenance.Name
                it[description] = maintenance.Description
                it[lastModified] = maintenance.LastModified
                it[version] = maintenance.Version
            }

            if (updated == 0) return@transaction null

            val existingIds = checkFormsTable
                .select { checkFormsTable.maintenancesId eq maintenance.MaintenanceID }
                .map { it[checkFormsTable.checkFormId] }
                .toSet()
            val requestedIds = checkForms.map { it.CheckFormID }.toSet()

            existingIds.subtract(requestedIds).forEach { removedId ->
                checkFormsTable.update({ checkFormsTable.checkFormId eq removedId }) {
                    it[isDeleted] = true
                    it[lastModified] = maintenance.LastModified
                }
            }

            checkForms.forEach { checkForm ->
                if (checkForm.CheckFormID in existingIds) {
                    checkFormsTable.update({
                        (checkFormsTable.checkFormId eq checkForm.CheckFormID) and
                            (checkFormsTable.maintenancesId eq maintenance.MaintenanceID)
                    }) {
                        it[description] = checkForm.Description
                        it[valueExpected] = checkForm.ValueExpected
                        it[valueType] = checkForm.ValueType
                        it[lastModified] = checkForm.LastModified
                        it[version] = checkForm.Version
                        it[isDeleted] = false
                    }
                } else {
                    checkFormsTable.insert {
                        it[checkFormId] = checkForm.CheckFormID
                        it[remoteId] = checkForm.RemoteID
                        it[maintenancesId] = maintenance.MaintenanceID
                        it[description] = checkForm.Description
                        it[valueExpected] = checkForm.ValueExpected
                        it[valueType] = checkForm.ValueType
                        it[lastModified] = checkForm.LastModified
                        it[dateCreated] = checkForm.DateCreated
                        it[version] = checkForm.Version
                    }
                }
            }

            val savedMaintenance = maintenancesTable
                .select { maintenancesTable.maintenanceId eq maintenance.MaintenanceID }
                .single()
                .let {
                    Maintenances(
                        MaintenanceID = it[maintenancesTable.maintenanceId],
                        RemoteID = it[maintenancesTable.remoteID],
                        Name = it[maintenancesTable.name],
                        Description = it[maintenancesTable.description],
                        LastModified = it[maintenancesTable.lastModified],
                        DateCreated = it[maintenancesTable.dateCreated],
                        Version = it[maintenancesTable.version]
                    )
                }
            val savedCheckForms = checkFormsTable
                .select {
                    (checkFormsTable.maintenancesId eq maintenance.MaintenanceID) and
                        (checkFormsTable.isDeleted eq false)
                }
                .map {
                    CheckForms(
                        CheckFormID = it[checkFormsTable.checkFormId],
                        RemoteID = it[checkFormsTable.remoteId],
                        MaintenancesID = it[checkFormsTable.maintenancesId],
                        Description = it[checkFormsTable.description],
                        ValueExpected = it[checkFormsTable.valueExpected],
                        ValueType = it[checkFormsTable.valueType],
                        LastModified = it[checkFormsTable.lastModified],
                        DateCreated = it[checkFormsTable.dateCreated],
                        Version = it[checkFormsTable.version]
                    )
                }

            MaintenanceWithCheckFormsResponse(
                Maintenance = savedMaintenance,
                CheckForms = savedCheckForms
            )
        }
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val deleted = maintenancesTable.update({
                (maintenancesTable.maintenanceId eq id) and (maintenancesTable.isDeleted eq false)
            }) { it[isDeleted] = true }

            if (deleted > 0) {
                checkFormsTable.update({
                    (checkFormsTable.maintenancesId eq id) and (checkFormsTable.isDeleted eq false)
                }) { it[isDeleted] = true }
            }

            deleted > 0
        }
    }
}
