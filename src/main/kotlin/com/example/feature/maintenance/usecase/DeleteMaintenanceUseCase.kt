package com.example.feature.maintenance.usecase

import com.example.core.RequestContext
import com.example.feature.maintenance.repository.MaintenanceRepository

class DeleteMaintenanceUseCase(private val repository: MaintenanceRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
