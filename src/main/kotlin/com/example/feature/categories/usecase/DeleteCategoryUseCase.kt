package com.example.feature.categories.usecase

import com.example.core.RequestContext
import com.example.feature.categories.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
