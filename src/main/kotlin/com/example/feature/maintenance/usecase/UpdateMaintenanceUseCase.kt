package com.example.feature.maintenance.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.maintenance.dto.MaintenanceWithCheckFormsResponse
import com.example.feature.maintenance.dto.UpdateMaintenanceWithCheckFormsRequest
import com.example.feature.maintenance.repository.MaintenanceRepository
import com.example.models.api.CheckForms
import com.example.models.api.Maintenances
import java.util.UUID

class UpdateMaintenanceUseCase(private val repository: MaintenanceRepository) {
    fun execute(
        ctx: RequestContext,
        input: UpdateMaintenanceWithCheckFormsRequest
    ): MaintenanceWithCheckFormsResponse? {
        val storageDate = DateUtils.nowStorage()
        val maintenance = Maintenances(
            MaintenanceID = input.MaintenanceID,
            RemoteID = null,
            Name = input.Name,
            Description = input.Description,
            LastModified = storageDate,
            DateCreated = null,
            Version = input.Version ?: "1"
        )
        val checkForms = input.CheckForms.map { checkForm ->
            CheckForms(
                CheckFormID = checkForm.CheckFormID ?: UUID.randomUUID().toString(),
                RemoteID = null,
                MaintenancesID = input.MaintenanceID,
                Description = checkForm.Description,
                ValueExpected = checkForm.ValueExpected,
                ValueType = checkForm.ValueType,
                LastModified = storageDate,
                DateCreated = storageDate,
                Version = checkForm.Version ?: "1"
            )
        }

        return repository.updateWithCheckForms(ctx, maintenance, checkForms)
    }
}
