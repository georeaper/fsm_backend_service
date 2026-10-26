package com.example.feature.notification.usecase

import com.example.core.RequestContext
import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.notification.dto.*
import com.example.feature.notification.model.*
import com.example.feature.notification.registry.NotificationRegistry
import com.example.feature.notification.repository.NotificationRepository
import java.time.Duration
import java.time.ZoneId

class NotificationUseCases(private val repository: NotificationRepository) {
    fun metadata() = NotificationRegistry.metadata()

    fun listRules(auth: AuthenticatedRequestContext): List<NotificationRuleResponse> { requireAdmin(auth); return repository.rules(auth.toRequestContext()) }
    fun createRule(auth: AuthenticatedRequestContext, request: CreateNotificationRuleRequest): NotificationRuleResponse {
        requireAdmin(auth); NotificationRegistry.validate(request); val ctx = auth.toRequestContext(); validateSpecificUser(ctx, request.recipientType, request.specificUserId)
        return repository.createRule(ctx, request, requireTenantUser(auth))
    }
    fun updateRule(auth: AuthenticatedRequestContext, id: String, request: UpdateNotificationRuleRequest): NotificationRuleResponse? {
        requireAdmin(auth); val create = request.toCreate(); NotificationRegistry.validate(create); validateSpecificUser(auth.toRequestContext(), request.recipientType, request.specificUserId)
        return repository.updateRule(auth.toRequestContext(), id, request)
    }
    fun setEnabled(auth: AuthenticatedRequestContext, id: String, enabled: Boolean): Boolean { requireAdmin(auth); return repository.setRuleEnabled(auth.toRequestContext(), id, enabled) }
    fun deleteRule(auth: AuthenticatedRequestContext, id: String): Boolean { requireAdmin(auth); return repository.deleteRule(auth.toRequestContext(), id) }
    fun notifications(auth: AuthenticatedRequestContext, unreadOnly: Boolean, limit: Int) = repository.notifications(auth.toRequestContext(), requireTenantUser(auth), unreadOnly, limit.coerceIn(1, 100))
    fun markRead(auth: AuthenticatedRequestContext, id: String) = repository.markRead(auth.toRequestContext(), id, requireTenantUser(auth))
    fun unreadCount(auth: AuthenticatedRequestContext) = repository.unreadCount(auth.toRequestContext(), requireTenantUser(auth))
    fun jobs(auth: AuthenticatedRequestContext, status: String?, limit: Int): List<NotificationJobResponse> { requireAdmin(auth); return repository.jobs(auth.toRequestContext(), status?.uppercase(), limit.coerceIn(1, 200)) }
    fun retryJob(auth: AuthenticatedRequestContext, id: String): Boolean { requireAdmin(auth); return repository.retryJob(auth.toRequestContext(), id) }
    fun registerDevice(auth: AuthenticatedRequestContext, request: RegisterDeviceTokenRequest) {
        require(request.deviceToken.isNotBlank() && request.deviceToken.length <= 4096) { "Invalid device token" }
        require(request.platform.uppercase() in setOf("ANDROID", "WEB")) { "Invalid platform" }
        repository.registerDevice(auth.toRequestContext(), requireTenantUser(auth), request)
    }
    fun removeDevice(auth: AuthenticatedRequestContext, tokenId: String) = repository.removeDevice(auth.toRequestContext(), requireTenantUser(auth), tokenId)

    private fun requireTenantUser(auth: AuthenticatedRequestContext) = requireNotNull(repository.tenantUserId(auth.toRequestContext(), auth.userId)) { "Authenticated user is not linked to a tenant user" }
    private fun requireAdmin(auth: AuthenticatedRequestContext) = require(auth.role in setOf("owner", "admin")) { "Insufficient permissions" }
    private fun validateSpecificUser(ctx: RequestContext, type: String, id: String?) { if (type.equals("SPECIFIC_USER", true)) require(id != null && repository.userExists(ctx, id)) { "Specific user does not belong to this tenant" } }
    private fun UpdateNotificationRuleRequest.toCreate() = CreateNotificationRuleRequest(name, entityType, triggerType, eventType, dateField, offsetValue, offsetUnit, offsetDirection, conditions, recipientType, specificUserId, titleTemplate, messageTemplate, isEnabled)
}

