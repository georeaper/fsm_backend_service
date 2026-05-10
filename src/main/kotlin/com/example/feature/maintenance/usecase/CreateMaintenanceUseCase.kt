package com.example.feature.maintenance.usecase

import com.example.core.RequestContext
import com.example.feature.maintenance.repository.MaintenanceRepository
import com.example.feature.maintenance.dto.CreateMaintenanceResponse
import com.example.models.api.Maintenances
import com.example.core.DateUtils
import java.util.UUID

class CreateMaintenanceUseCase (private val repository: MaintenanceRepository){
    fun execute(ctx: RequestContext, request: CreateMaintenanceResponse) : Maintenances {
        val storageDate = DateUtils.nowStorage()
        val maintenances = Maintenances(
            MaintenanceID = UUID.randomUUID().toString(),
            RemoteID = null,
            Name = request.Name,
            Description = request.Description,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, maintenances)
    }
}
