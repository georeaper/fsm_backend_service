package com.example.feature.equipments

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse
import com.example.feature.equipments.dto.EquipmentsResponse
import com.example.models.api.Equipments
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class EquipmentRepositoryImpl (
private val dbProvider: DatabaseProvider
) : EquipmentRepository {
    override fun save(
        ctx: RequestContext,
        data: Equipments
    ): Equipments {
        val db=dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            equipmentTable.insert{

                    it[equipmentId]=data.EquipmentID
                    it[remoteId] = data.RemoteID
                    it[ name] =data.Name
                    it[ serialNumber] =data.SerialNumber
                    it[ model] =data.Model
                    it[ manufacturer] =data.Manufacturer
                    it[ notes] =data.Notes
                    it[ description] =data.Description
                    it[ equipmentVersion] =data.EquipmentVersion
                    it[ equipmentCategory] =data.EquipmentCategory
                    it[ warranty] =data.Warranty
                    it[ equipmentStatus] =data.EquipmentStatus
                    it[ installationDate] =data.InstallationDate
                    it[ lastModified] =data.LastModified
                    it[ dateCreated] =data.DateCreated
                    it[version] =data.Version
                    it[ customerId ]= data.CustomerID

            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<EquipmentsResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val map = equipmentTable.selectAll().map {
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
                    CustomerID = it[equipmentTable.customerId]
                )
            }
            map
        }
    }

    override fun edit(
        ctx: RequestContext,
        data: Equipments
    ): EditEquipmentResponse {
        TODO("Not yet implemented")
    }

}