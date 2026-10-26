package com.example.feature.pdf.service

import com.example.core.DateUtils
import com.example.feature.pdf.dto.*
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.StringLoader
import java.io.StringWriter

class HtmlTemplateService {
    private val engine = PebbleEngine.Builder()
        .loader(StringLoader())
        .autoEscaping(true)
        .strictVariables(false)
        .cacheActive(false)
        .maxRenderedSize(MAX_RENDERED_HTML)
        .build()

    fun render(data: FieldReportPdfDataDto, templates: PdfTemplateDto): String {
        val context = context(data)
        val sections = listOf(templates.main, templates.equipment, templates.checkforms).mapIndexed { index, template ->
            validate(template)
            val writer = StringWriter()
            engine.getTemplate(template).evaluate(writer, context)
            "<section class=\"pdf-section section-${index + 1}\">${writer}</section>"
        }
        return document(data, sections.joinToString("\n"))
    }

    internal fun validate(template: String) {
        require(template.length <= MAX_TEMPLATE_CHARS) { "PDF template is too large" }
        require(!FORBIDDEN_MARKUP.containsMatchIn(template)) { "PDF template contains forbidden markup" }
        require(!FORBIDDEN_PEBBLE.containsMatchIn(template)) { "PDF template contains a forbidden template operation" }
        require(!EXTERNAL_URI.containsMatchIn(template)) { "PDF template contains an external resource" }
        require(!METHOD_CALL.containsMatchIn(template)) { "PDF template method calls are not allowed" }
    }

    fun isValidTemplate(template: String): Boolean = runCatching {
        validate(template)
        engine.getTemplate(template)
    }.isSuccess

    private fun context(data: FieldReportPdfDataDto): Map<String, Any?> {
        val report = data.report
        require(report.Equipments.size <= MAX_ROWS && report.Inventories.size <= MAX_ROWS && report.SpecialTools.size <= MAX_ROWS) { "Field report is too large for PDF generation" }
        require(report.Equipments.sumOf { it.FieldReportCheckforms.size } <= MAX_ROWS) { "Field report is too large for PDF generation" }
        val equipments = report.Equipments.map { equipment ->
            mapOf(
                "id" to equipment.FieldReportEquipmentID, "equipmentId" to equipment.EquipmentID,
                "name" to equipment.EquipmentName, "manufacturer" to equipment.Manufacturer,
                "model" to equipment.EquipmentModel, "serialNumber" to equipment.SerialNumber,
                "maintenanceName" to equipment.MaintenanceName, "completed" to equipment.CompletedStatus,
                "checkforms" to equipment.FieldReportCheckforms.map { check -> mapOf(
                    "description" to check.Description, "expected" to check.ValueExpected,
                    "measured" to check.ValueMeasured, "result" to check.Result
                ) }
            )
        }
        return mapOf(
            "report" to mapOf(
                "id" to report.FieldReportID, "reportNumber" to report.ReportNumber, "title" to report.Title,
                "description" to report.Description, "startDate" to displayDate(report.StartDate), "endDate" to displayDate(report.EndDate),
                "scheduledStart" to displayDate(report.ScheduledStart), "scheduledEnd" to displayDate(report.ScheduledEnd),
                "status" to report.Status, "priority" to report.Priority, "type" to report.Type,
                "customerName" to report.CustomerName, "clientName" to report.ClientName,
                "technicianName" to report.UserName, "department" to report.Department,
                "contractName" to report.ContractName, "ticketNumber" to report.TicketNumber,
                "signatureDataUri" to data.signatureDataUri
            ),
            "company" to mapOf(
                "name" to data.company.name, "email" to data.company.email, "address" to data.company.address,
                "phone" to data.company.phone, "footer" to data.company.footer, "logoDataUri" to data.company.logoDataUri
            ),
            "equipments" to equipments,
            "inventories" to report.Inventories.map { mapOf("name" to it.InventoryName, "quantity" to it.Quantity) },
            "tools" to report.SpecialTools.map { mapOf(
                "name" to (it.ToolName ?: it.Title), "manufacturer" to it.Manufacturer, "model" to it.Model,
                "serialNumber" to it.SerialNumber, "calibrationDate" to displayDate(it.CalibrationDate)
            ) }
        )
    }

