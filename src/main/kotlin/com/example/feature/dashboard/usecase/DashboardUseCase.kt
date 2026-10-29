package com.example.feature.dashboard.usecase

import com.example.core.RequestContext
import com.example.feature.dashboard.dto.*
import com.example.feature.dashboard.repository.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

class DashboardUseCase(private val repository: DashboardRepository, private val clock: Clock = Clock.systemUTC()) {
    fun knowledgeBase(
        ctx: RequestContext,
        period: String,
        customerIds: Set<String>,
        limit: Int
    ): KnowledgeBaseOverview? {
        val to = LocalDate.now(clock)
        val normalizedPeriod = period.trim().lowercase()
        val from = when (normalizedPeriod) {
            "last30days" -> to.minusDays(29)
            "last3months" -> to.minusMonths(3).plusDays(1)
            "last6months" -> to.minusMonths(6).plusDays(1)
            "last12months" -> to.minusMonths(12).plusDays(1)
            else -> return null
        }
        val dayCount = java.time.temporal.ChronoUnit.DAYS.between(from, to)
        val previousTo = from.minusDays(1)
        val previousFrom = previousTo.minusDays(dayCount)
        val allRows = repository.technicalCases(ctx)
        val availableCustomers = allRows
            .filter { parseDate(it.created)?.let { date -> date in from..to } == true }
            .mapNotNull { row -> row.customerId?.let { KnowledgeBaseCustomerOption(it, row.customerName) } }
            .distinctBy { it.customerId }
            .sortedWith(compareBy<KnowledgeBaseCustomerOption> { it.customerName?.lowercase() ?: "" }.thenBy { it.customerId })
        val selectedRows = allRows.filter { customerIds.isEmpty() || it.customerId in customerIds }
        val current = selectedRows.filter { parseDate(it.created)?.let { date -> date in from..to } == true }
        val previous = selectedRows.filter { parseDate(it.created)?.let { date -> date in previousFrom..previousTo } == true }
        val settings = repository.statusSettings(ctx, "TechnicalCaseStatus")

        return KnowledgeBaseOverview(
            range = DashboardDateRange(from.toString(), to.toString()),
            previousRange = DashboardDateRange(previousFrom.toString(), previousTo.toString()),
            filters = KnowledgeBaseFilters(period = when (normalizedPeriod) {
                "last30days" -> "last30Days"
                "last3months" -> "last3Months"
                "last12months" -> "last12Months"
                else -> "last6Months"
            }, customerIds = customerIds.sorted(), availableCustomers = availableCustomers),
            mostFrequentEquipment = rankMetric(current, previous, limit) { it.equipmentModel },
            mostFrequentManufacturer = rankMetric(current, previous, limit) { it.equipmentManufacturer },
            topIncidentTypes = rankMetric(current, previous, limit) { it.type },
            incidentStatus = statusMetric(current, previous, settings)
        )
    }

    fun technicianWorkloadSummary(ctx: RequestContext, from: LocalDate, to: LocalDate): TechnicianWorkloadSummary {
        val data = workloadData(ctx, from, to)
        val previousTo = from.minusDays(1)
        val previousFrom = previousTo.minusDays(java.time.temporal.ChronoUnit.DAYS.between(from, to))
        val previous = workloadData(ctx, previousFrom, previousTo)
        return TechnicianWorkloadSummary(
            range = DashboardDateRange(from.toString(), to.toString()),
            activeAssignments = metric(data.active, previous.active),
            overdueAssignments = metric(data.overdue, previous.overdue),
            highPriorityAssignments = metric(data.highPriority, previous.highPriority),
            technicians = technicianMetric(data.active, previous.active)
        )
    }

