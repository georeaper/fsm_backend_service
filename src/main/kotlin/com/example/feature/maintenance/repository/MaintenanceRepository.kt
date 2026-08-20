package com.example.feature.maintenance.repository

import com.example.core.RequestContext
import com.example.feature.maintenance.dto.MaintenanceWithCheckFormsResponse
import com.example.feature.maintenance.dto.MaintenanceDetailsResponse
import com.example.feature.maintenance.dto.MaintenancesResponse
import com.example.models.api.CheckForms
import com.example.models.api.Maintenances

interface MaintenanceRepository {
    fun saveWithCheckForms(
        ctx: RequestContext,
        maintenance: Maintenances,
        checkForms: List<CheckForms>
    ): MaintenanceWithCheckFormsResponse
    fun findAll(ctx: RequestContext): List<MaintenancesResponse>
    fun findByIdWithCheckForms(
        ctx: RequestContext,
        maintenanceId: String
    ): MaintenanceDetailsResponse?
    fun updateWithCheckForms(
        ctx: RequestContext,
        maintenance: Maintenances,
        checkForms: List<CheckForms>
    ): MaintenanceWithCheckFormsResponse?
    fun delete(ctx: RequestContext, id: String): Boolean
}
