package com.example.feature.dashboard.repository

import com.example.core.RequestContext

data class WorkOrderRow(val id: String, val number: String?, val status: String?, val created: String?, val completedOrDue: String?, val customerId: String?, val customerName: String?)
data class TechnicalCaseRow(val id: String, val number: String?, val title: String?, val active: Boolean?, val status: String?, val type: String?, val urgency: String?, val created: String?, val due: String?, val customerId: String?, val customerName: String?)
data class ContractRow(val status: String?, val created: String?, val end: String?)
data class TaskRow(val id: String, val title: String?, val status: String?, val priority: String?, val due: String?, val ticketId: String?, val ticketNumber: String?, val type: String? = null)
data class StatusSettingRow(val id: String, val value: String, val systemCategory: String?, val style: String?, val isDeleted: Boolean)

interface DashboardRepository {
    fun workOrders(ctx: RequestContext): List<WorkOrderRow>
    fun technicalCases(ctx: RequestContext): List<TechnicalCaseRow>
    fun contracts(ctx: RequestContext): List<ContractRow>
    fun tasks(ctx: RequestContext): List<TaskRow>
    fun statusSettings(ctx: RequestContext, key: String): List<StatusSettingRow>
    fun completeTask(ctx: RequestContext, taskId: String, completedAt: String): Boolean?
}
