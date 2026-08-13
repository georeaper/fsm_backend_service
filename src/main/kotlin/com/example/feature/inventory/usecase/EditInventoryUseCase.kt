package com.example.feature.inventory.usecase

import com.example.core.RequestContext
import com.example.feature.inventory.repository.InventoryRepository
import com.example.feature.inventory.dto.EditInventoryResponse

class EditInventoryUseCase(private val repository: InventoryRepository) {
    fun execute(ctx: RequestContext, input: EditInventoryResponse): EditInventoryResponse {
        val inventory = EditInventoryResponse(
            InventoryID = input.InventoryID,
            RemoteID = input.RemoteID,
            Title = input.Title,
            Description = input.Description,
            Quantity = input.Quantity,
            Value = input.Value,
            Type = input.Type,
            LastModified = input.LastModified,
            DateCreated = input.DateCreated,
            Version = input.Version
        )
        return repository.edit(ctx, inventory)
    }
}
