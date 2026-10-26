package com.example.feature.notification.scheduler

import com.example.core.RequestContext
import com.example.feature.notification.model.*
import com.example.feature.notification.repository.*
import com.example.feature.notification.registry.NotificationRegistry
import com.example.feature.notification.usecase.NotificationDatePlanner
import kotlinx.coroutines.*
import java.net.InetAddress
import java.time.ZoneId
import java.util.UUID

internal object NotificationJobExecutionPolicy {
    fun missingEntityReason(snapshot: EntitySnapshot?, hasEventRendering: Boolean): String? =
        if ((snapshot == null || snapshot.isDeleted) && !hasEventRendering) "Entity missing or deleted" else null
}

class NotificationScheduler(
    private val repository: NotificationRepository,
    private val tenantDirectory: NotificationTenantDirectory,
    private val pollIntervalMs: Long = 30_000
) {
    private val workerId = "${runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("worker")}-${UUID.randomUUID()}"
    fun start(scope: CoroutineScope): Job = scope.launch(Dispatchers.IO) {
        while (isActive) {
            runCatching { tenantDirectory.activeTenants() }.getOrElse { emptyList() }.forEach { tenant ->
                if (!isActive) return@launch
                runCatching { processTenant(tenant) }.onFailure { System.err.println("Notification scheduler failed for ${tenant.dbName}: ${it.message}") }
            }
            delay(pollIntervalMs)
        }
    }
    private fun processTenant(tenant: TenantDescriptor) {
        val ctx = RequestContext(tenant.dbName, "notification-scheduler")
        val zone = runCatching { ZoneId.of(tenant.timeZone) }.getOrDefault(ZoneId.of("UTC"))
        NotificationDatePlanner(repository, zone).reconcile(ctx, tenant.companyId)
        val now = System.currentTimeMillis()
        repository.claimDue(ctx, workerId, now, 25).forEach { job ->
            runCatching {
                val entity = NotificationEntityType.valueOf(job.entityType)
                val snapshot = repository.snapshot(ctx, entity, job.entityId)
                val rule = job.ruleId?.let { id -> repository.enabledRules(ctx, NotificationTriggerType.DATE, entity).firstOrNull { it.ruleId == id }
                    ?: repository.enabledRules(ctx, NotificationTriggerType.EVENT, entity).firstOrNull { it.ruleId == id } }
                val hasEventRendering = job.renderedTitle != null && job.renderedMessage != null
                val missingEntityReason = NotificationJobExecutionPolicy.missingEntityReason(snapshot, hasEventRendering)
                if (missingEntityReason != null) repository.skipJob(ctx, job.jobId, missingEntityReason)
                else if (rule == null || !rule.isEnabled) repository.skipJob(ctx, job.jobId, "Rule missing or disabled")
                else if (hasEventRendering) repository.completeJob(ctx, job.jobId, null, rule)
                else if (!NotificationRegistry.conditionsMatch(rule.conditions, snapshot!!)) repository.skipJob(ctx, job.jobId, "Conditions no longer match")
                else repository.completeJob(ctx, job.jobId, snapshot, rule)
            }.onFailure { repository.failJob(ctx, job.jobId, it.message ?: it.javaClass.simpleName) }
        }
    }
}
