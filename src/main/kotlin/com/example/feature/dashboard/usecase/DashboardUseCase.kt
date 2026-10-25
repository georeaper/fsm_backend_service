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
        val rows = repository.workOrders(ctx); val settings = repository.statusSettings(ctx, "WorkOrderStatus"); val today = LocalDate.now(clock); val monthLabels = java.time.format.TextStyle.SHORT
        val months = (1..12).map { m ->
            val ym = YearMonth.of(year, m)
            MonthlyWorkOrders(ym.toString(), Month.of(m).getDisplayName(monthLabels, Locale.ENGLISH), rows.count { parseDate(it.created)?.let(YearMonth::from) == ym }, rows.count { resolveStatus(it.status, settings).systemCategory == "CLOSED" && parseDate(it.completedOrDue)?.let(YearMonth::from) == ym })
        }
        return WorkOrderAnalytics(months, WorkOrderStatusDistribution(
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
}

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
private fun parseInstant(value: String?): Instant = value?.trim()?.let { raw ->
    runCatching { Instant.parse(raw) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, storage).toInstant(ZoneOffset.UTC) }.getOrNull()
        ?: Instant.EPOCH
} ?: Instant.EPOCH

private fun parseDate(value: String?): LocalDate? = value?.trim()?.let { raw ->
    runCatching { LocalDateTime.parse(raw, storage).toLocalDate() }.getOrNull()
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
