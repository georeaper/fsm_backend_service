package com.example.feature.pdf.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.fieldreport.repository.FieldReportApiRepository
import com.example.feature.pdf.dto.*
import com.example.feature.pdf.service.SafeImageService
import com.example.feature.pdf.service.HtmlTemplateService
import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.CompanyTable
import com.example.models.databaseModels.settingsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PdfTemplateRepositoryImpl(
    private val dbProvider: DatabaseProvider,
    private val fieldReports: FieldReportApiRepository,
    private val images: SafeImageService,
    private val htmlTemplates: HtmlTemplateService
) : PdfTemplateRepository {
    override fun loadFieldReport(ctx: RequestContext, companyId: Int, fieldReportId: String): FieldReportPdfDataDto? {
        val report = fieldReports.findById(ctx, fieldReportId) ?: return null
        val settings = pdfSettings(ctx)
        val company = transaction(DatabaseFactory.centralDb) {
            CompanyTable.select { (CompanyTable.id eq companyId) and (CompanyTable.databaseName eq ctx.dbName) }
                .singleOrNull()?.let { it[CompanyTable.name] to it[CompanyTable.contactEmail] }
        } ?: return null
        return FieldReportPdfDataDto(
            report,
            CompanyPdfHeaderDto(
                company.first, company.second, settings[ADDRESS], settings[PHONE], settings[FOOTER],
                images.validDataUri(settings[LOGO])
            ),
            images.validDataUri(report.ClientSignature)
        )
    }

    override fun loadFieldReportTemplates(ctx: RequestContext): PdfTemplateDto {
        val custom = pdfSettings(ctx)
        return PdfTemplateDto(
            selectTemplate(custom[MAIN], resource("fieldreport-main.html"), htmlTemplates),
            selectTemplate(custom[EQUIPMENT], resource("fieldreport-equipment.html"), htmlTemplates),
            selectTemplate(custom[CHECKFORMS], resource("fieldreport-checkforms.html"), htmlTemplates)
        )
    }

    private fun pdfSettings(ctx: RequestContext): Map<String, String> = transaction(dbProvider.getDatabase(ctx.dbName)) {
        settingsTable.slice(settingsTable.settingsKey, settingsTable.settingsValue)
            .select { (settingsTable.settingsKey inList KEYS) and (settingsTable.isDeleted eq false) }
            .mapNotNull { row -> row[settingsTable.settingsKey]?.let { key -> row[settingsTable.settingsValue]?.let { key to it } } }
            .toMap()
    }

    private fun resource(name: String) = requireNotNull(javaClass.getResource("/pdf/templates/$name")) { "Bundled PDF template is missing: $name" }.readText()
    companion object {
        const val MAIN = "PDF_FIELDREPORT_MAIN"
        const val EQUIPMENT = "PDF_FIELDREPORT_EQUIPMENT"
        const val CHECKFORMS = "PDF_FIELDREPORT_CHECKFORMS"
        const val LOGO = "PDF_COMPANY_LOGO"
        const val PHONE = "PDF_COMPANY_PHONE"
        const val ADDRESS = "PDF_COMPANY_ADDRESS"
        const val FOOTER = "PDF_COMPANY_FOOTER"
        const val MAX_TEMPLATE_CHARS = 250_000
        private val KEYS = listOf(MAIN, EQUIPMENT, CHECKFORMS, LOGO, PHONE, ADDRESS, FOOTER)
    }
}

internal fun selectTemplate(custom: String?, bundledDefault: String, service: HtmlTemplateService): String =
    custom?.takeIf { it.isNotBlank() && it.length <= PdfTemplateRepositoryImpl.MAX_TEMPLATE_CHARS && service.isValidTemplate(it) } ?: bundledDefault
