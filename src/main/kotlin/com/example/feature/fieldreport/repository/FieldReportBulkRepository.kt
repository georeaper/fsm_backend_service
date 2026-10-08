package com.example.feature.fieldreport.repository

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.PostFieldReportResponse

interface FieldReportBulkRepository {
    fun saveBulk(ctx: RequestContext, request: PostFieldReportResponse): PostFieldReportResponse
}
