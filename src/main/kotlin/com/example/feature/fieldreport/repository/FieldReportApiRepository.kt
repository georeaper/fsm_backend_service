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
    fun delete(ctx: RequestContext, id: String): Boolean
}
