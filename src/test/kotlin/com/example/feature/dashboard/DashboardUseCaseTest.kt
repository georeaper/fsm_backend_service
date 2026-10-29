package com.example.feature.dashboard

import com.example.core.RequestContext
import com.example.feature.dashboard.repository.*
import com.example.feature.dashboard.usecase.DashboardUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.*

class DashboardUseCaseTest {
    private val ctx = RequestContext("tenant", "user")
    private val clock = Clock.fixed(Instant.parse("2026-10-24T12:30:00Z"), ZoneOffset.UTC)

    @Test fun `year analytics always contains twelve months and maps statuses`() {
        val repo = FakeRepository(work = listOf(
            WorkOrderRow("1", "WO-1", "OPEN", "20260101000000", "20260110000000", null, null),
            WorkOrderRow("2", "WO-2", "COMPLETED", "20260102000000", "20260210000000", null, null)
        ), settingRows = listOf(
            StatusSettingRow("open", "OPEN", "OPEN", "#0000ff", false),
            StatusSettingRow("closed", "COMPLETED", "CLOSED", "#00ff00", false)
        ), typeSettingRows = listOf(
            StatusSettingRow("maintenance-id", "Maintenance", null, "#2563EB", false),
            StatusSettingRow("installation-id", "Installation", null, "#F97316", false)
        ), typeCounts = listOf(
            WorkOrderTypeCountRow("Maintenance", "2026-01", 4),
            WorkOrderTypeCountRow("installation-id", "2026-01", 2),
            WorkOrderTypeCountRow("maintenance-id", "2026-02", 3)
        ))
        val result = DashboardUseCase(repo, clock).workOrders(ctx, 2026)
        assertEquals(12, result.createdVsCompleted.size)
        assertEquals(2, result.createdVsCompleted.first().created)
        assertEquals(1, result.createdVsCompleted[1].completed)
        assertEquals(1, result.statusDistribution.open)
        assertEquals(1, result.statusDistribution.statuses.first { it.value == "OPEN" }.count)
        assertEquals("#0000ff", result.statusDistribution.statuses.first { it.value == "OPEN" }.style)
        assertEquals(listOf("installation-id", "maintenance-id"), result.reportsByType.types.map { it.settingId })
        assertEquals(4, result.reportsByType.periods.first().counts["maintenance-id"])
        assertEquals(2, result.reportsByType.periods.first().counts["installation-id"])
        assertEquals(3, result.reportsByType.periods[1].counts["maintenance-id"])
        assertEquals(0, result.reportsByType.periods[1].counts["installation-id"])
    }

    @Test fun `year analytics accepts ISO local datetime written by bulk field reports`() {
        val repo = FakeRepository(
            work = listOf(
                WorkOrderRow("1", "WO-1", "Completed", "2026-03-15T10:30:45.123", "2026-04-01T12:00:00", null, null)
            ),
            settingRows = listOf(StatusSettingRow("closed", "Completed", "CLOSED", null, false))
        )

        val result = DashboardUseCase(repo, clock).workOrders(ctx, 2026)

        assertEquals(1, result.createdVsCompleted[2].created)
        assertEquals(1, result.createdVsCompleted[3].completed)
    }

    @Test fun `zero previous period is documented as one hundred percent when current has data`() {
        val repo = FakeRepository(
            work = listOf(WorkOrderRow("1", null, "OPEN", "20261001000000", null, null, null)),
            settingRows = listOf(StatusSettingRow("open", "OPEN", "OPEN", null, false))
        )
        assertEquals(100, DashboardUseCase(repo, clock).summary(ctx).workOrders.changePercentage)
    }

    @Test fun `work order open and overdue aggregate multiple custom and deleted statuses`() {
        val repo = FakeRepository(
            work = listOf(
                WorkOrderRow("1", null, "Pending Approval", null, "20261020000000", null, null),
                WorkOrderRow("2", null, "Technician Assigned", null, "20261030000000", null, null),
                WorkOrderRow("3", null, "Legacy Open", null, "20261021000000", null, null),
                WorkOrderRow("4", null, "Completed", null, "20261020000000", null, null)
            ),
            settingRows = listOf(
                StatusSettingRow("1", "Pending Approval", "OPEN", "blue", false),
                StatusSettingRow("2", "Technician Assigned", "OPEN", "orange", false),
                StatusSettingRow("3", "Legacy Open", "OPEN", "gray", true),
                StatusSettingRow("4", "Completed", "CLOSED", "green", false)
            )
        )

        val result = DashboardUseCase(repo, clock).summary(ctx).workOrders

        assertEquals(4, result.total)
        assertEquals(3, result.open)
        assertEquals(2, result.overdue)
        assertEquals(1, result.statuses.first { it.value == "Legacy Open" }.count)
    }