    fun technicianWorkloadCharts(ctx: RequestContext, from: LocalDate, to: LocalDate): TechnicianWorkloadCharts {
        val data = workloadData(ctx, from, to)
        val technicians = repository.workloadTechnicians(ctx).associateBy { it.userId }
        val items = data.active.filter { it.userId != null }.groupBy { it.userId!! }.map { (userId, rows) ->
            TechnicianWorkloadItem(
                userId = userId,
                name = technicians[userId]?.name?.takeIf { it.isNotBlank() } ?: "Unknown technician",
                technicalCases = rows.count { it.type == AssignmentType.TECHNICAL_CASE },
                workOrders = rows.count { it.type == AssignmentType.WORK_ORDER },
                tasks = rows.count { it.type == AssignmentType.TASK },
                total = rows.size,
                scheduledHours = rows.filter { it.type == AssignmentType.WORK_ORDER }
                    .sumOf { com.example.core.DateUtils.durationHours(it.start, it.due) }
            )
        }.sortedWith(compareByDescending<TechnicianWorkloadItem> { it.total }.thenBy { it.name.lowercase() }.thenBy { it.userId })
        return TechnicianWorkloadCharts(
            range = DashboardDateRange(from.toString(), to.toString()),
            workloadByTechnician = items,
            assignmentsByType = typeTotals(data.active)
        )
    }

    fun technicianDetails(ctx: RequestContext, from: LocalDate, to: LocalDate): TechnicianDetails {
        val data = workloadData(ctx, from, to)
        val technicians = repository.workloadTechnicians(ctx).associateBy { it.userId }
        val items = data.active.mapNotNull { row -> row.userId?.let { it to row } }
            .groupBy({ it.first }, { it.second })
            .map { (userId, rows) ->
                val overdueKeys = data.overdue.filter { it.userId == userId }.map { it.type to it.id }.toSet()
                val highPriorityKeys = data.highPriority.filter { it.userId == userId }.map { it.type to it.id }.toSet()
                TechnicianDetailItem(
                    userId = userId,
                    name = technicians[userId]?.name?.takeIf { it.isNotBlank() } ?: "Unknown technician",
                    total = rows.size,
                    technicalCases = rows.count { it.type == AssignmentType.TECHNICAL_CASE },
                    workOrders = rows.count { it.type == AssignmentType.WORK_ORDER },
                    tasks = rows.count { it.type == AssignmentType.TASK },
                    overdue = rows.count { (it.type to it.id) in overdueKeys },
                    highPriority = rows.count { (it.type to it.id) in highPriorityKeys }
                )
            }
            .sortedWith(compareByDescending<TechnicianDetailItem> { it.total }.thenBy { it.name.lowercase() }.thenBy { it.userId })
        return TechnicianDetails(DashboardDateRange(from.toString(), to.toString()), items)
    }

    fun upcomingAssignments(ctx: RequestContext, type: AssignmentType?): List<UpcomingAssignment> {
        val technicians = repository.workloadTechnicians(ctx).associateBy { it.userId }
        val statusSettings = mapOf(
            AssignmentType.TECHNICAL_CASE to repository.statusSettings(ctx, "TechnicalCaseStatus"),
            AssignmentType.WORK_ORDER to repository.statusSettings(ctx, "WorkOrderStatus"),
            AssignmentType.TASK to repository.statusSettings(ctx, "TaskStatus")
        )
        val now = Instant.now(clock)
        return repository.workloadAssignments(ctx)
            .asSequence()
            .filter { type == null || it.type == type }
            .filter { category(it.status, statusSettings[it.type].orEmpty()) == "OPEN" }
            .mapNotNull { row ->
                val start = assignmentInstant(row.start)
                val due = assignmentInstant(row.due)
                val scheduledAt = start ?: due ?: return@mapNotNull null
                if (scheduledAt.isBefore(now) && (due == null || due.isBefore(now))) null
                else Triple(row, scheduledAt, due)
            }
            .groupBy { it.first.type to it.first.id }
            .map { (_, matches) ->
                val first = matches.first().first
                val scheduledAt = matches.minOf { it.second }
                UpcomingAssignment(
                    assignmentId = first.id,
                    assignmentType = first.type.name,
                    number = first.number,
                    title = first.title,
                    scheduledAt = scheduledAt.toString(),
                    dueAt = matches.mapNotNull { it.third }.minOrNull()?.toString(),
                    priority = priority(first.priority),
                    technicians = matches.mapNotNull { it.first.userId }.distinct().map { userId ->
                        UpcomingAssignmentTechnician(userId, technicians[userId]?.name?.takeIf { it.isNotBlank() } ?: "Unknown technician")
                    }
                )
            }
            .sortedWith(compareBy<UpcomingAssignment> { Instant.parse(it.scheduledAt) }.thenBy { it.assignmentType }.thenBy { it.assignmentId })
            .take(50)
    }

