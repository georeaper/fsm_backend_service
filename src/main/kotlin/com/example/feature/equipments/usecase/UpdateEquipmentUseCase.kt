package com.example.feature.equipments.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.equipments.dto.CreateEquipmentResponse
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.models.api.Equipments

class UpdateEquipmentUseCase(
    private val repository: EquipmentRepository
) {
    fun execute(ctx: RequestContext, id: String, input: CreateEquipmentResponse): Equipments? {
        val equipment = Equipments(
            EquipmentID = id,
            RemoteID = input.RemoteID,
            Name = input.Name,
            SerialNumber = input.SerialNumber,
            Model = input.Model,
            Manufacturer = input.Manufacturer,
            Notes = input.Notes,
            Description = input.Description,
            EquipmentVersion = input.EquipmentVersion,
            EquipmentCategory = input.EquipmentCategory,
            Warranty = input.Warranty,
            EquipmentStatus = input.EquipmentStatus,
            InstallationDate = input.InstallationDate,
            LastModified = DateUtils.nowStorage(),
            Version = input.Version,
            CustomerID = input.CustomerID
        )

        return if (repository.update(ctx, equipment)) equipment else null
    }
}
