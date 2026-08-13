package com.example.feature.manufacturer.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.manufacturer.repository.ManufacturerRepository
import com.example.feature.manufacturer.dto.ManufacturerResponse

class GetManufacturerUseCase (private val repository: ManufacturerRepository){
    fun execute(ctx: RequestContext) :List<ManufacturerResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            ManufacturerResponse(
                ManufacturerID = it.ManufacturerID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                Style = it.Style,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
