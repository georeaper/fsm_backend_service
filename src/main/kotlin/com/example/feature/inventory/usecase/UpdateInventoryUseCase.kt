package com.example.feature.inventory.usecase

import com.example.core.RequestContext
import com.example.feature.inventory.dto.EditInventoryResponse
import com.example.feature.inventory.repository.InventoryRepository

class UpdateInventoryUseCase(private val repository: InventoryRepository) {
    fun execute(ctx: RequestContext, input: EditInventoryResponse): EditInventoryResponse {
        return repository.edit(ctx, input)
    }
}