    @Test fun `upcoming tasks put overdue before today future and missing dates`() {
        val repo = FakeRepository(taskRows = listOf(
            TaskRow("4", "none", "OPEN", "LOW", null, null, null),
            TaskRow("3", "future", "OPEN", "LOW", "20261025000000", null, null),
            TaskRow("1", "overdue", "OPEN", "HIGH", "20261020000000", null, null),
            TaskRow("2", "today", "OPEN", "MEDIUM", "20261024000000", null, null),
            TaskRow("5", "done", "COMPLETED", "HIGH", "20261019000000", null, null)
        ))
        assertEquals(listOf("1", "2", "3", "4"), DashboardUseCase(repo, clock).upcomingTasks(ctx, 10).map { it.taskId })
    }

    @Test fun `contract summary uses string status`() {
        val repo = FakeRepository(contractRows = listOf(
            ContractRow("ACTIVE", "20261001000000", null),
            ContractRow("INACTIVE", "20261002000000", null)
        ))

        val result = DashboardUseCase(repo, clock).summary(ctx)

        assertEquals(1, result.contracts.active)
    }

    @Test fun `technical cases aggregate custom open statuses and preserve active fallback`() {
        val repo = FakeRepository(
            cases = listOf(
                TechnicalCaseRow("1", "TC-1", "One", true, "Waiting", null, "High", "20261024000000", "20261020000000", null, null),
                TechnicalCaseRow("2", "TC-2", "Two", true, "Assigned", null, "Medium", "20261023000000", "20261030000000", null, null),
                TechnicalCaseRow("3", "TC-3", "Legacy", false, null, null, "Low", "20261022000000", null, null, null)
            ),
            settingRows = listOf(
                StatusSettingRow("waiting", "Waiting", "OPEN", "blue", false),
                StatusSettingRow("assigned", "Assigned", "OPEN", "orange", false),
                StatusSettingRow("old", "Old Open", "OPEN", "gray", true)
            )
        )

        val result = DashboardUseCase(repo, clock).technicalCases(ctx, 5)

        assertEquals(3, result.statusDistribution.total)
        assertEquals(2, result.statusDistribution.open)
        assertEquals(1, result.statusDistribution.overdue)
        assertEquals("CLOSED", result.recent.first { it.technicalCaseId == "3" }.systemCategory)
        assertTrue(result.statusDistribution.statuses.none { it.value == "Old Open" })
    }

    @Test fun `knowledge base uses equipment model manufacturer case type status period and customer filters`() {
        val repo = FakeRepository(
            cases = listOf(
                TechnicalCaseRow("1", "TC-1", "One", true, "Open", "Mechanical Failure", null, "20261020000000", null, "c1", "Acme", "e1", "CR 15-5", "Grundfos"),
                TechnicalCaseRow("2", "TC-2", "Two", false, "Resolved", "No Power", null, "20260920000000", null, "c1", "Acme", "e2", "CR 15-5", "Grundfos"),
                TechnicalCaseRow("3", "TC-3", "Three", true, "Open", "Mechanical Failure", null, "20260820000000", null, "c2", "Other", "e3", "GA 37", "Atlas Copco"),
                TechnicalCaseRow("4", "TC-4", "Previous", true, "Open", "Mechanical Failure", null, "20260420000000", null, "c1", "Acme", "e1", "CR 15-5", "Grundfos")
            ),
            settingRows = listOf(
                StatusSettingRow("open-id", "Open", "OPEN", "blue", false),
                StatusSettingRow("resolved-id", "Resolved", "CLOSED", "green", false)
            )
        )

        val result = assertNotNull(DashboardUseCase(repo, clock).knowledgeBase(ctx, "last6Months", setOf("c1"), 5))

        assertEquals("2026-04-25", result.range.from)
        assertEquals(listOf("c1"), result.filters.customerIds)
        assertEquals(listOf("c1", "c2"), result.filters.availableCustomers.map { it.customerId })
        assertEquals("CR 15-5", result.mostFrequentEquipment.leadingValue)
        assertEquals(2, result.mostFrequentEquipment.leadingCount)
        assertEquals(100, result.mostFrequentEquipment.changePercentage)
        assertEquals("Grundfos", result.mostFrequentManufacturer.leadingValue)
        assertEquals(listOf("Mechanical Failure", "No Power"), result.topIncidentTypes.items.map { it.value })
        assertEquals(2, result.incidentStatus.totalIncidents)
        assertEquals(50, result.incidentStatus.items.first { it.value == "Resolved" }.percentage)
    }

