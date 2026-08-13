package com.example.feature.inventory.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.inventory.repository.InventoryRepository
import com.example.feature.inventory.dto.InventoryResponse

class GetInventoryUseCase (private val repository: InventoryRepository){
    fun execute(ctx: RequestContext) :List<InventoryResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            InventoryResponse(
                InventoryID = it.InventoryID,
                RemoteID = it.RemoteID,
                Title = it.Title,
                Description = it.Description,
                Quantity = it.Quantity,
                Value = it.Value,
                Type = it.Type,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