    fun summary(ctx: RequestContext): DashboardSummary {
        val now = LocalDate.now(clock); val month = YearMonth.from(now); val previous = month.minusMonths(1)
        val work = repository.workOrders(ctx); val workSettings = repository.statusSettings(ctx, "WorkOrderStatus"); val cases = repository.technicalCases(ctx); val caseSettings = repository.statusSettings(ctx, "TechnicalCaseStatus"); val contracts = repository.contracts(ctx)
        val workCurrent = work.count { parseDate(it.created)?.let(YearMonth::from) == month }; val workPrevious = work.count { parseDate(it.created)?.let(YearMonth::from) == previous }
        val caseCurrent = cases.count { parseDate(it.created)?.let(YearMonth::from) == month }; val casePrevious = cases.count { parseDate(it.created)?.let(YearMonth::from) == previous }
        val contractCurrent = contracts.count { parseDate(it.created)?.let(YearMonth::from) == month }; val contractPrevious = contracts.count { parseDate(it.created)?.let(YearMonth::from) == previous }
        return DashboardSummary(
            WorkOrderSummaryMetric(
                total = work.size,
                open = work.count { resolveStatus(it.status, workSettings).systemCategory == "OPEN" },
                overdue = work.count { resolveStatus(it.status, workSettings).systemCategory == "OPEN" && parseDate(it.completedOrDue)?.isBefore(now) == true },
                statuses = statusItems(work, workSettings),
                changePercentage = change(workCurrent, workPrevious)
            ),
            ContractMetric(contracts.count { contractActive(it.status) }, contractCurrent, change(contractCurrent, contractPrevious)),
            TechnicalCaseSummaryMetric(
                total = cases.size,
                open = cases.count { resolveTechnicalCaseStatus(it, caseSettings).systemCategory == "OPEN" },
                overdue = cases.count { resolveTechnicalCaseStatus(it, caseSettings).systemCategory == "OPEN" && parseDate(it.due)?.isBefore(now) == true },
                statuses = technicalCaseStatusItems(cases, caseSettings),
                changePercentage = change(caseCurrent, casePrevious)
            )
        )
    }
    fun workOrders(ctx: RequestContext, year: Int): WorkOrderAnalytics {
        val rows = repository.workOrders(ctx); val settings = repository.statusSettings(ctx, "WorkOrderStatus"); val typeSettings = repository.statusSettings(ctx, "WorkOrderType"); val typeCounts = repository.workOrderTypeCounts(ctx, year); val today = LocalDate.now(clock); val monthLabels = java.time.format.TextStyle.SHORT
        val months = (1..12).map { m ->
            val ym = YearMonth.of(year, m)
            MonthlyWorkOrders(ym.toString(), Month.of(m).getDisplayName(monthLabels, Locale.ENGLISH), rows.count { parseDate(it.created)?.let(YearMonth::from) == ym }, rows.count { resolveStatus(it.status, settings).systemCategory == "CLOSED" && parseDate(it.completedOrDue)?.let(YearMonth::from) == ym })
        }
        val preferredTypes = preferredSettings(typeSettings)
        val typesById = typeSettings.associateBy { it.id.lowercase() }
        val countsBySettingAndPeriod = typeCounts.mapNotNull { count ->
            val storedType = count.typeValue.trim().lowercase()
            (typesById[storedType] ?: preferredTypes[storedType])?.let { setting ->
                (setting.id to count.period) to count.count
            }
        }.groupingBy { it.first }.fold(0) { total, item -> total + item.second }
        val visibleTypes = preferredTypes.values
            .filter { setting -> !setting.isDeleted || countsBySettingAndPeriod.keys.any { it.first == setting.id } }
            .sortedBy { it.value.lowercase() }
        val reportsByType = ReportsByType(
            types = visibleTypes.map { WorkOrderTypeItem(it.id, it.value, it.style) },
            periods = (1..12).map { month ->
                val period = YearMonth.of(year, month).toString()
                WorkOrderTypePeriod(
                    period = period,
                    label = Month.of(month).getDisplayName(monthLabels, Locale.ENGLISH),
                    counts = visibleTypes.associate { setting ->
                        setting.id to (countsBySettingAndPeriod[setting.id to period] ?: 0)
                    }
                )
            }
        )
        return WorkOrderAnalytics(reportsByType, months, WorkOrderStatusDistribution(
            total = rows.size,
            open = rows.count { resolveStatus(it.status, settings).systemCategory == "OPEN" },
            overdue = rows.count { resolveStatus(it.status, settings).systemCategory == "OPEN" && parseDate(it.completedOrDue)?.isBefore(today) == true },
            statuses = statusItems(rows, settings)
        ))
    }
    fun technicalCases(ctx: RequestContext, limit: Int): TechnicalCasesDashboard {
        val rows = repository.technicalCases(ctx); val settings = repository.statusSettings(ctx, "TechnicalCaseStatus"); val today = LocalDate.now(clock)
        return TechnicalCasesDashboard(
            TechnicalCaseStatusDistribution(
                total = rows.size,
                open = rows.count { resolveTechnicalCaseStatus(it, settings).systemCategory == "OPEN" },
                overdue = rows.count { resolveTechnicalCaseStatus(it, settings).systemCategory == "OPEN" && parseDate(it.due)?.isBefore(today) == true },
                statuses = technicalCaseStatusItems(rows, settings)
            ),
            rows.sortedWith(compareByDescending<TechnicalCaseRow> { parseInstant(it.created) }.thenByDescending { it.id }).take(limit).map {
                val resolved = resolveTechnicalCaseStatus(it, settings)
                RecentTechnicalCase(it.id, it.number, it.title, it.customerId, it.customerName, resolved.value, resolved.systemCategory, resolved.style, it.type, isoDate(it.due), priority(it.urgency))
            }
        )
    }
    fun upcomingTasks(ctx: RequestContext, limit: Int): List<UpcomingTask> {
        val today = LocalDate.now(clock)
        return repository.tasks(ctx).filter { it.status.normalized() !in setOf("completed", "complete", "closed", "cancelled", "canceled") }.sortedWith(compareBy<TaskRow> { val d = parseDate(it.due); when { d == null -> 3; d.isBefore(today) -> 0; d == today -> 1; else -> 2 } }.thenBy { parseDate(it.due) ?: LocalDate.MAX }.thenBy { it.id }).take(limit).map { UpcomingTask(it.id, it.title, taskStatus(it.status), if (it.ticketId == null) null else "technicalCase", it.ticketId, it.ticketNumber, isoDate(it.due), priority(it.priority), it.type) }
    }
    fun completeTask(ctx: RequestContext, id: String): CompletedTask? { val now = Instant.now(clock).toString(); return when (repository.completeTask(ctx, id, storageInstant(now))) { null -> null; false -> throw IllegalStateException("Task cannot be completed"); true -> CompletedTask(id, "completed", now) } }
    fun dueDates(ctx: RequestContext): DueDates {
        val today = LocalDate.now(clock); val start = today.with(java.time.DayOfWeek.MONDAY); val end = start.plusDays(6); val month = YearMonth.from(today)
        val workSettings = repository.statusSettings(ctx, "WorkOrderStatus"); val caseSettings = repository.statusSettings(ctx, "TechnicalCaseStatus")
        return DueDates(repository.workOrders(ctx).count { resolveStatus(it.status, workSettings).systemCategory == "OPEN" && parseDate(it.completedOrDue)?.let { date -> date in start..end } == true }, repository.technicalCases(ctx).count { resolveTechnicalCaseStatus(it, caseSettings).systemCategory == "OPEN" && parseDate(it.due)?.let { date -> date in start..end } == true }, repository.contracts(ctx).count { contractActive(it.status) && parseDate(it.end)?.let(YearMonth::from) == month })
    }
    fun topCustomers(ctx: RequestContext, limit: Int): List<TopCustomer> { val month = YearMonth.now(clock); return repository.workOrders(ctx).filter { it.customerId != null && parseDate(it.created)?.let(YearMonth::from) == month }.groupBy { it.customerId!! to it.customerName }.map { TopCustomer(it.key.first, it.key.second, it.value.size) }.sortedWith(compareByDescending<TopCustomer> { it.workOrderCount }.thenBy { it.customerName ?: "" }.thenBy { it.customerId }).take(limit) }
    private fun distribution(values: List<String>, order: List<String>) = StatusDistribution(values.size, order.map { status -> StatusCount(status, values.count { it == status }) })
    private fun statusItems(rows: List<WorkOrderRow>, settings: List<StatusSettingRow>): List<DashboardStatusItem> {
        val preferred = preferredSettings(settings)
        val counts = rows.groupingBy { it.status?.trim()?.lowercase().orEmpty() }.eachCount()
        val activeItems = preferred.values.filter { !it.isDeleted }.map { setting ->
            DashboardStatusItem(setting.id, setting.value, setting.systemCategory, setting.style, counts[setting.value.trim().lowercase()] ?: 0)
        }
        val represented = activeItems.map { it.value.trim().lowercase() }.toSet()
        val historicalOrUnknown = counts.filterKeys { it !in represented }.map { (normalizedValue, count) ->
            val setting = preferred[normalizedValue]
            DashboardStatusItem(setting?.id, setting?.value ?: normalizedValue.ifBlank { "Unspecified" }, setting?.systemCategory, setting?.style, count)
        }
        return (activeItems + historicalOrUnknown).sortedWith(compareBy<DashboardStatusItem> { it.systemCategory ?: "ZZZ" }.thenBy { it.value.lowercase() })
    }
    private fun technicalCaseStatusItems(rows: List<TechnicalCaseRow>, settings: List<StatusSettingRow>): List<DashboardStatusItem> {
        val activeSettings = preferredSettings(settings).values.filter { !it.isDeleted }
        val resolvedRows = rows.map { resolveTechnicalCaseStatus(it, settings) }
        val counts = resolvedRows.groupingBy { statusIdentity(it) }.eachCount()
        val activeItems = activeSettings.map { setting ->
            DashboardStatusItem(setting.id, setting.value, setting.systemCategory, setting.style, counts[statusIdentity(setting)] ?: 0)
        }
        val represented = activeItems.map { statusIdentity(it) }.toSet()
        val historicalOrUnknown = resolvedRows.distinctBy(::statusIdentity).filter { statusIdentity(it) !in represented }.map { setting ->
            DashboardStatusItem(setting.id.takeIf { it.isNotBlank() }, setting.value.ifBlank { "Unspecified" }, setting.systemCategory, setting.style, counts[statusIdentity(setting)] ?: 0)
        }
        return (activeItems + historicalOrUnknown).sortedWith(compareBy<DashboardStatusItem> { it.systemCategory ?: "ZZZ" }.thenBy { it.value.lowercase() })
    }
    private fun change(current: Int, previous: Int) = if (previous == 0) if (current == 0) 0 else 100 else (((current - previous) * 100.0) / previous).roundToInt()

