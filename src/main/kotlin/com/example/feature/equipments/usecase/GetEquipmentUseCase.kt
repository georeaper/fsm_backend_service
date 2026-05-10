package com.example.feature.equipments.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.feature.equipments.dto.EquipmentsResponse

class GetEquipmentUseCase (private val repository: EquipmentRepository){
    fun execute(ctx: RequestContext) :List<EquipmentsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            EquipmentsResponse(
                EquipmentID = it.EquipmentID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                SerialNumber = it.SerialNumber,
                Model = it.Model,
                Manufacturer = it.Manufacturer,
                Notes = it.Notes,
                Description = it.Description,
                EquipmentVersion = it.EquipmentVersion,
                EquipmentCategory = it.EquipmentCategory,
                Warranty = it.Warranty,
                EquipmentStatus = it.EquipmentStatus,
                InstallationDate = DateUtils.storageToUi(it.InstallationDate),
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version,
                CustomerID = it.CustomerID,
                CustomerName = it.CustomerName
            )
        }
    }
}