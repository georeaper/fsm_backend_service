package com.example.core

import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlin.math.ceil

@PublishedApi
internal const val DEFAULT_PAGE_SIZE = 50

@PublishedApi
internal const val MAX_PAGE_SIZE = 200

suspend inline fun <reified T> ApplicationCall.respondPaginated(items: List<T>) {
    val pageText = request.queryParameters["page"]
    val pageSizeText = request.queryParameters["pageSize"]
    val page = pageText?.toIntOrNull() ?: 1
    val pageSize = pageSizeText?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
    if ((pageText != null && pageText.toIntOrNull() == null) ||
        (pageSizeText != null && pageSizeText.toIntOrNull() == null) ||
        page < 1 || pageSize !in 1..MAX_PAGE_SIZE
    ) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "page must be >= 1 and pageSize must be between 1 and $MAX_PAGE_SIZE"))
        return
    }
    val total = items.size
    val totalPages = if (total == 0) 0 else ceil(total.toDouble() / pageSize).toInt()
    val fromIndex = ((page - 1).toLong() * pageSize).coerceAtMost(total.toLong()).toInt()
    val pageItems = items.subList(fromIndex, (fromIndex + pageSize).coerceAtMost(total))
    response.headers.apply {
        append("X-Total-Count", total.toString())
        append("X-Page", page.toString())
        append("X-Page-Size", pageSize.toString())
        append("X-Total-Pages", totalPages.toString())
    }
    respond(pageItems)
}
