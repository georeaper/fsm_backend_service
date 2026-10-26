package com.example.feature.pdf.dto

import com.example.feature.fieldreport.dto.FieldReportDetailDto

data class PdfTemplateDto(val main: String, val equipment: String, val checkforms: String)
data class CompanyPdfHeaderDto(
    val name: String, val email: String, val address: String?, val phone: String?,
    val footer: String?, val logoDataUri: String?
)
data class FieldReportPdfDataDto(
    val report: FieldReportDetailDto,
    val company: CompanyPdfHeaderDto,
    val signatureDataUri: String?
)
data class GeneratedPdf(val bytes: ByteArray, val filename: String)
