package com.example.feature.maintenance.usecase

import com.example.core.RequestContext
import com.example.feature.maintenance.repository.MaintenanceRepository
import com.example.feature.maintenance.dto.CreateMaintenanceWithCheckFormsRequest
import com.example.feature.maintenance.dto.MaintenanceWithCheckFormsResponse
import com.example.models.api.CheckForms
import com.example.models.api.Maintenances
import com.example.core.DateUtils
import java.util.UUID

class CreateMaintenanceUseCase (private val repository: MaintenanceRepository){
    fun execute(
        ctx: RequestContext,
        request: CreateMaintenanceWithCheckFormsRequest
    ): MaintenanceWithCheckFormsResponse {
        val storageDate = DateUtils.nowStorage()
        val maintenanceId = UUID.randomUUID().toString()
        val maintenance = Maintenances(
            MaintenanceID = maintenanceId,
            RemoteID = null,
            Name = request.Name,
            Description = request.Description,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        val checkForms = request.CheckForms.map { checkForm ->
            CheckForms(
                CheckFormID = UUID.randomUUID().toString(),
                RemoteID = null,
                MaintenancesID = maintenanceId,
                Description = checkForm.Description,
                ValueExpected = checkForm.ValueExpected,
                ValueType = checkForm.ValueType,
                LastModified = storageDate,
                DateCreated = storageDate,
                Version = "1"
            )
        }

        return repository.saveWithCheckForms(ctx, maintenance, checkForms)
    }
}
