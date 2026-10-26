package com.example.feature.notification.repository

import com.example.core.RequestContext
import com.example.feature.notification.dto.*
import com.example.feature.notification.model.*

interface NotificationRepository {
    fun tenantUserId(ctx: RequestContext, authUserId: Int): String?
    fun createRule(ctx: RequestContext, request: CreateNotificationRuleRequest, createdBy: String?): NotificationRuleResponse
    fun updateRule(ctx: RequestContext, id: String, request: UpdateNotificationRuleRequest): NotificationRuleResponse?
    fun rules(ctx: RequestContext): List<NotificationRuleResponse>
    fun rule(ctx: RequestContext, id: String): NotificationRuleResponse?
    fun setRuleEnabled(ctx: RequestContext, id: String, enabled: Boolean): Boolean
    fun deleteRule(ctx: RequestContext, id: String): Boolean
    fun userExists(ctx: RequestContext, id: String): Boolean
    fun notifications(ctx: RequestContext, userId: String, unreadOnly: Boolean, limit: Int): List<UserNotificationResponse>
    fun markRead(ctx: RequestContext, notificationId: String, userId: String): Boolean
    fun unreadCount(ctx: RequestContext, userId: String): Long
    fun jobs(ctx: RequestContext, status: String?, limit: Int): List<NotificationJobResponse>
    fun retryJob(ctx: RequestContext, id: String): Boolean
    fun registerDevice(ctx: RequestContext, userId: String, request: RegisterDeviceTokenRequest)
    fun removeDevice(ctx: RequestContext, userId: String, tokenId: String): Boolean
    fun enabledRules(ctx: RequestContext, trigger: NotificationTriggerType, entity: NotificationEntityType? = null, event: NotificationEventType? = null): List<NotificationRule>
    fun snapshots(ctx: RequestContext, entity: NotificationEntityType): List<EntitySnapshot>
    fun snapshot(ctx: RequestContext, entity: NotificationEntityType, id: String): EntitySnapshot?
    fun recipients(ctx: RequestContext, companyId: Int, rule: NotificationRule, snapshot: EntitySnapshot): List<String>
    fun enqueue(ctx: RequestContext, rule: NotificationRule, snapshot: EntitySnapshot, recipient: String, scheduledAt: Long, sourceValue: String, eventKey: String? = null)
    fun cancelObsoleteDateJobs(ctx: RequestContext, rule: NotificationRule, snapshot: EntitySnapshot, currentKey: String)
    fun claimDue(ctx: RequestContext, workerId: String, now: Long, limit: Int): List<NotificationJobResponse>
    fun completeJob(ctx: RequestContext, jobId: String, snapshot: EntitySnapshot?, rule: NotificationRule?)
    fun skipJob(ctx: RequestContext, jobId: String, reason: String)
    fun failJob(ctx: RequestContext, jobId: String, error: String)
}

data class TenantDescriptor(val companyId: Int, val dbName: String, val timeZone: String)
interface NotificationTenantDirectory { fun activeTenants(): List<TenantDescriptor> }
