package com.example.feature.fieldreport.repository

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.FieldReportEquipment

interface FieldReportEquipmentRepository {
    fun save(ctx: RequestContext, fieldReportId: String, data: FieldReportEquipment): FieldReportEquipment?
    fun update(
        ctx: RequestContext,
        fieldReportId: String,
        fieldReportEquipmentId: String,
        data: FieldReportEquipment
    ): FieldReportEquipment?
    fun delete(ctx: RequestContext, fieldReportId: String, fieldReportEquipmentId: String): Boolean
}
