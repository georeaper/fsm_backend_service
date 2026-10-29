package com.example.feature.equipments.dto

import kotlinx.serialization.Serializable

@Serializable data class EquipmentKnowledgeBaseError(val code: String, val message: String)
@Serializable data class EquipmentKnowledgeBaseListItem(
    val equipmentId: String,
    val code: String?,
    val serialNumber: String?,
    val name: String?,
    val manufacturer: String?,
    val model: String?,
    val customerId: String?,
    val customerName: String?,
    val lastServiceDate: String?,
    val openTechnicalCases: Int,
    val warranty: String?,
    val warrantyStatus: String,
    val status: String
)
@Serializable data class EquipmentKnowledgeBaseCustomerOption(val customerId: String, val customerName: String?)
@Serializable data class EquipmentKnowledgeBaseFilterOptions(
    val manufacturers: List<String>,
    val customers: List<EquipmentKnowledgeBaseCustomerOption>,
    val statuses: List<String> = listOf("ACTIVE", "INACTIVE"),
    val warrantyStatuses: List<String> = listOf("ACTIVE", "EXPIRED", "UNKNOWN"),
    val openTechnicalCaseOptions: List<String> = listOf("ANY", "WITH_OPEN", "WITHOUT_OPEN")
)
@Serializable data class EquipmentKnowledgeBaseTechnicalCase(
    val technicalCaseId: String,
    val number: String?,
    val title: String?,
    val type: String?,
    val status: String?,
    val priority: String?,
    val openedAt: String?,
    val dueAt: String?,
    val isOpen: Boolean
)
@Serializable data class EquipmentKnowledgeBaseService(
    val workOrderId: String,
    val number: String?,
    val title: String?,
    val type: String?,
    val status: String?,
    val description: String?,
    val startedAt: String?,
    val completedAt: String?,
    val scheduledStart: String?,
    val scheduledEnd: String?,
    val maintenanceId: String?,
    val maintenanceName: String?
)
@Serializable data class EquipmentKnowledgeBaseOverview(
    val equipmentId: String,
    val code: String?,
    val name: String?,
    val manufacturer: String?,
    val model: String?,
    val serialNumber: String?,
    val category: String?,
    val version: String?,
    val description: String?,
    val customerId: String?,
    val customerName: String?,
    val location: String?,
    val installationDate: String?,
    val warranty: String?,
    val warrantyStatus: String,
    val status: String,
    val lastServiceDate: String?,
    val nextScheduledService: String?,
    val openTechnicalCases: Int
)
@Serializable data class EquipmentKnowledgeBaseActivity(
    val activityType: String,
    val occurredAt: String?,
    val title: String?,
    val description: String?,
    val referenceId: String,
    val referenceNumber: String?,
    val status: String?
)
@Serializable data class EquipmentKnowledgeBaseDetail(
    val overview: EquipmentKnowledgeBaseOverview,
    val recentActivity: List<EquipmentKnowledgeBaseActivity>,
    val serviceHistory: List<EquipmentKnowledgeBaseService>,
    val technicalCases: List<EquipmentKnowledgeBaseTechnicalCase>,
    val maintenanceSchedule: List<EquipmentKnowledgeBaseService>
)
