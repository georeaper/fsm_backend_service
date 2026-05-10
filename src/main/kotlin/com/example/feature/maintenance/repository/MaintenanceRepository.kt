package com.example.feature.maintenance.repository

import com.example.core.RequestContext
import com.example.feature.maintenance.dto.EditMaintenanceResponse
import com.example.feature.maintenance.dto.MaintenancesResponse
import com.example.models.api.Maintenances

interface MaintenanceRepository {
    fun save(ctx: RequestContext, data: Maintenances): Maintenances
    fun findAll(ctx: RequestContext): List<MaintenancesResponse>
    fun edit(ctx: RequestContext, data: EditMaintenanceResponse): EditMaintenanceResponse
}