    private fun displayDate(value: String?): String? = value?.let { DateUtils.storageToUi(it) ?: ISO_DATE.find(it)?.value?.let { date ->
        val parts = date.split('-'); "${parts[2]}/${parts[1]}/${parts[0]}"
    } ?: it }

    private fun document(data: FieldReportPdfDataDto, body: String): String {
        val logo = data.company.logoDataUri?.let { "<img class=\"company-logo\" src=\"$it\" alt=\"Logo\" />" } ?: ""
        val companyName = escape(data.company.name)
        val contact = listOfNotNull(data.company.address, data.company.phone, data.company.email).joinToString(" · ").let(::escape)
        val footer = escape(data.company.footer.orEmpty())
        val reportNumber = escape(data.report.ReportNumber ?: data.report.FieldReportID)
        return """<!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml"><head><meta charset="UTF-8"/><style>
            @page { size: A4 portrait; margin: 32mm 14mm 20mm 14mm; @top-center { content: element(pdfHeader) } @bottom-center { content: element(pdfFooter) } }
            * { box-sizing: border-box; } body { font-family: 'Noto Sans', sans-serif; font-size: 9.5pt; color: #1f2937; line-height: 1.35; }
            .pdf-header { position: running(pdfHeader); width: 100%; border-bottom: 1px solid #cbd5e1; padding: 5mm 0 3mm; }
            .company-logo { max-height: 15mm; max-width: 38mm; float: left; margin-right: 5mm; } .company-name { font-size: 14pt; font-weight: bold; color: #0f3b66; }
            .company-contact { font-size: 8pt; color: #475569; } .pdf-footer { position: running(pdfFooter); font-size: 8pt; color: #64748b; border-top: 1px solid #cbd5e1; padding-top: 2mm; }
            .page-number:after { content: counter(page) ' / ' counter(pages); } .footer-right { float: right; }
            .pdf-section + .pdf-section { page-break-before: always; } h1 { color: #0f3b66; font-size: 18pt; margin: 0 0 5mm; } h2 { color: #0f3b66; font-size: 13pt; page-break-after: avoid; margin: 5mm 0 2mm; }
            h3 { page-break-after: avoid; } table { width: 100%; border-collapse: collapse; margin: 2mm 0 5mm; } thead { display: table-header-group; }
            tr { page-break-inside: avoid; } th, td { border: 1px solid #cbd5e1; padding: 2mm; vertical-align: top; } th { background: #eaf1f7; text-align: left; color: #0f3b66; }
            .label { color: #64748b; font-size: 8pt; } .value { font-weight: bold; } .description { white-space: pre-wrap; overflow-wrap: break-word; }
            .info-table td { width: 25%; } .signature { height: 25mm; max-width: 70mm; } .signature-box { height: 30mm; border: 1px solid #cbd5e1; padding: 2mm; }
            .check-group { page-break-inside: avoid; } .check-group.large { page-break-inside: auto; }
        </style></head><body>
        <div class="pdf-header">$logo<div class="company-name">$companyName</div><div class="company-contact">$contact</div></div>
        <div class="pdf-footer"><span>$footer</span><span class="footer-right">$reportNumber · Page <span class="page-number"></span></span></div>
        $body</body></html>"""
    }

    private fun escape(value: String) = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    companion object {
        private const val MAX_TEMPLATE_CHARS = 250_000
        private const val MAX_RENDERED_HTML = 5_000_000
        private const val MAX_ROWS = 5_000
        private val FORBIDDEN_MARKUP = Regex("""<\s*(script|iframe|object|embed|link|base|form|input|button|video|audio|canvas|svg|meta\s+http-equiv)\b""", setOf(RegexOption.IGNORE_CASE))
        private val FORBIDDEN_PEBBLE = Regex("""\{%\s*(include|import|from|extends|embed|parallel|set)\b""", setOf(RegexOption.IGNORE_CASE))
        private val EXTERNAL_URI = Regex("""(?:https?:|file:|ftp:|//)""", setOf(RegexOption.IGNORE_CASE))
        private val METHOD_CALL = Regex("""\{\{[^}]*\w\s*\([^}]*}}""")
        private val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
