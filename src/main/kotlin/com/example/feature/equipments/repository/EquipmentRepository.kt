package com.example.feature.equipments.repository

import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse
import com.example.feature.equipments.dto.EquipmentsResponse
import com.example.models.api.Equipments

data class EquipmentKnowledgeBaseRow(
    val equipmentId: String, val name: String?, val serialNumber: String?, val model: String?,
    val manufacturer: String?, val description: String?, val version: String?, val category: String?,
    val warranty: String?, val active: Boolean?, val installationDate: String?, val customerId: String?,
    val customerName: String?, val customerLocation: String?
)
data class EquipmentTechnicalCaseRow(
    val id: String, val equipmentId: String?, val number: String?, val title: String?, val type: String?,
    val status: String?, val priority: String?, val active: Boolean?, val openedAt: String?, val dueAt: String?
)
data class EquipmentServiceRow(
    val equipmentId: String?, val workOrderId: String, val number: String?, val title: String?,
    val type: String?, val status: String?, val description: String?, val startedAt: String?,
    val completedAt: String?, val scheduledStart: String?, val scheduledEnd: String?,
    val maintenanceId: String?, val maintenanceName: String?
)
data class EquipmentKnowledgeBaseData(
    val equipment: List<EquipmentKnowledgeBaseRow>,
    val technicalCases: List<EquipmentTechnicalCaseRow>,
    val services: List<EquipmentServiceRow>
)

interface EquipmentRepository {
    fun save(ctx: RequestContext, data: Equipments): Equipments
    fun findAll(ctx: RequestContext): List<EquipmentsResponse>
    fun edit(ctx: RequestContext, data: EditEquipmentResponse): EditEquipmentResponse
    fun update(ctx: RequestContext, data: Equipments): Boolean
    fun delete(ctx: RequestContext, id: String): Boolean
    fun knowledgeBaseData(ctx: RequestContext): EquipmentKnowledgeBaseData
}
