package com.example.feature.fieldreport.repository

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.*

interface FieldReportApiRepository {
    fun findAll(ctx: RequestContext): List<FieldReportListDto>
    fun findById(ctx: RequestContext, id: String): FieldReportDetailDto?
    fun create(ctx: RequestContext, request: FieldReportCreateDto): FieldReportDetailDto
    fun update(ctx: RequestContext, id: String, request: FieldReportUpdateDto): FieldReportDetailDto?
    fun syncEquipments(ctx: RequestContext, id: String, request: FieldReportEquipmentSyncRequest): FieldReportDetailDto?
    fun syncInventories(ctx: RequestContext, id: String, request: FieldReportInventorySyncRequest): FieldReportDetailDto?
    fun syncTools(ctx: RequestContext, id: String, request: FieldReportToolsSyncRequest): FieldReportDetailDto?
    fun technicalCases(ctx: RequestContext, id: String): List<FieldReportTechnicalCaseDto>?
    fun replaceTechnicalCases(ctx: RequestContext, id: String, technicalCaseIds: List<String>): List<FieldReportTechnicalCaseDto>?
    fun technicians(ctx: RequestContext, id: String): List<FieldReportTechnicianDto>?
    fun replaceTechnicians(ctx: RequestContext, id: String, technicians: List<FieldReportTechnicianWriteDto>): List<FieldReportTechnicianDto>?
    fun scheduledWorkOrders(ctx: RequestContext, from: String, to: String, technicianId: String?, status: String?): List<SchedulingWorkOrderDto>
    fun complete(ctx: RequestContext, id: String, request: CompleteFieldReportRequest): CompleteFieldReportResponse?
    fun delete(ctx: RequestContext, id: String): Boolean
}
