package com.example.feature.fieldreport.repository

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.EditFieldReportResponse
import com.example.feature.fieldreport.dto.FieldReportsResponse
import com.example.models.api.FieldReports

interface FieldReportRepository {
    fun save(ctx: RequestContext, data: FieldReports): FieldReports
    fun findAll(ctx: RequestContext): List<FieldReportsResponse>
    fun edit(ctx: RequestContext, data: EditFieldReportResponse): EditFieldReportResponse
    fun delete(ctx: RequestContext, id: String): Boolean
}
