package com.example.feature.fieldreport.repository

import com.example.core.DatabaseProvider
import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.FieldReportEquipment
import com.example.models.databaseModels.fieldReportCheckformTable
import com.example.models.databaseModels.fieldReportEquipmentTable
import com.example.models.databaseModels.fieldReportsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class FieldReportEquipmentRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : FieldReportEquipmentRepository {

    override fun save(
        ctx: RequestContext,
        fieldReportId: String,
        data: FieldReportEquipment
    ): FieldReportEquipment? {
        val db = dbProvider.getDatabase(ctx.dbName)
        val id = data.FieldReportEquipmentID?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val now = DateUtils.nowStorage()

        return transaction(db) {
            val fieldReportExists = fieldReportsTable.select {
                (fieldReportsTable.fieldReportId eq fieldReportId) and
                    (fieldReportsTable.isDeleted eq false)
            }.limit(1).any()

            if (!fieldReportExists) return@transaction null

            fieldReportEquipmentTable.insert {
                it[fieldReportEquipmentID] = id
                it[remoteID] = data.RemoteID
                it[completedStatus] = data.CompletedStatus
                it[lastModified] = data.LastModified?.takeIf(String::isNotBlank) ?: now
                it[dateCreated] = data.DateCreated?.takeIf(String::isNotBlank) ?: now
                it[version] = data.Version?.takeIf(String::isNotBlank) ?: "1"
                it[isDeleted] = false
                it[fieldReportID] = fieldReportId
                it[equipmentID] = data.EquipmentID?.takeIf(String::isNotBlank)
                it[maintenanceID] = data.MaintenanceID?.takeIf(String::isNotBlank)
            }

            data.copy(
                FieldReportEquipmentID = id,
                FieldReportID = fieldReportId,
                LastModified = data.LastModified?.takeIf(String::isNotBlank) ?: now,
                DateCreated = data.DateCreated?.takeIf(String::isNotBlank) ?: now,
                Version = data.Version?.takeIf(String::isNotBlank) ?: "1",
                isDeleted = false
            )
        }
    }

    override fun update(
        ctx: RequestContext,
        fieldReportId: String,
        fieldReportEquipmentId: String,
        data: FieldReportEquipment
    ): FieldReportEquipment? {
        val db = dbProvider.getDatabase(ctx.dbName)
        val now = DateUtils.nowStorage()

        return transaction(db) {
            val updated = fieldReportEquipmentTable.update({
                (fieldReportEquipmentTable.fieldReportEquipmentID eq fieldReportEquipmentId) and
                    (fieldReportEquipmentTable.fieldReportID eq fieldReportId) and
                    (fieldReportEquipmentTable.isDeleted eq false)
            }) {
                it[remoteID] = data.RemoteID
                it[completedStatus] = data.CompletedStatus
                it[lastModified] = now
                it[version] = data.Version?.takeIf(String::isNotBlank) ?: "1"
                it[equipmentID] = data.EquipmentID?.takeIf(String::isNotBlank)
                it[maintenanceID] = data.MaintenanceID?.takeIf(String::isNotBlank)
            } > 0

            if (!updated) null else data.copy(
                FieldReportEquipmentID = fieldReportEquipmentId,
                FieldReportID = fieldReportId,
                LastModified = now,
                Version = data.Version?.takeIf(String::isNotBlank) ?: "1",
                isDeleted = false
            )
        }
    }

    override fun delete(ctx: RequestContext, fieldReportId: String, fieldReportEquipmentId: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        val now = DateUtils.nowStorage()

        return transaction(db) {
            val deleted = fieldReportEquipmentTable.update({
                (fieldReportEquipmentTable.fieldReportEquipmentID eq fieldReportEquipmentId) and
                    (fieldReportEquipmentTable.fieldReportID eq fieldReportId) and
                    (fieldReportEquipmentTable.isDeleted eq false)
            }) {
                it[isDeleted] = true
                it[lastModified] = now
            } > 0

            if (deleted) {
                fieldReportCheckformTable.update({
                    (fieldReportCheckformTable.fieldReportEquipmentId eq fieldReportEquipmentId) and
                        (fieldReportCheckformTable.isDeleted eq false)
                }) {
                    it[isDeleted] = true
                    it[lastModified] = now
                }
            }
            deleted
        }
    }
}
