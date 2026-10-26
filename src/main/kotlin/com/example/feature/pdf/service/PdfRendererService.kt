package com.example.feature.pdf.service

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.outputdevice.helper.ExternalResourceControlPriority
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import java.io.ByteArrayOutputStream

interface PdfRendererService { fun render(html: String): ByteArray }

class OpenHtmlPdfRendererService : PdfRendererService {
    override fun render(html: String): ByteArray {
        require(html.length <= MAX_HTML_CHARS) { "Rendered PDF HTML is too large" }
        val output = ByteArrayOutputStream()
        output.use { stream ->
            val builder = PdfRendererBuilder()
                .withHtmlContent(html, null)
                .useDefaultPageSize(210f, 297f, BaseRendererBuilder.PageSizeUnits.MM)
                .useExternalResourceAccessControl({ uri, _ -> uri.startsWith("data:image/png;base64,") || uri.startsWith("data:image/jpeg;base64,") }, ExternalResourceControlPriority.RUN_AFTER_RESOLVING_URI)
                .useFastMode()
                .toStream(stream)
            builder.useFont({ requireNotNull(javaClass.getResourceAsStream(FONT_RESOURCE)) { "Bundled PDF font is missing" } }, "Noto Sans")
            builder.run()
        }
        return output.toByteArray().also { require(it.size <= MAX_PDF_BYTES) { "Generated PDF exceeds the size limit" } }
    }

    companion object {
        private const val FONT_RESOURCE = "/pdf/fonts/NotoSans-Regular.ttf"
        private const val MAX_HTML_CHARS = 5_000_000
        private const val MAX_PDF_BYTES = 25 * 1024 * 1024
    }
}
