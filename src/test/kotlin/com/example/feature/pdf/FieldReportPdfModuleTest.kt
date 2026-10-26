package com.example.feature.pdf

import com.example.core.RequestContext
import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.fieldreport.dto.*
import com.example.feature.pdf.dto.*
import com.example.feature.pdf.repository.PdfTemplateRepository
import com.example.feature.pdf.repository.selectTemplate
import com.example.feature.pdf.routes.pdfContentDisposition
import com.example.feature.pdf.service.*
import com.example.feature.pdf.usecase.GenerateFieldReportPdfUseCase
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import kotlin.test.*

class FieldReportPdfModuleTest {
    private val html = HtmlTemplateService()
    private val renderer = OpenHtmlPdfRendererService()
    private val defaults = PdfTemplateDto(resource("fieldreport-main.html"), resource("fieldreport-equipment.html"), resource("fieldreport-checkforms.html"))

    @Test fun `renders Greek report without equipment logo tools or inventory`() {
        val data = data(description = "Εργασία συντήρησης\nΟλοκληρώθηκε επιτυχώς")
        val bytes = renderer.render(html.render(data, defaults))
        Loader.loadPDF(bytes).use { document ->
            val text = PDFTextStripper().getText(document)
            assertTrue(document.numberOfPages >= 3)
            assertTrue(text.contains("Εργασία συντήρησης"))
            assertTrue(text.contains("No equipment recorded"))
            assertTrue(text.contains("No tools recorded"))
        }
    }

    @Test fun `large equipment and checkform collections create readable multipage PDF`() {
        val equipments = (1..25).map { number -> equipment(number, if (number == 1) 45 else 3) }
        val bytes = renderer.render(html.render(data(equipments = equipments, description = "Long description. ".repeat(500)), defaults))
        Loader.loadPDF(bytes).use { document ->
            val text = PDFTextStripper().getText(document)
            assertTrue(document.numberOfPages > 3)
            assertTrue(text.contains("Equipment 25"))
            assertTrue(text.contains("Check item 45"))
        }
    }

    @Test fun `template values are escaped and unsafe template operations rejected`() {
        val rendered = html.render(data(description = "<script>alert('x')</script>"), defaults)
        assertTrue(rendered.contains("&lt;script&gt;"))
        assertFalse(rendered.contains("<script>alert"))
        assertFailsWith<IllegalArgumentException> { html.validate("{% include '/etc/passwd' %}") }
        assertFailsWith<IllegalArgumentException> { html.validate("<img src=\"https://example.test/a.png\"/>") }
    }

    @Test fun `invalid field report id is rejected before repository access`() {
        var called = false
        val repository = object : PdfTemplateRepository {
            override fun loadFieldReport(ctx: RequestContext, companyId: Int, fieldReportId: String): FieldReportPdfDataDto? { called = true; return null }
            override fun loadFieldReportTemplates(ctx: RequestContext) = defaults
        }
        assertFailsWith<IllegalArgumentException> {
            runBlocking { GenerateFieldReportPdfUseCase(repository, html, renderer).execute(AuthenticatedRequestContext("tenant", "user", 1, 1, "user"), "../other-tenant") }
        }
        assertFalse(called)
    }

    @Test fun `invalid image is ignored`() {
        assertNull(SafeImageService().validDataUri("data:image/png;base64,bm90LWEtcG5n"))
        assertNull(SafeImageService().validDataUri("<svg onload='x'/>"))
    }

    @Test fun `missing or unsafe custom template falls back to bundled default`() {
        assertEquals("default", selectTemplate(null, "default", html))
        assertEquals("default", selectTemplate("{% include 'secret' %}", "default", html))
        assertEquals("<p>{{ report.title }}</p>", selectTemplate("<p>{{ report.title }}</p>", "default", html))
    }

    @Test fun `pdf download content disposition uses generated filename`() {
        assertTrue(pdfContentDisposition("FR-0006.pdf").contains("attachment"))
        assertTrue(pdfContentDisposition("FR-0006.pdf").contains("FR-0006.pdf"))
    }

    private fun data(
        description: String? = null,
        equipments: List<FieldReportEquipmentDetailDto> = emptyList()
    ) = FieldReportPdfDataDto(
        FieldReportDetailDto(
            FieldReportID = "11111111-1111-1111-1111-111111111111", ReportNumber = "FR-0006", Title = "Service",
            Description = description, CustomerName = "Customer", UserName = "Technician", Equipments = equipments
        ),
        CompanyPdfHeaderDto("FSM Δοκιμή", "info@example.test", null, null, null, null), null
    )

    private fun equipment(number: Int, checks: Int) = FieldReportEquipmentDetailDto(
        FieldReportEquipmentID = "fre-$number", EquipmentID = "eq-$number", EquipmentName = "Equipment $number",
        EquipmentModel = "M$number", Manufacturer = "Maker", SerialNumber = "SN$number", MaintenanceID = null,
        MaintenanceName = null, CompletedStatus = true,
        FieldReportCheckforms = (1..checks).map { check -> FieldReportCheckformDetailDto("c-$number-$check", "Check item $check", "OK", "OK", "PASS") }
    )

    private fun resource(name: String) = requireNotNull(javaClass.getResource("/pdf/templates/$name")).readText()
}
