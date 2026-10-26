package com.example.feature.notification

import com.example.feature.auth.service.*
import com.example.feature.notification.dto.*
import com.example.feature.notification.usecase.NotificationUseCases
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificationRoutes(useCases: NotificationUseCases, resolver: AuthenticatedRequestContextResolver) {
    authenticate("auth-jwt-v2") {
        route("/api/v2") {
            get("/notification-metadata") { call.respond(useCases.metadata()) }
            route("/notification-rules") {
                get { withAuth(resolver) { call.respond(useCases.listRules(it)) } }
                post { withAuth(resolver) { auth -> safe { call.respond(HttpStatusCode.Created, useCases.createRule(auth, call.receive())) } } }
                put("/{id}") { withAuth(resolver) { auth -> safe { val id = requiredId(); useCases.updateRule(auth, id, call.receive<UpdateNotificationRuleRequest>())?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound) } } }
                patch("/{id}/enabled") { withAuth(resolver) { auth -> safe { val ok = useCases.setEnabled(auth, requiredId(), call.receive<SetNotificationRuleEnabledRequest>().isEnabled); call.respond(if (ok) HttpStatusCode.NoContent else HttpStatusCode.NotFound) } } }
                delete("/{id}") { withAuth(resolver) { auth -> safe { val ok = useCases.deleteRule(auth, requiredId()); call.respond(if (ok) HttpStatusCode.NoContent else HttpStatusCode.NotFound) } } }
            }
            get("/notifications") { withAuth(resolver) { auth -> safe { call.respond(useCases.notifications(auth, call.request.queryParameters["isRead"] == "false", call.request.queryParameters["limit"]?.toIntOrNull() ?: 50)) } } }
            get("/notifications/unread-count") { withAuth(resolver) { safe { call.respond(mapOf("count" to useCases.unreadCount(it))) } } }
            patch("/notifications/{id}/read") { withAuth(resolver) { auth -> safe { val ok = useCases.markRead(auth, requiredId()); call.respond(if (ok) HttpStatusCode.NoContent else HttpStatusCode.NotFound) } } }
            get("/notification-jobs") { withAuth(resolver) { auth -> safe { call.respond(useCases.jobs(auth, call.request.queryParameters["status"], call.request.queryParameters["limit"]?.toIntOrNull() ?: 100)) } } }
            post("/notification-jobs/{id}/retry") { withAuth(resolver) { auth -> safe { val ok = useCases.retryJob(auth, requiredId()); call.respond(if (ok) HttpStatusCode.Accepted else HttpStatusCode.NotFound) } } }
            post("/notification-devices") { withAuth(resolver) { auth -> safe { useCases.registerDevice(auth, call.receive()); call.respond(HttpStatusCode.NoContent) } } }
            delete("/notification-devices/{id}") { withAuth(resolver) { auth -> safe { val ok = useCases.removeDevice(auth, requiredId()); call.respond(if (ok) HttpStatusCode.NoContent else HttpStatusCode.NotFound) } } }
        }
    }
}

private suspend fun RoutingContext.withAuth(resolver: AuthenticatedRequestContextResolver, block: suspend (AuthenticatedRequestContext) -> Unit) {
    val auth = call.principal<JWTPrincipal>()?.let(resolver::resolve) ?: return call.respond(HttpStatusCode.Unauthorized)
    block(auth)
}
private suspend fun RoutingContext.safe(block: suspend () -> Unit) { try { block() } catch (e: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request"))) } }
private fun RoutingContext.requiredId() = requireNotNull(call.parameters["id"]) { "Missing id" }