    private fun rankMetric(
        current: List<TechnicalCaseRow>,
        previous: List<TechnicalCaseRow>,
        limit: Int,
        value: (TechnicalCaseRow) -> String?
    ): KnowledgeBaseRankMetric {
        fun counts(rows: List<TechnicalCaseRow>) = rows.mapNotNull(value)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .groupingBy { it.lowercase() }
            .eachCount()
        val labels = current.mapNotNull(value).map(String::trim).filter(String::isNotEmpty)
            .associateBy { it.lowercase() }
        val currentCounts = counts(current)
        val previousCounts = counts(previous)
        val items = currentCounts.map { (key, count) -> KnowledgeBaseRankItem(labels.getValue(key), count) }
            .sortedWith(compareByDescending<KnowledgeBaseRankItem> { it.count }.thenBy { it.value.lowercase() })
            .take(limit)
        val leading = items.firstOrNull()
        return KnowledgeBaseRankMetric(
            totalIncidents = current.size,
            leadingValue = leading?.value,
            leadingCount = leading?.count ?: 0,
            changePercentage = leading?.let { change(it.count, previousCounts[it.value.lowercase()] ?: 0) } ?: 0,
            items = items
        )
    }

    private fun statusMetric(
        current: List<TechnicalCaseRow>,
        previous: List<TechnicalCaseRow>,
        settings: List<StatusSettingRow>
    ): KnowledgeBaseStatusMetric {
        val currentResolved = current.map { resolveTechnicalCaseStatus(it, settings) }
        val previousCounts = previous.map { resolveTechnicalCaseStatus(it, settings) }
            .groupingBy(::statusIdentity).eachCount()
        val metadata = currentResolved.distinctBy(::statusIdentity).associateBy(::statusIdentity)
        val items = currentResolved.groupingBy(::statusIdentity).eachCount().map { (identity, count) ->
            val setting = metadata.getValue(identity)
            KnowledgeBaseStatusItem(
                settingId = setting.id.takeIf(String::isNotBlank),
                value = setting.value.ifBlank { "Unspecified" },
                systemCategory = setting.systemCategory,
                style = setting.style,
                count = count,
                percentage = percentage(count, current.size)
            )
        }.sortedWith(compareByDescending<KnowledgeBaseStatusItem> { it.count }.thenBy { it.value.lowercase() })
        val leading = items.firstOrNull()
        val leadingIdentity = leading?.let { item -> item.settingId ?: "legacy:${item.value.trim().lowercase()}:${item.systemCategory.orEmpty()}" }
        return KnowledgeBaseStatusMetric(
            totalIncidents = current.size,
            leadingValue = leading?.value,
            leadingCount = leading?.count ?: 0,
            leadingPercentage = leading?.percentage ?: 0,
            changePercentage = leading?.let { change(it.count, previousCounts[leadingIdentity] ?: 0) } ?: 0,
            items = items
        )
    }

