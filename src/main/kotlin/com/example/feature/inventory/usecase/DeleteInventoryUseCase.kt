package com.example.feature.inventory.usecase

import com.example.core.RequestContext
import com.example.feature.inventory.repository.InventoryRepository

class DeleteInventoryUseCase(private val repository: InventoryRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
