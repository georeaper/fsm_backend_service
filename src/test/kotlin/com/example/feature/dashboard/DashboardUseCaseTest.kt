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
        ))
        val result = DashboardUseCase(repo, clock).workOrders(ctx, 2026)
        assertEquals(12, result.createdVsCompleted.size)
        assertEquals(2, result.createdVsCompleted.first().created)
        assertEquals(1, result.createdVsCompleted[1].completed)
        assertEquals(1, result.statusDistribution.open)
        assertEquals(1, result.statusDistribution.statuses.first { it.value == "OPEN" }.count)
        assertEquals("#0000ff", result.statusDistribution.statuses.first { it.value == "OPEN" }.style)
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
}

private class FakeRepository(
    private val work: List<WorkOrderRow> = emptyList(),
    private val cases: List<TechnicalCaseRow> = emptyList(),
    private val contractRows: List<ContractRow> = emptyList(),
    private val taskRows: List<TaskRow> = emptyList(),
    private val settingRows: List<StatusSettingRow> = emptyList()
) : DashboardRepository {
    override fun workOrders(ctx: RequestContext) = work
    override fun technicalCases(ctx: RequestContext) = cases
    override fun contracts(ctx: RequestContext) = contractRows
    override fun tasks(ctx: RequestContext) = taskRows
    override fun statusSettings(ctx: RequestContext, key: String) = settingRows
    override fun completeTask(ctx: RequestContext, taskId: String, completedAt: String): Boolean? = true
}