    @Test fun `knowledge base rejects unsupported periods`() {
        assertNull(DashboardUseCase(FakeRepository(), clock).knowledgeBase(ctx, "allTime", emptySet(), 5))
    }

    @Test fun `workload summary and charts share active assignment totals`() {
        val repo = FakeRepository(
            workloadRows = listOf(
                WorkloadAssignmentRow("tc", AssignmentType.TECHNICAL_CASE, "u1", "Open", "Urgent", "19/10/2026 06:00:00", "20/10/2026 22:00:00"),
                WorkloadAssignmentRow("wo", AssignmentType.WORK_ORDER, "u1", "Open", "Normal", "2026-10-21", "2026-10-25"),
                WorkloadAssignmentRow("task", AssignmentType.TASK, "u2", "Done", "Urgent", "27/10/2026 06:00:00", "28/10/2026 22:00:00")
            ),
            workloadUsers = listOf(WorkloadTechnicianRow("u1", "John"), WorkloadTechnicianRow("u2", "Maria")),
            keyedSettings = mapOf(
                "TechnicalCaseStatus" to listOf(StatusSettingRow("s1", "Open", "OPEN", null, false)),
                "WorkOrderStatus" to listOf(StatusSettingRow("s2", "Open", "OPEN", null, false)),
                "TaskStatus" to listOf(StatusSettingRow("s3", "Done", "CLOSED", null, false)),
                "TechnicalCasePriority" to listOf(StatusSettingRow("p1", "Urgent", "HIGH", null, false)),
                "WorkOrderPriority" to listOf(StatusSettingRow("p2", "Normal", "MEDIUM", null, false)),
                "TaskPriority" to listOf(StatusSettingRow("p3", "Urgent", "HIGH", null, false))
            )
        )
        val useCase = DashboardUseCase(repo, clock)
        val summary = useCase.technicianWorkloadSummary(ctx, java.time.LocalDate.parse("2026-10-19"), java.time.LocalDate.parse("2026-10-25"))
        val charts = useCase.technicianWorkloadCharts(ctx, java.time.LocalDate.parse("2026-10-19"), java.time.LocalDate.parse("2026-10-25"))

        assertEquals(2, summary.activeAssignments.total)
        assertEquals(0, summary.activeAssignments.previousTotal)
        assertEquals(100, summary.activeAssignments.changePercentage)
        assertEquals(1, summary.highPriorityAssignments.total)
        assertEquals(1, summary.technicians.total)
        assertEquals(0, summary.technicians.previousTotal)
        assertEquals(100, summary.technicians.changePercentage)
        assertEquals(summary.activeAssignments.total, charts.assignmentsByType.total)
        assertEquals(2, charts.workloadByTechnician.single().total)
    }

    @Test fun `technician details include assignment and alert totals for the selected range`() {
        val repo = FakeRepository(
            workloadRows = listOf(
                WorkloadAssignmentRow("tc", AssignmentType.TECHNICAL_CASE, "u1", "Open", "Urgent", "2026-10-19T09:00:00Z", "2026-10-20T10:00:00Z"),
                WorkloadAssignmentRow("wo", AssignmentType.WORK_ORDER, "u1", "Open", "Normal", "2026-10-24T15:00:00Z", "2026-10-25T17:00:00Z"),
                WorkloadAssignmentRow("task", AssignmentType.TASK, "u2", "Open", "Urgent", "2026-10-24T16:00:00Z", "2026-10-25T18:00:00Z")
            ),
            workloadUsers = listOf(WorkloadTechnicianRow("u1", "John"), WorkloadTechnicianRow("u2", "Maria")),
            keyedSettings = workloadSettings()
        )

        val result = DashboardUseCase(repo, clock).technicianDetails(
            ctx, java.time.LocalDate.parse("2026-10-19"), java.time.LocalDate.parse("2026-10-25")
        )

        assertEquals(listOf("u1", "u2"), result.technicians.map { it.userId })
        assertEquals(2, result.technicians.first().total)
        assertEquals(1, result.technicians.first().overdue)
        assertEquals(1, result.technicians.first().highPriority)
    }

