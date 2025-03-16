package com.example.models.databaseModels
import com.example.models.databaseModels.ticketTable.nullable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object contractEquipmentsTable : Table("ContractEquipmentsTable") {
    val contractEquipmentId = varchar("ContractEquipmentId",36).default(java.util.UUID.randomUUID().toString())
    val remoteId = integer("RemoteId").nullable()
    val value = double("Value").nullable()
    val visits = double("Visits").nullable()
    val contractId = varchar("ContractId", 36).references(contractsTable.contractId).nullable()
    val equipmentId = varchar("EquipmentId", 36).references(equipmentTable.equipmentId).nullable()
    val lastModified = varchar("LastModified", 50).nullable()
    val dateCreated = varchar("DateCreated", 50).nullable()
    val version = varchar("Version", 20).nullable()
    override val primaryKey = PrimaryKey(contractEquipmentId)
}