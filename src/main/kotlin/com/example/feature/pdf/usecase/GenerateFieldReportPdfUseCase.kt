package com.example.feature.pdf.usecase

import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.pdf.dto.GeneratedPdf
import com.example.feature.pdf.repository.PdfTemplateRepository
import com.example.feature.pdf.service.HtmlTemplateService
import com.example.feature.pdf.service.PdfRendererService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfNotFoundException : RuntimeException()

class GenerateFieldReportPdfUseCase(
    private val repository: PdfTemplateRepository,
    private val templates: HtmlTemplateService,
    private val renderer: PdfRendererService
) {
    suspend fun execute(auth: AuthenticatedRequestContext, fieldReportId: String): GeneratedPdf = withContext(Dispatchers.IO) {
        require(ID_PATTERN.matches(fieldReportId)) { "Invalid FieldReportID" }
        val ctx = auth.toRequestContext()
        val data = repository.loadFieldReport(ctx, auth.companyId, fieldReportId) ?: throw PdfNotFoundException()
        val html = templates.render(data, repository.loadFieldReportTemplates(ctx))
        GeneratedPdf(renderer.render(html), "${safeFilename(data.report.ReportNumber ?: "field-report")}.pdf")
    }

    private fun safeFilename(value: String) = value.replace(Regex("[^A-Za-z0-9._-]"), "_").trim('.', '_').take(100).ifBlank { "field-report" }

    companion object { private val ID_PATTERN = Regex("[A-Za-z0-9_-]{1,36}") }
}