    private fun percentage(count: Int, total: Int) = if (total == 0) 0 else ((count * 100.0) / total).roundToInt()

    private fun workloadData(ctx: RequestContext, from: LocalDate, to: LocalDate): WorkloadData {
        val rows = repository.workloadAssignments(ctx).filter { overlaps(it, from, to) }
        val statusSettings = mapOf(
            AssignmentType.TECHNICAL_CASE to repository.statusSettings(ctx, "TechnicalCaseStatus"),
            AssignmentType.WORK_ORDER to repository.statusSettings(ctx, "WorkOrderStatus"),
            AssignmentType.TASK to repository.statusSettings(ctx, "TaskStatus")
        )
        val prioritySettings = mapOf(
            AssignmentType.TECHNICAL_CASE to repository.statusSettings(ctx, "TechnicalCasePriority"),
            AssignmentType.WORK_ORDER to repository.statusSettings(ctx, "WorkOrderPriority"),
            AssignmentType.TASK to repository.statusSettings(ctx, "TaskPriority")
        )
        val active = rows.filter { category(it.status, statusSettings[it.type].orEmpty()) == "OPEN" }
        val today = LocalDate.now(clock)
        return WorkloadData(
            active = active,
            overdue = active.filter { parseDate(it.due)?.isBefore(today) == true },
            highPriority = active.filter { category(it.priority, prioritySettings[it.type].orEmpty()) == "HIGH" }
        )
    }

