package com.example.feature.dashboard.dto

import kotlinx.serialization.Serializable

@Serializable data class DashboardError(val code: String, val message: String)
@Serializable data class DashboardStatusItem(val settingId: String?, val value: String, val systemCategory: String?, val style: String?, val count: Int)
@Serializable data class WorkOrderSummaryMetric(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>, val changePercentage: Int)
@Serializable data class TechnicalCaseSummaryMetric(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>, val changePercentage: Int)
@Serializable data class ContractMetric(val active: Int, val newThisMonth: Int, val changePercentage: Int)
@Serializable data class DashboardSummary(val workOrders: WorkOrderSummaryMetric, val contracts: ContractMetric, val technicalCases: TechnicalCaseSummaryMetric)
@Serializable data class MonthlyWorkOrders(val period: String, val label: String, val created: Int, val completed: Int)
@Serializable data class StatusCount(val status: String, val count: Int)
@Serializable data class StatusDistribution(val total: Int, val items: List<StatusCount>)
@Serializable data class WorkOrderStatusDistribution(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>)
@Serializable data class WorkOrderAnalytics(val createdVsCompleted: List<MonthlyWorkOrders>, val statusDistribution: WorkOrderStatusDistribution)
@Serializable data class RecentTechnicalCase(val technicalCaseId: String, val caseNumber: String?, val title: String?, val customerId: String?, val customerName: String?, val status: String, val systemCategory: String?, val statusStyle: String?, val type: String?, val dueDate: String?, val priority: String)
@Serializable data class TechnicalCaseStatusDistribution(val total: Int, val open: Int, val overdue: Int, val statuses: List<DashboardStatusItem>)
@Serializable data class TechnicalCasesDashboard(val statusDistribution: TechnicalCaseStatusDistribution, val recent: List<RecentTechnicalCase>)
@Serializable data class UpcomingTask(val taskId: String, val title: String?, val status: String, val relatedType: String?, val relatedId: String?, val relatedNumber: String?, val dueDate: String?, val priority: String, val type: String? = null)
@Serializable data class CompletedTask(val taskId: String, val status: String, val completedAt: String)
@Serializable data class DueDates(val workOrdersDueThisWeek: Int, val technicalCasesDueThisWeek: Int, val contractsExpiringThisMonth: Int)
@Serializable data class TopCustomer(val customerId: String, val customerName: String?, val workOrderCount: Int)
