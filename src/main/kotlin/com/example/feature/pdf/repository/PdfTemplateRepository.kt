package com.example.feature.pdf.repository

import com.example.core.RequestContext
import com.example.feature.pdf.dto.*

interface PdfTemplateRepository {
    fun loadFieldReport(ctx: RequestContext, companyId: Int, fieldReportId: String): FieldReportPdfDataDto?
    fun loadFieldReportTemplates(ctx: RequestContext): PdfTemplateDto
}
