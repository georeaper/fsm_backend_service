package com.example.feature.dashboard

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.dashboard.dto.DashboardError
import com.example.feature.dashboard.usecase.DashboardUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Year
import java.util.UUID

fun Route.dashboardRoute(useCase: DashboardUseCase, contextResolver: AuthenticatedRequestContextResolver) {
    authenticate("auth-jwt-v2") {
        route("/dashboard") {
            get("/summary") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                call.respond(useCase.summary(ctx))
            }
            get("/work-orders") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                if ((call.request.queryParameters["period"] ?: "year").lowercase() != "year") return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_PERIOD", "period must be year"))
                val yearText = call.request.queryParameters["year"]
                val year = yearText?.toIntOrNull() ?: Year.now().value
                if ((yearText != null && yearText.toIntOrNull() == null) || year !in 2000..2100) return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_YEAR", "year must be between 2000 and 2100"))
                call.respond(useCase.workOrders(ctx, year))
            }
            get("/technical-cases") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                val limit = call.boundedLimit("recentLimit", 5, 50) ?: return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_LIMIT", "recentLimit must be between 1 and 50"))
                call.respond(useCase.technicalCases(ctx, limit))
            }
            get("/tasks/upcoming") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                val limit = call.boundedLimit("limit", 5, 50) ?: return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_LIMIT", "limit must be between 1 and 50"))
                call.respond(useCase.upcomingTasks(ctx, limit))
            }
            get("/due-dates") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                call.respond(useCase.dueDates(ctx))
            }
            get("/top-customers") {
                val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
                if ((call.request.queryParameters["period"] ?: "month").lowercase() != "month") return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_PERIOD", "period must be month"))
                val limit = call.boundedLimit("limit", 5, 50) ?: return@get call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_LIMIT", "limit must be between 1 and 50"))
                call.respond(useCase.topCustomers(ctx, limit))
            }
        }
        patch("/tasks/{taskId}/complete") {
            val ctx = call.principal<JWTPrincipal>()?.let(contextResolver::resolve)?.toRequestContext()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized, DashboardError("UNAUTHORIZED", "Authentication is required"))
            val id = call.parameters["taskId"]?.takeIf { runCatching { UUID.fromString(it) }.isSuccess }
                ?: return@patch call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_TASK_ID", "taskId must be a UUID"))
            try {
                val result = useCase.completeTask(ctx, id) ?: return@patch call.respond(HttpStatusCode.NotFound, DashboardError("TASK_NOT_FOUND", "Task was not found"))
                call.respond(HttpStatusCode.OK, result)
            } catch (_: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, DashboardError("INVALID_TASK_TRANSITION", "Task cannot be completed"))
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.boundedLimit(name: String, default: Int, max: Int): Int? {
    val raw = request.queryParameters[name] ?: return default
    return raw.toIntOrNull()?.takeIf { it in 1..max }
}
