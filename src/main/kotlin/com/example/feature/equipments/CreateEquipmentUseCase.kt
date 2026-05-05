package com.example.feature.equipments

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.customers.CustomersRepository
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.equipments.dto.CreateEquipmentResponse
import com.example.models.api.Equipments
import java.util.UUID

class CreateEquipmentUseCase (private val repository: EquipmentRepository
) {
    fun execute( ctx : RequestContext ,input: CreateEquipmentResponse): Equipments{
        val storageDate = DateUtils.nowStorage()

        val equipments= Equipments(
            EquipmentID = UUID.randomUUID().toString(),
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
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = input.Version,
            CustomerID = input.CustomerID
        )
        val saved =repository.save(ctx,equipments)
        return saved
    }
}