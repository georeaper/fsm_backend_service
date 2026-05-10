package com.example.feature.maintenance.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.maintenance.repository.MaintenanceRepository
import com.example.feature.maintenance.dto.MaintenancesResponse

class GetMaintenanceUseCase (private val repository: MaintenanceRepository){
    fun execute(ctx: RequestContext) :List<MaintenancesResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            MaintenancesResponse(
                MaintenanceID = it.MaintenanceID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                Description = it.Description,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
