package com.example.feature.fieldreport

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.fieldreport.dto.FieldReportBulkRequest
import com.example.feature.fieldreport.dto.PostFieldReportResponse
import com.example.feature.fieldreport.repository.FieldReportBulkRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json

fun Route.fieldReportBulkRoute(
    bulkRepository: FieldReportBulkRepository,
    contextResolver: AuthenticatedRequestContextResolver
) {
    authenticate("auth-jwt-v2") {
        post("/fieldreports/bulk") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val raw = call.receiveText()
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            val request = runCatching {
                FieldReportPayloadNormalizer.normalizeFieldReportRequest(
                    json.decodeFromString<PostFieldReportResponse>(raw)
                )
            }.getOrElse {
                runCatching {
                    FieldReportPayloadNormalizer.normalizeBulkRequest(
                        json.decodeFromString<FieldReportBulkRequest>(raw)
                    )
                }.getOrElse { throw it }
            }

            val result = bulkRepository.saveBulk(ctx, request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
