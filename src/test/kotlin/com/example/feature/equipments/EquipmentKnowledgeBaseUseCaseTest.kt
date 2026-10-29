package com.example.feature.equipments

import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse
import com.example.feature.equipments.repository.*
import com.example.feature.equipments.usecase.EquipmentKnowledgeBaseQuery
import com.example.feature.equipments.usecase.GetEquipmentUseCase
import com.example.models.api.Equipments
import kotlin.test.*

class EquipmentKnowledgeBaseUseCaseTest {
    private val ctx = RequestContext("tenant", "user")

    @Test fun `filters are applied before the route paginates the result`() {
        val result = GetEquipmentUseCase(repository()).knowledgeBase(
            ctx,
            EquipmentKnowledgeBaseQuery("pump", "Grundfos", setOf("c1"), "ACTIVE", "ACTIVE", "WITH_OPEN")
        )

        assertEquals(1, result.size)
        assertEquals("e1", result.single().equipmentId)
        assertEquals("SN-1", result.single().code)
        assertEquals("SN-1", result.single().serialNumber)
        assertEquals(1, result.single().openTechnicalCases)
        assertEquals("2026-09-15", result.single().lastServiceDate)
    }

    @Test fun `detail contains overview services cases and maintenance schedule`() {
        val detail = assertNotNull(GetEquipmentUseCase(repository()).knowledgeBaseDetail(ctx, "e1"))

        assertEquals("Pump", detail.overview.name)
        assertEquals("SN-1", detail.overview.code)
        assertEquals("Factory", detail.overview.location)
        assertEquals(1, detail.overview.openTechnicalCases)
        assertEquals(listOf("SERVICE", "TECHNICAL_CASE"), detail.recentActivity.map { it.activityType })
        assertEquals("WO-1", detail.serviceHistory.single().number)
        assertEquals("TC-1", detail.technicalCases.single().number)
        assertEquals("Preventive", detail.maintenanceSchedule.single().maintenanceName)
    }

    @Test fun `filter options are distinct and sorted`() {
        val filters = GetEquipmentUseCase(repository()).knowledgeBaseFilters(ctx)
        assertEquals(listOf("Atlas Copco", "Grundfos"), filters.manufacturers)
        assertEquals(listOf("c1", "c2"), filters.customers.map { it.customerId })
    }

    private fun repository() = FakeEquipmentRepository(
        EquipmentKnowledgeBaseData(
            equipment = listOf(
                EquipmentKnowledgeBaseRow("e1", "Pump", "SN-1", "CR 15-5", "Grundfos", null, null, "Pump", "active", true, "20250110000000", "c1", "Acme", "Factory"),
                EquipmentKnowledgeBaseRow("e2", "Compressor", "SN-2", "GA 37", "Atlas Copco", null, null, "Compressor", "expired", true, null, "c2", "Beta", null)
            ),
            technicalCases = listOf(
                EquipmentTechnicalCaseRow("tc1", "e1", "TC-1", "Failure", "Mechanical", "Open", "High", true, "20260802000000", null)
            ),
            services = listOf(
                EquipmentServiceRow("e1", "wo1", "WO-1", "Service", "Preventive", "Completed", null, "20260915090000", "20260915110000", "20260915090000", "20260915110000", "m1", "Preventive")
            )
        )
    )
}

private class FakeEquipmentRepository(private val data: EquipmentKnowledgeBaseData) : EquipmentRepository {
    override fun knowledgeBaseData(ctx: RequestContext) = data
    override fun findAll(ctx: RequestContext) = emptyList<com.example.feature.equipments.dto.EquipmentsResponse>()
    override fun save(ctx: RequestContext, data: Equipments) = data
    override fun edit(ctx: RequestContext, data: EditEquipmentResponse) = data
    override fun update(ctx: RequestContext, data: Equipments) = true
    override fun delete(ctx: RequestContext, id: String) = true
}