class NotificationCoordinator(private val repository: NotificationRepository) {
    fun onCreated(ctx: RequestContext, companyId: Int, entity: NotificationEntityType, id: String) = safely { dispatch(ctx, companyId, entity, id, NotificationEventType.CREATED) }
    fun onDeleted(ctx: RequestContext, companyId: Int, snapshot: EntitySnapshot) = safely { dispatchSnapshot(ctx, companyId, snapshot, NotificationEventType.DELETED) }
    fun onUpdated(ctx: RequestContext, companyId: Int, before: EntitySnapshot?, after: EntitySnapshot) {
        safely {
            dispatchSnapshot(ctx, companyId, after, NotificationEventType.UPDATED)
            if (before?.status != after.status) dispatchSnapshot(ctx, companyId, after, NotificationEventType.STATUS_CHANGED)
            if (before?.assignedUserId != after.assignedUserId && after.assignedUserId != null) dispatchSnapshot(ctx, companyId, after, NotificationEventType.ASSIGNED)
        }
    }
    fun snapshot(ctx: RequestContext, entity: NotificationEntityType, id: String) = runCatching { repository.snapshot(ctx, entity, id) }
        .onFailure { System.err.println("Notification snapshot failed: ${it.message}") }.getOrNull()
    private fun dispatch(ctx: RequestContext, companyId: Int, entity: NotificationEntityType, id: String, event: NotificationEventType) { repository.snapshot(ctx, entity, id)?.let { dispatchSnapshot(ctx, companyId, it, event) } }
    private fun dispatchSnapshot(ctx: RequestContext, companyId: Int, snapshot: EntitySnapshot, event: NotificationEventType) {
        val now = System.currentTimeMillis()
        repository.enabledRules(ctx, NotificationTriggerType.EVENT, snapshot.entityType, event).filter { NotificationRegistry.conditionsMatch(it.conditions, snapshot) }.forEach { rule ->
            repository.recipients(ctx, companyId, rule, snapshot).forEach { recipient -> repository.enqueue(ctx, rule, snapshot, recipient, now, event.name, "$event:$now") }
        }
    }
    private inline fun safely(block: () -> Unit) = runCatching(block).onFailure { System.err.println("Notification event processing failed: ${it.message}") }.let { Unit }
}

class NotificationDatePlanner(private val repository: NotificationRepository, private val zoneId: ZoneId = ZoneId.of("UTC")) {
    fun reconcile(ctx: RequestContext, companyId: Int) {
        repository.enabledRules(ctx, NotificationTriggerType.DATE).forEach { rule ->
            repository.snapshots(ctx, rule.entityType).filterNot { it.isDeleted }.forEach { snapshot ->
                val raw = snapshot.dates[rule.dateField]
                if (raw.isNullOrBlank()) {
                    repository.cancelObsoleteDateJobs(ctx, rule, snapshot, "MISSING")
                    return@forEach
                }
                val instant = NotificationRegistry.parseLegacyDate(raw, zoneId)
                if (instant == null) {
                    repository.cancelObsoleteDateJobs(ctx, rule, snapshot, "INVALID:$raw")
                    return@forEach
                }
                val amount = when (rule.offsetUnit) { NotificationOffsetUnit.MINUTES -> Duration.ofMinutes(rule.offsetValue!!.toLong()); NotificationOffsetUnit.HOURS -> Duration.ofHours(rule.offsetValue!!.toLong()); NotificationOffsetUnit.DAYS -> Duration.ofDays(rule.offsetValue!!.toLong()); null -> return@forEach }
                val scheduled = if (rule.offsetDirection == NotificationOffsetDirection.BEFORE) instant.minus(amount) else instant.plus(amount)
                val recipientBasis = when (rule.recipientType) {
                    NotificationRecipientType.ASSIGNED_USER -> snapshot.assignedUserId ?: "UNASSIGNED"
                    NotificationRecipientType.SPECIFIC_USER -> rule.specificUserId ?: "MISSING"
                    NotificationRecipientType.ADMINISTRATORS -> rule.recipientType.name
                }
                val sourceKey = "${rule.dateField}:$raw:${scheduled.toEpochMilli()}:$recipientBasis"
                repository.cancelObsoleteDateJobs(ctx, rule, snapshot, sourceKey)
                if (scheduled.toEpochMilli() >= System.currentTimeMillis() && NotificationRegistry.conditionsMatch(rule.conditions, snapshot)) {
                    repository.recipients(ctx, companyId, rule, snapshot).forEach { repository.enqueue(ctx, rule, snapshot, it, scheduled.toEpochMilli(), sourceKey) }
                }
            }
        }
    }
}
