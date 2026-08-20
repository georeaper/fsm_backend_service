package com.example.feature.manufacturer.usecase

import com.example.core.RequestContext
import com.example.feature.manufacturer.repository.ManufacturerRepository

class DeleteManufacturerUseCase(private val repository: ManufacturerRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}