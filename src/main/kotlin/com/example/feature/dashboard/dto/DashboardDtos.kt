package com.example.feature.dashboard.dto

import kotlinx.serialization.Serializable

@Serializable data class DashboardError(val code: String, val message: String)
@Serializable data class DashboardStatusItem(val settingId: String?, val value: String, val systemCategory: String?, val style: String?, val count: Int)
@Serializable data class WorkOrderSummaryMetric(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>, val changePercentage: Int)
@Serializable data class TechnicalCaseSummaryMetric(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>, val changePercentage: Int)
@Serializable data class ContractMetric(val active: Int, val newThisMonth: Int, val changePercentage: Int)
@Serializable data class DashboardSummary(val workOrders: WorkOrderSummaryMetric, val contracts: ContractMetric, val technicalCases: TechnicalCaseSummaryMetric)
@Serializable data class MonthlyWorkOrders(val period: String, val label: String, val created: Int, val completed: Int)
@Serializable data class WorkOrderTypeItem(val settingId: String, val value: String, val style: String?)
@Serializable data class WorkOrderTypePeriod(val period: String, val label: String, val counts: Map<String, Int>)
@Serializable data class ReportsByType(val types: List<WorkOrderTypeItem>, val periods: List<WorkOrderTypePeriod>)
@Serializable data class StatusCount(val status: String, val count: Int)
@Serializable data class StatusDistribution(val total: Int, val items: List<StatusCount>)
@Serializable data class WorkOrderStatusDistribution(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>)
@Serializable data class WorkOrderAnalytics(val reportsByType: ReportsByType, val createdVsCompleted: List<MonthlyWorkOrders>, val statusDistribution: WorkOrderStatusDistribution)
@Serializable data class RecentTechnicalCase(val technicalCaseId: String, val caseNumber: String?, val title: String?, val customerId: String?, val customerName: String?, val status: String, val systemCategory: String?, val statusStyle: String?, val type: String?, val dueDate: String?, val priority: String)
@Serializable data class TechnicalCaseStatusDistribution(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>)
@Serializable data class TechnicalCasesDashboard(val statusDistribution: TechnicalCaseStatusDistribution, val recent: List<RecentTechnicalCase>)
@Serializable data class UpcomingTask(val taskId: String, val title: String?, val status: String, val relatedType: String?, val relatedId: String?, val relatedNumber: String?, val dueDate: String?, val priority: String, val type: String? = null)
@Serializable data class CompletedTask(val taskId: String, val status: String, val completedAt: String)
@Serializable data class DueDates(val workOrdersDueThisWeek: Int, val technicalCasesDueThisWeek: Int, val contractsExpiringThisMonth: Int)
@Serializable data class TopCustomer(val customerId: String, val customerName: String?, val workOrderCount: Int)

@Serializable data class KnowledgeBaseRankItem(val value: String, val count: Int)
@Serializable data class KnowledgeBaseRankMetric(
    val totalIncidents: Int,
    val leadingValue: String?,
    val leadingCount: Int,
    val changePercentage: Int,
    val items: List<KnowledgeBaseRankItem>
)
@Serializable data class KnowledgeBaseStatusItem(
    val settingId: String?,
    val value: String,
    val systemCategory: String?,
    val style: String?,
    val count: Int,
    val percentage: Int
)
@Serializable data class KnowledgeBaseStatusMetric(
    val totalIncidents: Int,
    val leadingValue: String?,
    val leadingCount: Int,
    val leadingPercentage: Int,
    val changePercentage: Int,
    val items: List<KnowledgeBaseStatusItem>
)
@Serializable data class KnowledgeBaseCustomerOption(val customerId: String, val customerName: String?)
@Serializable data class KnowledgeBaseFilters(
    val period: String,
    val customerIds: List<String>,
    val availableCustomers: List<KnowledgeBaseCustomerOption>
)
@Serializable data class KnowledgeBaseOverview(
    val range: DashboardDateRange,
    val previousRange: DashboardDateRange,
    val filters: KnowledgeBaseFilters,
    val mostFrequentEquipment: KnowledgeBaseRankMetric,
    val mostFrequentManufacturer: KnowledgeBaseRankMetric,
    val topIncidentTypes: KnowledgeBaseRankMetric,
    val incidentStatus: KnowledgeBaseStatusMetric
)

@Serializable data class DashboardDateRange(val from: String, val to: String)
@Serializable data class AssignmentMetric(val total: Int, val previousTotal: Int, val changePercentage: Int, val technicalCases: Int, val workOrders: Int, val tasks: Int)
@Serializable data class TechnicianMetric(val total: Int, val previousTotal: Int, val changePercentage: Int)
@Serializable data class TechnicianWorkloadSummary(
    val range: DashboardDateRange,
    val activeAssignments: AssignmentMetric,
    val overdueAssignments: AssignmentMetric,
    val highPriorityAssignments: AssignmentMetric,
    val technicians: TechnicianMetric
)
@Serializable data class TechnicianWorkloadItem(val userId: String, val name: String, val technicalCases: Int, val workOrders: Int, val tasks: Int, val total: Int, val scheduledHours: Double = 0.0)
@Serializable data class AssignmentsByType(val total: Int, val technicalCases: Int, val workOrders: Int, val tasks: Int)
@Serializable data class TechnicianWorkloadCharts(val range: DashboardDateRange, val workloadByTechnician: List<TechnicianWorkloadItem>, val assignmentsByType: AssignmentsByType)
@Serializable data class TechnicianDetailItem(
    val userId: String,
    val name: String,
    val total: Int,
    val technicalCases: Int,
    val workOrders: Int,
    val tasks: Int,
    val overdue: Int,
    val highPriority: Int
)
@Serializable data class TechnicianDetails(val range: DashboardDateRange, val technicians: List<TechnicianDetailItem>)
@Serializable data class UpcomingAssignmentTechnician(val userId: String, val name: String)
@Serializable data class UpcomingAssignment(
    val assignmentId: String,
    val assignmentType: String,
    val number: String?,
    val title: String?,
    val scheduledAt: String,
    val dueAt: String?,
    val priority: String,
    val technicians: List<UpcomingAssignmentTechnician>
)
