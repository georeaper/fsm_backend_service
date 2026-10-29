package com.example.feature.dashboard.repository

import com.example.core.RequestContext

data class WorkOrderRow(val id: String, val number: String?, val status: String?, val created: String?, val completedOrDue: String?, val customerId: String?, val customerName: String?)
data class TechnicalCaseRow(
    val id: String,
    val number: String?,
    val title: String?,
    val active: Boolean?,
    val status: String?,
    val type: String?,
    val urgency: String?,
    val created: String?,
    val due: String?,
    val customerId: String?,
    val customerName: String?,
    val equipmentId: String? = null,
    val equipmentModel: String? = null,
    val equipmentManufacturer: String? = null
)
data class ContractRow(val status: String?, val created: String?, val end: String?)
data class TaskRow(val id: String, val title: String?, val status: String?, val priority: String?, val due: String?, val ticketId: String?, val ticketNumber: String?, val type: String? = null)
data class StatusSettingRow(val id: String, val value: String, val systemCategory: String?, val style: String?, val isDeleted: Boolean)
data class WorkOrderTypeCountRow(val typeValue: String, val period: String, val count: Int)
enum class AssignmentType { TECHNICAL_CASE, WORK_ORDER, TASK }
data class WorkloadAssignmentRow(
    val id: String,
    val type: AssignmentType,
    val userId: String?,
    val status: String?,
    val priority: String?,
    val start: String?,
    val due: String?,
    val number: String? = null,
    val title: String? = null
)
data class WorkloadTechnicianRow(val userId: String, val name: String?)

interface DashboardRepository {
    fun workOrders(ctx: RequestContext): List<WorkOrderRow>
    fun technicalCases(ctx: RequestContext): List<TechnicalCaseRow>
    fun contracts(ctx: RequestContext): List<ContractRow>
    fun tasks(ctx: RequestContext): List<TaskRow>
    fun statusSettings(ctx: RequestContext, key: String): List<StatusSettingRow>
    fun workOrderTypeCounts(ctx: RequestContext, year: Int): List<WorkOrderTypeCountRow>
    fun workloadAssignments(ctx: RequestContext): List<WorkloadAssignmentRow>
    fun workloadTechnicians(ctx: RequestContext): List<WorkloadTechnicianRow>
    fun completeTask(ctx: RequestContext, taskId: String, completedAt: String): Boolean?
}
