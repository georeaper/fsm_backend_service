package com.example.feature.equipments
import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse

class EditEquipmentUseCase(private val repository: EquipmentRepository) {
    fun execute(ctx: RequestContext, input: EditEquipmentResponse): EditEquipmentResponse {
        val equipments = EditEquipmentResponse(
            EquipmentID = input.EquipmentID,
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
            LastModified = input.LastModified,
            DateCreated = input.DateCreated,
            Version = input.Version,
            CustomerID = input.CustomerID
        )
        return repository.edit(ctx, equipments)
    }
}