    private fun overlaps(row: WorkloadAssignmentRow, from: LocalDate, to: LocalDate): Boolean {
        val start = parseDate(row.start)
        val due = parseDate(row.due)
        return when {
            start != null && due != null -> !start.isAfter(to) && !due.isBefore(from)
            start != null -> start in from..to
            due != null -> due in from..to
            else -> false
        }
    }

    private fun category(value: String?, settings: List<StatusSettingRow>): String? {
        val normalized = value?.trim()?.lowercase() ?: return null
        val setting = settings.firstOrNull { it.id.lowercase() == normalized }
            ?: preferredSettings(settings)[normalized]
        return setting?.systemCategory
    }

    private fun metric(current: List<WorkloadAssignmentRow>, previous: List<WorkloadAssignmentRow>) = AssignmentMetric(
        total = current.size,
        previousTotal = previous.size,
        changePercentage = change(current.size, previous.size),
        technicalCases = current.count { it.type == AssignmentType.TECHNICAL_CASE },
        workOrders = current.count { it.type == AssignmentType.WORK_ORDER },
        tasks = current.count { it.type == AssignmentType.TASK }
    )

    private fun technicianMetric(current: List<WorkloadAssignmentRow>, previous: List<WorkloadAssignmentRow>): TechnicianMetric {
        val currentTotal = current.mapNotNull { it.userId }.distinct().size
        val previousTotal = previous.mapNotNull { it.userId }.distinct().size
        return TechnicianMetric(currentTotal, previousTotal, change(currentTotal, previousTotal))
    }

    private fun typeTotals(rows: List<WorkloadAssignmentRow>) = AssignmentsByType(
        total = rows.size,
        technicalCases = rows.count { it.type == AssignmentType.TECHNICAL_CASE },
        workOrders = rows.count { it.type == AssignmentType.WORK_ORDER },
        tasks = rows.count { it.type == AssignmentType.TASK }
    )
}

