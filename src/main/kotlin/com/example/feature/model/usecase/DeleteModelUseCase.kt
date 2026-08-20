package com.example.feature.model.usecase

import com.example.core.RequestContext
import com.example.feature.model.repository.ModelRepository

class DeleteModelUseCase (private val repository: ModelRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}