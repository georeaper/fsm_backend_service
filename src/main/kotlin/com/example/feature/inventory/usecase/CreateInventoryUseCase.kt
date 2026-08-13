package com.example.feature.inventory.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.inventory.repository.InventoryRepository
import com.example.feature.inventory.dto.CreateInventoryResponse
import com.example.models.api.Inventory
import java.util.UUID

class CreateInventoryUseCase (private val repository: InventoryRepository
) {
    fun execute(ctx : RequestContext, input: CreateInventoryResponse): Inventory {
        val storageDate = DateUtils.nowStorage()

        val inventory = Inventory(
            InventoryID = UUID.randomUUID().toString(),
            RemoteID = input.RemoteID,
            Title = input.Title,
            Description = input.Description,
            Quantity = input.Quantity,
            Value = input.Value,
            Type = input.Type,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = input.Version
        )
        val saved =repository.save(ctx,inventory)
        return saved
    }
}
