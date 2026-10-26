package com.example.feature.pdf.routes

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.pdf.usecase.GenerateFieldReportPdfUseCase
import com.example.feature.pdf.usecase.PdfNotFoundException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.pdfRoutes(useCase: GenerateFieldReportPdfUseCase, resolver: AuthenticatedRequestContextResolver) {
    authenticate("auth-jwt-v2") {
        get("/fieldreports/{id}/pdf") {
            val auth = call.principal<JWTPrincipal>()?.let(resolver::resolve)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, pdfError("INVALID_ID", "FieldReportID is required"))
            try {
                val pdf = useCase.execute(auth, id)
                call.response.header(HttpHeaders.ContentDisposition, pdfContentDisposition(pdf.filename))
                call.respondBytes(pdf.bytes, ContentType.Application.Pdf, HttpStatusCode.OK)
            } catch (_: PdfNotFoundException) {
                call.respond(HttpStatusCode.NotFound, pdfError("FIELD_REPORT_NOT_FOUND", "Field Report was not found"))
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, pdfError("INVALID_REQUEST", error.message ?: "Invalid request"))
            } catch (_: Exception) {
                call.respond(HttpStatusCode.InternalServerError, pdfError("PDF_GENERATION_FAILED", "Unable to generate Field Report PDF"))
            }
        }
    }
}

private fun pdfError(code: String, message: String) = mapOf("code" to code, "message" to message)
internal fun pdfContentDisposition(filename: String) = ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, filename).toString()
