package com.example.feature.equipments.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse
import com.example.feature.equipments.dto.EquipmentsResponse
import com.example.models.api.Equipments
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import com.example.models.databaseModels.fieldReportEquipmentTable
import com.example.models.databaseModels.fieldReportsTable
import com.example.models.databaseModels.maintenancesTable
import com.example.models.databaseModels.ticketTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class EquipmentRepositoryImpl (
private val dbProvider: DatabaseProvider
) : EquipmentRepository {
    override fun save(
        ctx: RequestContext,
        data: Equipments
    ): Equipments {
        val db=dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            equipmentTable.insert {

                it[equipmentId] = data.EquipmentID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[serialNumber] = data.SerialNumber
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[notes] = data.Notes
                it[description] = data.Description
                it[equipmentVersion] = data.EquipmentVersion
                it[equipmentCategory] = data.EquipmentCategory
                it[warranty] = data.Warranty
                it[equipmentStatus] = data.EquipmentStatus
                it[installationDate] = data.InstallationDate
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
                it[customerId] = data.CustomerID

            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<EquipmentsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val joinQuery = equipmentTable.leftJoin(customerTable)
            val map = joinQuery
                .select { equipmentTable.isDeleted eq false }
                .map {
                EquipmentsResponse(
                    EquipmentID = it[equipmentTable.equipmentId],
                    RemoteID = it[equipmentTable.remoteId],
                    Name = it[equipmentTable.name],
                    SerialNumber = it[equipmentTable.serialNumber],
                    Model = it[equipmentTable.model],
                    Manufacturer = it[equipmentTable.manufacturer],
                    Notes = it[equipmentTable.notes],
                    Description = it[equipmentTable.description],
                    EquipmentVersion = it[equipmentTable.equipmentVersion],
                    EquipmentCategory = it[equipmentTable.equipmentCategory],
                    Warranty = it[equipmentTable.warranty],
                    EquipmentStatus = it[equipmentTable.equipmentStatus],
                    InstallationDate = it[equipmentTable.installationDate],
                    LastModified = it[equipmentTable.lastModified],
                    DateCreated = it[equipmentTable.dateCreated],
                    Version = it[equipmentTable.version],
                    CustomerID = it[equipmentTable.customerId],
                    CustomerName = it[customerTable.name]
                )
            }
            map
        }
    }

    override fun edit(
        ctx: RequestContext,
        data: EditEquipmentResponse
    ): EditEquipmentResponse {
        val db=dbProvider.getDatabase(ctx.dbName)

        val eq= EditEquipmentResponse(
            EquipmentID = data.EquipmentID,
            RemoteID = data.RemoteID,
            Name = data.Name,
            SerialNumber = data.SerialNumber,
            Model = data.Model,
            Manufacturer = data.Manufacturer,
            Notes = data.Notes,
            Description = data.Description,
            EquipmentVersion = data.EquipmentVersion,
            EquipmentCategory = data.EquipmentCategory,
            Warranty = data.Warranty,
            EquipmentStatus = data.EquipmentStatus,
            InstallationDate = data.InstallationDate,
            LastModified = data.LastModified,
            DateCreated = data.DateCreated,
            Version = data.Version,
            CustomerID = data.CustomerID

        )
        transaction(db) {
            equipmentTable.update({ equipmentTable.equipmentId eq eq.EquipmentID }) {

                it[equipmentId] = eq.EquipmentID
                it[remoteId] = eq.RemoteID
                it[name] = eq.Name
                it[serialNumber] = eq.SerialNumber
                it[model] = eq.Model
                it[manufacturer] = eq.Manufacturer
                it[notes] = eq.Notes
                it[description] = eq.Description
                it[equipmentVersion] = eq.EquipmentVersion
                it[equipmentCategory] = eq.EquipmentCategory
                it[warranty] = eq.Warranty
                it[equipmentStatus] = eq.EquipmentStatus
                it[installationDate] = eq.InstallationDate
                it[lastModified] = eq.LastModified
                it[dateCreated] = eq.DateCreated
                it[version] = eq.Version
                it[customerId] = eq.CustomerID

            }
        }
        return eq
    }

    override fun update(ctx: RequestContext, data: Equipments): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            equipmentTable.update({
                (equipmentTable.equipmentId eq data.EquipmentID) and
                    (equipmentTable.isDeleted eq false)
            }) {
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[serialNumber] = data.SerialNumber
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[notes] = data.Notes
                it[description] = data.Description
                it[equipmentVersion] = data.EquipmentVersion
                it[equipmentCategory] = data.EquipmentCategory
                it[warranty] = data.Warranty
                it[equipmentStatus] = data.EquipmentStatus
                it[installationDate] = data.InstallationDate
                it[lastModified] = data.LastModified
                it[version] = data.Version
                it[customerId] = data.CustomerID
            } > 0
        }
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            equipmentTable.update({
                (equipmentTable.equipmentId eq id) and
                    (equipmentTable.isDeleted eq false)
            }) {
                it[isDeleted] = true
            } > 0
        }
    }

    override fun knowledgeBaseData(ctx: RequestContext): EquipmentKnowledgeBaseData =
        transaction(dbProvider.getDatabase(ctx.dbName)) {
            val equipment = equipmentTable.leftJoin(customerTable)
                .select { equipmentTable.isDeleted eq false }
                .map {
                    EquipmentKnowledgeBaseRow(
                        equipmentId = it[equipmentTable.equipmentId], name = it[equipmentTable.name],
                        serialNumber = it[equipmentTable.serialNumber], model = it[equipmentTable.model],
                        manufacturer = it[equipmentTable.manufacturer], description = it[equipmentTable.description],
                        version = it[equipmentTable.equipmentVersion], category = it[equipmentTable.equipmentCategory],
                        warranty = it[equipmentTable.warranty], active = it[equipmentTable.equipmentStatus],
                        installationDate = it[equipmentTable.installationDate], customerId = it[equipmentTable.customerId],
                        customerName = it[customerTable.name], customerLocation = it[customerTable.address]
                    )
                }
            val cases = ticketTable.select { ticketTable.isDeleted eq false }.map {
                EquipmentTechnicalCaseRow(
                    it[ticketTable.ticketId], it[ticketTable.equipmentId], it[ticketTable.ticketNumber],
                    it[ticketTable.title], it[ticketTable.caseType], it[ticketTable.status], it[ticketTable.urgency],
                    it[ticketTable.active], it[ticketTable.dateCreated], it[ticketTable.dateEnd]
                )
            }
            val services = fieldReportEquipmentTable
                .leftJoin(fieldReportsTable)
                .leftJoin(maintenancesTable)
                .select {
                    (fieldReportEquipmentTable.isDeleted eq false) and
                        (fieldReportsTable.isDeleted eq false)
                }
                .map {
                    EquipmentServiceRow(
                        equipmentId = it[fieldReportEquipmentTable.equipmentID],
                        workOrderId = it[fieldReportsTable.fieldReportId], number = it[fieldReportsTable.reportNumber],
                        title = it[fieldReportsTable.title], type = it[fieldReportsTable.workOrderType],
                        status = it[fieldReportsTable.reportStatus], description = it[fieldReportsTable.description],
                        startedAt = it[fieldReportsTable.startDate], completedAt = it[fieldReportsTable.endDate],
                        scheduledStart = it[fieldReportsTable.scheduledStart], scheduledEnd = it[fieldReportsTable.scheduledEnd],
                        maintenanceId = it[fieldReportEquipmentTable.maintenanceID],
                        maintenanceName = it[maintenancesTable.name]
                    )
                }
            EquipmentKnowledgeBaseData(equipment, cases, services)
        }

}
