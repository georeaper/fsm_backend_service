package com.example.feature.fieldreport

import com.example.core.RequestContext
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
    bulkRepository: FieldReportBulkRepository
) {
    authenticate("auth-jwt") {
        post("/fieldreports/bulk") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )

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