private data class WorkloadData(
    val active: List<WorkloadAssignmentRow>,
    val overdue: List<WorkloadAssignmentRow>,
    val highPriority: List<WorkloadAssignmentRow>
)

private fun preferredSettings(settings: List<StatusSettingRow>): Map<String, StatusSettingRow> = settings
    .groupBy { it.value.trim().lowercase() }
    .mapValues { (_, matches) -> matches.sortedWith(compareBy<StatusSettingRow> { it.isDeleted }.thenBy { it.id }).first() }

private fun resolveStatus(value: String?, settings: List<StatusSettingRow>): StatusSettingRow {
    val normalized = value?.trim()?.lowercase().orEmpty()
    return preferredSettings(settings)[normalized]
        ?: StatusSettingRow("", value?.trim().orEmpty(), null, null, false)
}

private fun resolveTechnicalCaseStatus(row: TechnicalCaseRow, settings: List<StatusSettingRow>): StatusSettingRow {
    val normalized = row.status?.trim()?.lowercase().orEmpty()
    val matched = preferredSettings(settings)[normalized]
    if (matched != null) return matched
    return StatusSettingRow(
        id = "",
        value = row.status?.trim()?.takeIf { it.isNotEmpty() } ?: if (row.active == false) "Resolved" else "Open",
        systemCategory = if (row.active == false) "CLOSED" else "OPEN",
        style = null,
        isDeleted = false
    )
}

private fun statusIdentity(setting: StatusSettingRow) = setting.id.takeIf { it.isNotBlank() }
    ?: "legacy:${setting.value.trim().lowercase()}:${setting.systemCategory.orEmpty()}"

private fun statusIdentity(item: DashboardStatusItem) = item.settingId
    ?: "legacy:${item.value.trim().lowercase()}:${item.systemCategory.orEmpty()}"

private val storage = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
private val legacyDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
private fun parseInstant(value: String?): Instant = value?.trim()?.let { raw ->
    runCatching { Instant.parse(raw) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, storage).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: Instant.EPOCH
} ?: Instant.EPOCH

private fun assignmentInstant(value: String?): Instant? = value?.trim()?.let { raw ->
    runCatching { Instant.parse(raw) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, storage).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, legacyDateTime).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDate.parse(raw).atStartOfDay().toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay().toInstant(ZoneOffset.UTC) }.getOrNull()
}

private fun parseDate(value: String?): LocalDate? = value?.trim()?.let { raw ->
    runCatching { LocalDateTime.parse(raw, storage).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, legacyDateTime).toLocalDate() }.getOrNull()
        ?: runCatching { Instant.parse(raw).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(raw).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(raw) }.getOrNull()
        ?: runCatching { LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
}
private fun isoDate(value: String?) = parseDate(value)?.toString()
private fun storageInstant(iso: String) = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC).format(Instant.parse(iso))
private fun String?.normalized() = this?.trim()?.lowercase()?.replace("_", "")?.replace("-", "")?.replace(" ", "") ?: ""
private fun workStatus(value: String?) = when (value.normalized()) { "completed", "complete", "done", "closed", "canceled", "cancelled" -> "completed"; "inprogress", "active" -> "inProgress"; "onhold", "paused" -> "onHold"; else -> "pending" }
private fun taskStatus(value: String?) = when (value.normalized()) { "inprogress" -> "inProgress"; "onhold" -> "onHold"; else -> value.normalized().ifBlank { "pending" } }
private fun caseStatus(status: String?, active: Boolean?) = when (status.normalized()) { "inprogress" -> "inProgress"; "resolved", "completed", "complete", "closed", "canceled", "cancelled" -> "resolved"; "onhold", "paused" -> "onHold"; "open", "pending", "new" -> "open"; else -> if (active == false) "resolved" else "open" }
private fun caseFinished(status: String?, active: Boolean?) = caseStatus(status, active) == "resolved"
private fun priority(value: String?) = when (value.normalized()) { "urgent", "critical", "high" -> "high"; "low" -> "low"; else -> "medium" }
private fun isFinished(value: String?) = workStatus(value) == "completed"
private fun contractActive(value: String?) = value.normalized() in setOf("active", "inprogress", "valid", "open")
