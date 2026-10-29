package com.example.feature.equipments.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.feature.equipments.dto.EquipmentsResponse
import com.example.feature.equipments.dto.*
import com.example.feature.equipments.repository.EquipmentKnowledgeBaseData
import com.example.feature.equipments.repository.EquipmentServiceRow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class EquipmentKnowledgeBaseQuery(
    val search: String?, val manufacturer: String?, val customerIds: Set<String>,
    val status: String?, val warrantyStatus: String?, val openTechnicalCases: String?
)

class GetEquipmentUseCase (private val repository: EquipmentRepository){
    fun execute(ctx: RequestContext) :List<EquipmentsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            EquipmentsResponse(
                EquipmentID = it.EquipmentID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                SerialNumber = it.SerialNumber,
                Model = it.Model,
                Manufacturer = it.Manufacturer,
                Notes = it.Notes,
                Description = it.Description,
                EquipmentVersion = it.EquipmentVersion,
                EquipmentCategory = it.EquipmentCategory,
                Warranty = it.Warranty,
                EquipmentStatus = it.EquipmentStatus,
                InstallationDate = DateUtils.storageToUi(it.InstallationDate),
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version,
                CustomerID = it.CustomerID,
                CustomerName = it.CustomerName
            )
        }
    }

    fun knowledgeBase(ctx: RequestContext, query: EquipmentKnowledgeBaseQuery): List<EquipmentKnowledgeBaseListItem> {
        val data = repository.knowledgeBaseData(ctx)
        val casesByEquipment = data.technicalCases.filter { it.equipmentId != null }.groupBy { it.equipmentId!! }
        val servicesByEquipment = data.services.filter { it.equipmentId != null }.groupBy { it.equipmentId!! }
        return data.equipment.map { equipment ->
            val cases = casesByEquipment[equipment.equipmentId].orEmpty()
            val openCases = cases.count { technicalCaseOpen(it.status, it.active) }
            EquipmentKnowledgeBaseListItem(
                equipmentId = equipment.equipmentId,
                code = equipment.serialNumber,
                serialNumber = equipment.serialNumber,
                name = equipment.name,
                manufacturer = equipment.manufacturer,
                model = equipment.model,
                customerId = equipment.customerId,
                customerName = equipment.customerName,
                lastServiceDate = servicesByEquipment[equipment.equipmentId].orEmpty()
                    .mapNotNull { serviceDate(it) }.maxOrNull()?.toString(),
                openTechnicalCases = openCases,
                warranty = equipment.warranty,
                warrantyStatus = warrantyStatus(equipment.warranty),
                status = if (equipment.active == false) "INACTIVE" else "ACTIVE"
            )
        }.filter { item ->
            val search = query.search?.trim()?.lowercase().orEmpty()
            (search.isEmpty() || listOf(item.code, item.serialNumber, item.equipmentId, item.name, item.manufacturer, item.model, item.customerName)
                .any { it?.lowercase()?.contains(search) == true }) &&
                (query.manufacturer.isNullOrBlank() || item.manufacturer.equals(query.manufacturer, true)) &&
                (query.customerIds.isEmpty() || item.customerId in query.customerIds) &&
                (query.status.isNullOrBlank() || item.status.equals(query.status, true)) &&
                (query.warrantyStatus.isNullOrBlank() || item.warrantyStatus.equals(query.warrantyStatus, true)) &&
                when (query.openTechnicalCases?.uppercase()) {
                    null, "", "ANY" -> true
                    "WITH_OPEN" -> item.openTechnicalCases > 0
                    "WITHOUT_OPEN" -> item.openTechnicalCases == 0
                    else -> false
                }
        }.sortedWith(compareBy<EquipmentKnowledgeBaseListItem> { it.name?.lowercase() ?: "" }.thenBy { it.equipmentId })
    }

    fun knowledgeBaseFilters(ctx: RequestContext): EquipmentKnowledgeBaseFilterOptions {
        val equipment = repository.knowledgeBaseData(ctx).equipment
        return EquipmentKnowledgeBaseFilterOptions(
            manufacturers = equipment.mapNotNull { it.manufacturer?.trim() }.filter(String::isNotEmpty).distinctBy(String::lowercase).sortedBy(String::lowercase),
            customers = equipment.mapNotNull { row -> row.customerId?.let { EquipmentKnowledgeBaseCustomerOption(it, row.customerName) } }
                .distinctBy { it.customerId }
                .sortedWith(compareBy<EquipmentKnowledgeBaseCustomerOption> { it.customerName?.lowercase() ?: "" }.thenBy { it.customerId })
        )
    }

    fun knowledgeBaseDetail(ctx: RequestContext, equipmentId: String): EquipmentKnowledgeBaseDetail? {
        val data = repository.knowledgeBaseData(ctx)
        val equipment = data.equipment.firstOrNull { it.equipmentId == equipmentId } ?: return null
        val cases = data.technicalCases.filter { it.equipmentId == equipmentId }
            .sortedByDescending { parseDate(it.openedAt) ?: LocalDate.MIN }
            .map {
                EquipmentKnowledgeBaseTechnicalCase(
                    it.id, it.number, it.title, it.type, it.status, it.priority,
                    parseDate(it.openedAt)?.toString(), parseDate(it.dueAt)?.toString(),
                    technicalCaseOpen(it.status, it.active)
                )
            }
        val rawServices = data.services.filter { it.equipmentId == equipmentId }
        val services = rawServices.sortedByDescending { serviceDate(it) ?: LocalDate.MIN }.map(::serviceDto)
        val schedule = rawServices.filter { parseDate(it.scheduledStart) != null }
            .sortedBy { parseDate(it.scheduledStart) }.map(::serviceDto)
        val nextScheduled = rawServices.mapNotNull { parseDate(it.scheduledStart) }
            .filter { !it.isBefore(LocalDate.now()) }.minOrNull()?.toString()
        val activities = (
            rawServices.map {
                EquipmentKnowledgeBaseActivity(
                    activityType = "SERVICE", occurredAt = serviceDate(it)?.toString(), title = it.title,
                    description = it.description, referenceId = it.workOrderId, referenceNumber = it.number,
                    status = it.status
                )
            } + data.technicalCases.filter { it.equipmentId == equipmentId }.map {
                EquipmentKnowledgeBaseActivity(
                    activityType = "TECHNICAL_CASE", occurredAt = parseDate(it.openedAt)?.toString(), title = it.title,
                    description = it.type, referenceId = it.id, referenceNumber = it.number, status = it.status
                )
            }
        ).sortedByDescending { parseDate(it.occurredAt) ?: LocalDate.MIN }.take(20)
        return EquipmentKnowledgeBaseDetail(
            overview = EquipmentKnowledgeBaseOverview(
                equipmentId = equipment.equipmentId, code = equipment.serialNumber, name = equipment.name,
                manufacturer = equipment.manufacturer, model = equipment.model, serialNumber = equipment.serialNumber,
                category = equipment.category, version = equipment.version, description = equipment.description,
                customerId = equipment.customerId, customerName = equipment.customerName, location = equipment.customerLocation,
                installationDate = parseDate(equipment.installationDate)?.toString(), warranty = equipment.warranty,
                warrantyStatus = warrantyStatus(equipment.warranty), status = if (equipment.active == false) "INACTIVE" else "ACTIVE",
                lastServiceDate = rawServices.mapNotNull(::serviceDate).maxOrNull()?.toString(),
                nextScheduledService = nextScheduled, openTechnicalCases = cases.count { it.isOpen }
            ),
            recentActivity = activities,
            serviceHistory = services,
            technicalCases = cases,
            maintenanceSchedule = schedule
        )
    }

    private fun serviceDto(row: EquipmentServiceRow) = EquipmentKnowledgeBaseService(
        row.workOrderId, row.number, row.title, row.type, row.status, row.description,
        parseDate(row.startedAt)?.toString(), parseDate(row.completedAt)?.toString(),
        parseDate(row.scheduledStart)?.toString(), parseDate(row.scheduledEnd)?.toString(),
        row.maintenanceId, row.maintenanceName
    )

    private fun serviceDate(row: EquipmentServiceRow) = parseDate(row.completedAt)
        ?: parseDate(row.startedAt) ?: parseDate(row.scheduledStart)

    private fun warrantyStatus(value: String?): String {
        val normalized = value?.trim()?.lowercase().orEmpty()
        if (normalized in setOf("active", "valid", "in warranty")) return "ACTIVE"
        if (normalized in setOf("expired", "inactive", "out of warranty")) return "EXPIRED"
        val date = parseDate(value) ?: return "UNKNOWN"
        return if (date.isBefore(LocalDate.now())) "EXPIRED" else "ACTIVE"
    }

    private fun technicalCaseOpen(status: String?, active: Boolean?): Boolean {
        if (active == false) return false
        val normalized = status?.trim()?.lowercase()?.replace("_", "")?.replace("-", "")?.replace(" ", "")
        return normalized !in setOf("resolved", "completed", "complete", "closed", "cancelled", "canceled")
    }

    private fun parseDate(value: String?): LocalDate? = value?.trim()?.let { raw ->
        runCatching { LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).toLocalDate() }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(raw).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(raw) }.getOrNull()
            ?: runCatching { LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
    }
}