    @Test fun `upcoming assignments group technicians include ongoing rows filter expired rows and cap results`() {
        val futureWorkOrders = (1..55).flatMap { index ->
            listOf("u1", "u2").map { userId ->
                WorkloadAssignmentRow(
                    "wo-$index", AssignmentType.WORK_ORDER, userId, "Open", "High",
                    "2026-10-25T${index.coerceAtMost(23).toString().padStart(2, '0')}:00:00Z",
                    "2026-10-26T00:00:00Z", "WO-$index", "Work order $index"
                )
            }
        }
        val repo = FakeRepository(
            workloadRows = futureWorkOrders + listOf(
                WorkloadAssignmentRow(
                    "ongoing", AssignmentType.WORK_ORDER, "u1", "Open", "High",
                    "2026-10-24T09:00:00Z", "2026-10-25T10:00:00Z", "WO-ONGOING", "Ongoing work"
                ),
                WorkloadAssignmentRow(
                    "past", AssignmentType.WORK_ORDER, "u1", "Open", "Low",
                    "2026-10-20T09:00:00Z", "2026-10-20T10:00:00Z", "WO-PAST", "Past work"
                )
            ),
            workloadUsers = listOf(WorkloadTechnicianRow("u1", "John"), WorkloadTechnicianRow("u2", "Maria")),
            keyedSettings = workloadSettings()
        )

        val result = DashboardUseCase(repo, clock).upcomingAssignments(ctx, AssignmentType.WORK_ORDER)

        assertEquals(50, result.size)
        assertTrue(result.all { it.assignmentType == "WORK_ORDER" })
        assertEquals("ongoing", result.first().assignmentId)
        assertTrue(result.drop(1).all { it.technicians.size == 2 })
        assertTrue(result.none { it.assignmentId == "past" })
    }
}

private fun workloadSettings() = mapOf(
    "TechnicalCaseStatus" to listOf(StatusSettingRow("tc-open", "Open", "OPEN", null, false)),
    "WorkOrderStatus" to listOf(StatusSettingRow("wo-open", "Open", "OPEN", null, false)),
    "TaskStatus" to listOf(StatusSettingRow("task-open", "Open", "OPEN", null, false)),
    "TechnicalCasePriority" to listOf(StatusSettingRow("tc-high", "Urgent", "HIGH", null, false)),
    "WorkOrderPriority" to listOf(StatusSettingRow("wo-high", "High", "HIGH", null, false)),
    "TaskPriority" to listOf(StatusSettingRow("task-high", "Urgent", "HIGH", null, false))
)

private class FakeRepository(
    private val work: List<WorkOrderRow> = emptyList(),
    private val cases: List<TechnicalCaseRow> = emptyList(),
    private val contractRows: List<ContractRow> = emptyList(),
    private val taskRows: List<TaskRow> = emptyList(),
    private val settingRows: List<StatusSettingRow> = emptyList(),
    private val typeSettingRows: List<StatusSettingRow> = emptyList(),
    private val typeCounts: List<WorkOrderTypeCountRow> = emptyList(),
    private val workloadRows: List<WorkloadAssignmentRow> = emptyList(),
    private val workloadUsers: List<WorkloadTechnicianRow> = emptyList(),
    private val keyedSettings: Map<String, List<StatusSettingRow>> = emptyMap()
) : DashboardRepository {
    override fun workOrders(ctx: RequestContext) = work
    override fun technicalCases(ctx: RequestContext) = cases
    override fun contracts(ctx: RequestContext) = contractRows
    override fun tasks(ctx: RequestContext) = taskRows
    override fun statusSettings(ctx: RequestContext, key: String) =
        keyedSettings[key] ?: if (key == "WorkOrderType") typeSettingRows else settingRows
    override fun workOrderTypeCounts(ctx: RequestContext, year: Int) = typeCounts
    override fun workloadAssignments(ctx: RequestContext) = workloadRows
    override fun workloadTechnicians(ctx: RequestContext) = workloadUsers
    override fun completeTask(ctx: RequestContext, taskId: String, completedAt: String): Boolean? = true
}
