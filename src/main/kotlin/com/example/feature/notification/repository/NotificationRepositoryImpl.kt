package com.example.feature.notification.repository

import com.example.core.*
import com.example.feature.notification.dto.*
import com.example.feature.notification.model.*
import com.example.feature.notification.service.NotificationTemplateRenderer
import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.UserTable
import com.example.models.databaseModels.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class NotificationRepositoryImpl(private val dbProvider: DatabaseProvider) : NotificationRepository {
    private val json = Json { ignoreUnknownKeys = false }
    private fun db(ctx: RequestContext) = dbProvider.getDatabase(ctx.dbName)

    override fun tenantUserId(ctx: RequestContext, authUserId: Int): String? = transaction(db(ctx)) {
        userTable.slice(userTable.userId).select { (userTable.authUserId eq authUserId) and (userTable.isDeleted eq false) }
            .singleOrNull()?.get(userTable.userId)
    }

    override fun createRule(ctx: RequestContext, request: CreateNotificationRuleRequest, createdBy: String?): NotificationRuleResponse = transaction(db(ctx)) {
        val now = System.currentTimeMillis(); val id = UUID.randomUUID().toString()
        NotificationRulesTable.insert { row -> fillRule(row, request, now); row[ruleId] = id; row[createdByUserId] = createdBy; row[dateCreatedEpochMs] = now }
        ruleRow(id)!!
    }

    override fun updateRule(ctx: RequestContext, id: String, request: UpdateNotificationRuleRequest): NotificationRuleResponse? = transaction(db(ctx)) {
        val create = request.toCreate()
        val changed = NotificationRulesTable.update({ NotificationRulesTable.ruleId eq id }) { fillRule(it, create, System.currentTimeMillis()) }
        if (changed == 0) null else ruleRow(id)
    }

    override fun rules(ctx: RequestContext) = transaction(db(ctx)) { NotificationRulesTable.selectAll().orderBy(NotificationRulesTable.dateCreatedEpochMs, SortOrder.DESC).map(::toRuleResponse) }
    override fun rule(ctx: RequestContext, id: String) = transaction(db(ctx)) { ruleRow(id) }
    override fun setRuleEnabled(ctx: RequestContext, id: String, enabled: Boolean) = transaction(db(ctx)) {
        val changed = NotificationRulesTable.update({ NotificationRulesTable.ruleId eq id }) { it[isEnabled] = enabled; it[lastModifiedEpochMs] = System.currentTimeMillis() }
        if (!enabled) cancelRuleJobs(id, "Rule disabled")
        changed > 0
    }
    override fun deleteRule(ctx: RequestContext, id: String) = setRuleEnabled(ctx, id, false)

    override fun userExists(ctx: RequestContext, id: String) = transaction(db(ctx)) { userTable.select { (userTable.userId eq id) and (userTable.isDeleted eq false) }.any() }
    override fun notifications(ctx: RequestContext, userId: String, unreadOnly: Boolean, limit: Int) = transaction(db(ctx)) {
        val condition = (notificationsTable.userId eq userId) and (notificationsTable.isDeleted eq false) and
            if (unreadOnly) (notificationsTable.isRead eq false) else Op.TRUE
        notificationsTable.select { condition }.orderBy(notificationsTable.createdAtEpochMs, SortOrder.DESC).limit(limit).map {
            UserNotificationResponse(it[notificationsTable.notificationId], it[notificationsTable.title], it[notificationsTable.message], it[notificationsTable.type], it[notificationsTable.entityType], it[notificationsTable.entityId], it[notificationsTable.isRead], it[notificationsTable.createdAtEpochMs])
        }
    }
    override fun markRead(ctx: RequestContext, notificationId: String, userId: String) = transaction(db(ctx)) {
        val now = System.currentTimeMillis()
        notificationsTable.update({ (notificationsTable.notificationId eq notificationId) and (notificationsTable.userId eq userId) and (notificationsTable.isDeleted eq false) }) {
            it[isRead] = true; it[readAtEpochMs] = now; it[readAt] = DateUtils.nowStorage()
        } > 0
    }
    override fun unreadCount(ctx: RequestContext, userId: String) = transaction(db(ctx)) {
        notificationsTable.select { (notificationsTable.userId eq userId) and (notificationsTable.isRead eq false) and (notificationsTable.isDeleted eq false) }.count()
    }

    override fun jobs(ctx: RequestContext, status: String?, limit: Int) = transaction(db(ctx)) {
        val query = status?.let { NotificationJobsTable.select { NotificationJobsTable.status eq it } } ?: NotificationJobsTable.selectAll()
        query.orderBy(NotificationJobsTable.scheduledAtEpochMs, SortOrder.DESC).limit(limit).map(::toJob)
    }
    override fun retryJob(ctx: RequestContext, id: String) = transaction(db(ctx)) {
        NotificationJobsTable.update({ (NotificationJobsTable.jobId eq id) and (NotificationJobsTable.status eq NotificationJobStatus.FAILED.name) }) {
            it[status] = NotificationJobStatus.RETRY.name; it[nextAttemptAtEpochMs] = System.currentTimeMillis(); it[lastError] = null
        } > 0
    }

    override fun registerDevice(ctx: RequestContext, userId: String, request: RegisterDeviceTokenRequest) = transaction(db(ctx)) {
        val now = System.currentTimeMillis()
        val updated = UserDeviceTokensTable.update({ UserDeviceTokensTable.deviceToken eq request.deviceToken }) {
            it[UserDeviceTokensTable.userId] = userId; it[platform] = request.platform.uppercase(); it[isActive] = true; it[lastSeenAtEpochMs] = now
        }
        if (updated == 0) UserDeviceTokensTable.insert {
            it[tokenId] = UUID.randomUUID().toString(); it[UserDeviceTokensTable.userId] = userId; it[deviceToken] = request.deviceToken
            it[platform] = request.platform.uppercase(); it[isActive] = true; it[lastSeenAtEpochMs] = now; it[dateCreatedEpochMs] = now
        }
        Unit
    }
    override fun removeDevice(ctx: RequestContext, userId: String, tokenId: String) = transaction(db(ctx)) {
        UserDeviceTokensTable.update({ (UserDeviceTokensTable.tokenId eq tokenId) and (UserDeviceTokensTable.userId eq userId) }) { it[isActive] = false } > 0
    }

    override fun enabledRules(ctx: RequestContext, trigger: NotificationTriggerType, entity: NotificationEntityType?, event: NotificationEventType?) = transaction(db(ctx)) {
        var op: Op<Boolean> = (NotificationRulesTable.isEnabled eq true) and (NotificationRulesTable.triggerType eq trigger.name)
        entity?.let { op = op and (NotificationRulesTable.entityType eq it.name) }
        event?.let { op = op and (NotificationRulesTable.eventType eq it.name) }
        NotificationRulesTable.select { op }.map(::toRule)
    }

    override fun snapshots(ctx: RequestContext, entity: NotificationEntityType) = transaction(db(ctx)) { snapshotsInTransaction(entity, null) }
    override fun snapshot(ctx: RequestContext, entity: NotificationEntityType, id: String) = transaction(db(ctx)) { snapshotsInTransaction(entity, id).singleOrNull() }

    override fun recipients(ctx: RequestContext, companyId: Int, rule: NotificationRule, snapshot: EntitySnapshot): List<String> = when (rule.recipientType) {
        NotificationRecipientType.ASSIGNED_USER -> listOfNotNull(snapshot.assignedUserId).filter { userExists(ctx, it) }
        NotificationRecipientType.SPECIFIC_USER -> listOfNotNull(rule.specificUserId).filter { userExists(ctx, it) }
        NotificationRecipientType.ADMINISTRATORS -> {
            val authIds = transaction(DatabaseFactory.centralDb) {
                UserTable.slice(UserTable.id).select { (UserTable.companyId eq companyId) and (UserTable.status eq "active") and (UserTable.role inList listOf("owner", "admin")) }.map { it[UserTable.id] }
            }
            transaction(db(ctx)) { userTable.slice(userTable.userId).select { (userTable.authUserId inList authIds) and (userTable.isDeleted eq false) }.map { it[userTable.userId] } }
        }
    }.distinct()

    override fun enqueue(ctx: RequestContext, rule: NotificationRule, snapshot: EntitySnapshot, recipient: String, scheduledAt: Long, sourceValue: String, eventKey: String?) = transaction(db(ctx)) {
        val key = listOf(rule.ruleId, snapshot.entityType.name, snapshot.entityId, recipient, eventKey ?: sourceValue).joinToString(":")
        if (!NotificationJobsTable.select { NotificationJobsTable.idempotencyKey eq key }.any()) NotificationJobsTable.insert {
            it[jobId] = UUID.randomUUID().toString(); it[ruleId] = rule.ruleId; it[entityType] = snapshot.entityType.name; it[entityId] = snapshot.entityId
            it[recipientUserId] = recipient; it[scheduledAtEpochMs] = scheduledAt; it[status] = NotificationJobStatus.PENDING.name
            it[attempts] = 0; it[maxAttempts] = 5; it[NotificationJobsTable.sourceValue] = sourceValue; it[idempotencyKey] = key; it[dateCreatedEpochMs] = System.currentTimeMillis()
            if (rule.triggerType == NotificationTriggerType.EVENT) {
                it[renderedTitle] = NotificationTemplateRenderer.render(rule.titleTemplate, snapshot)
                it[renderedMessage] = NotificationTemplateRenderer.render(rule.messageTemplate, snapshot)
            }
        }
    }

    override fun cancelObsoleteDateJobs(ctx: RequestContext, rule: NotificationRule, snapshot: EntitySnapshot, currentKey: String) = transaction(db(ctx)) {
        val now = System.currentTimeMillis()
        NotificationJobsTable.update({ (NotificationJobsTable.ruleId eq rule.ruleId) and (NotificationJobsTable.entityId eq snapshot.entityId) and
            (NotificationJobsTable.status inList listOf("PENDING", "RETRY")) and (NotificationJobsTable.sourceValue neq currentKey) }) {
            it[status] = NotificationJobStatus.CANCELLED.name; it[cancelledAtEpochMs] = now; it[cancelReason] = "Source date changed"
        }
        Unit
    }

    override fun claimDue(ctx: RequestContext, workerId: String, now: Long, limit: Int): List<NotificationJobResponse> = transaction(db(ctx)) {
        val safeWorker = workerId.replace("'", "")
        exec("""UPDATE "notification_jobs" SET "Status"='PROCESSING', "ClaimedBy"='$safeWorker', "ClaimedAtEpochMs"=$now, "LeaseUntilEpochMs"=${now + 120_000}
            WHERE "JobId" IN (SELECT "JobId" FROM "notification_jobs" WHERE (("Status" IN ('PENDING','RETRY') AND COALESCE("NextAttemptAtEpochMs","ScheduledAtEpochMs") <= $now) OR ("Status"='PROCESSING' AND "LeaseUntilEpochMs" < $now)) ORDER BY "ScheduledAtEpochMs" FOR UPDATE SKIP LOCKED LIMIT $limit)""".trimIndent())
        NotificationJobsTable.select { (NotificationJobsTable.claimedBy eq workerId) and (NotificationJobsTable.status eq "PROCESSING") and (NotificationJobsTable.claimedAtEpochMs eq now) }.map(::toJob)
    }

    override fun completeJob(ctx: RequestContext, jobId: String, snapshot: EntitySnapshot?, rule: NotificationRule?) = transaction(db(ctx)) {
        val job = NotificationJobsTable.select { NotificationJobsTable.jobId eq jobId }.singleOrNull() ?: return@transaction
        val idempotency = job[NotificationJobsTable.idempotencyKey]
        if (!notificationsTable.select { notificationsTable.idempotencyKey eq idempotency }.any()) notificationsTable.insert {
            it[notificationId] = UUID.randomUUID().toString(); it[userId] = job[NotificationJobsTable.recipientUserId]
            it[title] = NotificationTemplateRenderer.renderForDelivery(rule?.titleTemplate ?: "Notification", snapshot, job[NotificationJobsTable.renderedTitle])
            it[message] = NotificationTemplateRenderer.renderForDelivery(rule?.messageTemplate ?: "", snapshot, job[NotificationJobsTable.renderedMessage])
            it[type] = rule?.triggerType?.name; it[entityType] = job[NotificationJobsTable.entityType]; it[entityId] = job[NotificationJobsTable.entityId]
            it[isRead] = false; it[notificationsTable.jobId] = jobId; it[idempotencyKey] = idempotency; it[createdAtEpochMs] = System.currentTimeMillis(); it[dateCreated] = DateUtils.nowStorage()
        }
        NotificationJobsTable.update({ NotificationJobsTable.jobId eq jobId }) { it[status] = "COMPLETED"; it[processedAtEpochMs] = System.currentTimeMillis(); it[leaseUntilEpochMs] = null }
    }
    override fun skipJob(ctx: RequestContext, jobId: String, reason: String) = setJobTerminal(ctx, jobId, "SKIPPED", reason)
    override fun failJob(ctx: RequestContext, jobId: String, error: String) = transaction(db(ctx)) {
        val row = NotificationJobsTable.select { NotificationJobsTable.jobId eq jobId }.singleOrNull() ?: return@transaction
        val attempts = row[NotificationJobsTable.attempts] + 1; val terminal = attempts >= row[NotificationJobsTable.maxAttempts]
        NotificationJobsTable.update({ NotificationJobsTable.jobId eq jobId }) {
            it[NotificationJobsTable.attempts] = attempts; it[status] = if (terminal) "FAILED" else "RETRY"; it[lastError] = error.take(2000)
            it[nextAttemptAtEpochMs] = if (terminal) null else System.currentTimeMillis() + attempts * 60_000L; it[leaseUntilEpochMs] = null
        }
    }

    private fun setJobTerminal(ctx: RequestContext, id: String, status: String, reason: String) = transaction(db(ctx)) {
        NotificationJobsTable.update({ NotificationJobsTable.jobId eq id }) { it[NotificationJobsTable.status] = status; it[cancelReason] = reason; it[processedAtEpochMs] = System.currentTimeMillis(); it[leaseUntilEpochMs] = null }; Unit
    }
    private fun cancelRuleJobs(id: String, reason: String) { NotificationJobsTable.update({ (NotificationJobsTable.ruleId eq id) and (NotificationJobsTable.status inList listOf("PENDING", "RETRY")) }) { it[status] = "CANCELLED"; it[cancelReason] = reason; it[cancelledAtEpochMs] = System.currentTimeMillis() } }

    private fun snapshotsInTransaction(entity: NotificationEntityType, id: String?): List<EntitySnapshot> = when (entity) {
        NotificationEntityType.CONTRACT -> contractsTable.select { (id?.let { contractsTable.contractId eq it } ?: Op.TRUE) }.map {
            val entityId = it[contractsTable.contractId]; val title = it[contractsTable.title]; val status = it[contractsTable.contractStatus]
            EntitySnapshot(entity, entityId, title ?: "Contract", status, null, mapOf("DATE_START" to it[contractsTable.dateStart], "DATE_END" to it[contractsTable.dateEnd], "DATE_CREATED" to it[contractsTable.dateCreated]), it[contractsTable.isDeleted], mapOf(
                "contract.id" to entityId, "contract.title" to title, "contract.status" to status, "contract.dateStart" to it[contractsTable.dateStart], "contract.dateEnd" to it[contractsTable.dateEnd], "contract.dateCreated" to it[contractsTable.dateCreated]
            ))
        }
        NotificationEntityType.WORK_ORDER -> fieldReportsTable.select { (id?.let { fieldReportsTable.fieldReportId eq it } ?: Op.TRUE) }.map {
            val entityId = it[fieldReportsTable.fieldReportId]; val reportNumber = it[fieldReportsTable.reportNumber]; val title = it[fieldReportsTable.title]; val status = it[fieldReportsTable.reportStatus]
            EntitySnapshot(entity, entityId, title ?: reportNumber ?: "Work order", status, it[fieldReportsTable.userID], mapOf("SCHEDULED_START" to it[fieldReportsTable.scheduledStart], "SCHEDULED_END" to it[fieldReportsTable.scheduledEnd], "DATE_CREATED" to it[fieldReportsTable.dateCreated]), it[fieldReportsTable.isDeleted], mapOf(
                "workOrder.id" to entityId, "workOrder.reportNumber" to reportNumber, "workOrder.title" to title, "workOrder.status" to status, "workOrder.scheduledStart" to it[fieldReportsTable.scheduledStart], "workOrder.scheduledEnd" to it[fieldReportsTable.scheduledEnd], "workOrder.dateCreated" to it[fieldReportsTable.dateCreated]
            ))
        }
        NotificationEntityType.TECHNICAL_CASE -> ticketTable.select { (id?.let { ticketTable.ticketId eq it } ?: Op.TRUE) }.map {
            val entityId = it[ticketTable.ticketId]; val ticketNumber = it[ticketTable.ticketNumber]; val title = it[ticketTable.title]; val status = it[ticketTable.status]
            EntitySnapshot(entity, entityId, title ?: ticketNumber ?: "Technical case", status, it[ticketTable.userId], mapOf("DATE_START" to it[ticketTable.dateStart], "DATE_END" to it[ticketTable.dateEnd], "DATE_CREATED" to it[ticketTable.dateCreated]), it[ticketTable.isDeleted], mapOf(
                "technicalCase.id" to entityId, "technicalCase.ticketNumber" to ticketNumber, "technicalCase.title" to title, "technicalCase.status" to status, "technicalCase.dateStart" to it[ticketTable.dateStart], "technicalCase.dateEnd" to it[ticketTable.dateEnd], "technicalCase.dateCreated" to it[ticketTable.dateCreated]
            ))
        }
        NotificationEntityType.TASK -> tasksTable.select { (id?.let { tasksTable.taskId eq it } ?: Op.TRUE) }.map {
            val entityId = it[tasksTable.taskId]; val title = it[tasksTable.title]; val status = it[tasksTable.status]
            EntitySnapshot(entity, entityId, title ?: "Task", status, it[tasksTable.userId], mapOf("DATE_START" to it[tasksTable.dateStart], "DUE_DATE" to it[tasksTable.dateDue], "DATE_COMPLETED" to it[tasksTable.dateCompleted], "DATE_CREATED" to it[tasksTable.dateCreated]), it[tasksTable.isDeleted], mapOf(
                "task.id" to entityId, "task.title" to title, "task.status" to status, "task.dateStart" to it[tasksTable.dateStart], "task.dueDate" to it[tasksTable.dateDue], "task.dateCompleted" to it[tasksTable.dateCompleted], "task.dateCreated" to it[tasksTable.dateCreated]
            ))
        }
    }

    private fun fillRule(row: UpdateBuilder<*>, r: CreateNotificationRuleRequest, now: Long) { with(NotificationRulesTable) {
        row[name] = r.name.trim(); row[entityType] = r.entityType.uppercase(); row[triggerType] = r.triggerType.uppercase(); row[eventType] = r.eventType?.uppercase()
        row[dateField] = r.dateField?.uppercase(); row[offsetValue] = r.offsetValue; row[offsetUnit] = r.offsetUnit?.uppercase(); row[offsetDirection] = r.offsetDirection?.uppercase()
        row[conditionsJson] = json.encodeToString(r.conditions); row[recipientType] = r.recipientType.uppercase(); row[specificUserId] = r.specificUserId
        row[titleTemplate] = r.titleTemplate; row[messageTemplate] = r.messageTemplate; row[isEnabled] = r.isEnabled; row[lastModifiedEpochMs] = now
    } }
    private fun ruleRow(id: String) = NotificationRulesTable.select { NotificationRulesTable.ruleId eq id }.singleOrNull()?.let(::toRuleResponse)
    private fun toRuleResponse(r: ResultRow) = NotificationRuleResponse(r[NotificationRulesTable.ruleId], r[NotificationRulesTable.name], r[NotificationRulesTable.entityType], r[NotificationRulesTable.triggerType], r[NotificationRulesTable.eventType], r[NotificationRulesTable.dateField], r[NotificationRulesTable.offsetValue], r[NotificationRulesTable.offsetUnit], r[NotificationRulesTable.offsetDirection], json.decodeFromString(r[NotificationRulesTable.conditionsJson]), r[NotificationRulesTable.recipientType], r[NotificationRulesTable.specificUserId], r[NotificationRulesTable.titleTemplate], r[NotificationRulesTable.messageTemplate], r[NotificationRulesTable.isEnabled], r[NotificationRulesTable.dateCreatedEpochMs], r[NotificationRulesTable.lastModifiedEpochMs])
    private fun toRule(r: ResultRow) = toRuleResponse(r).let { NotificationRule(it.ruleId, it.name, NotificationEntityType.valueOf(it.entityType), NotificationTriggerType.valueOf(it.triggerType), it.eventType?.let(NotificationEventType::valueOf), it.dateField, it.offsetValue, it.offsetUnit?.let(NotificationOffsetUnit::valueOf), it.offsetDirection?.let(NotificationOffsetDirection::valueOf), it.conditions, NotificationRecipientType.valueOf(it.recipientType), it.specificUserId, it.titleTemplate, it.messageTemplate, it.isEnabled, it.dateCreatedEpochMs, it.lastModifiedEpochMs) }
    private fun toJob(r: ResultRow) = NotificationJobResponse(r[NotificationJobsTable.jobId], r[NotificationJobsTable.ruleId], r[NotificationJobsTable.entityType], r[NotificationJobsTable.entityId], r[NotificationJobsTable.recipientUserId], r[NotificationJobsTable.scheduledAtEpochMs], r[NotificationJobsTable.status], r[NotificationJobsTable.attempts], r[NotificationJobsTable.lastError], r[NotificationJobsTable.renderedTitle], r[NotificationJobsTable.renderedMessage])
    private fun UpdateNotificationRuleRequest.toCreate() = CreateNotificationRuleRequest(name, entityType, triggerType, eventType, dateField, offsetValue, offsetUnit, offsetDirection, conditions, recipientType, specificUserId, titleTemplate, messageTemplate, isEnabled)
}
