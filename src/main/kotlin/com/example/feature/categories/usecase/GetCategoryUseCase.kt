package com.example.feature.categories.usecase

import com.example.core.RequestContext
import com.example.feature.categories.dto.CategoryResponse
import com.example.feature.categories.repository.CategoryRepository

class GetCategoryUseCase(private val repository: CategoryRepository) {
    fun execute(ctx: RequestContext): List<CategoryResponse> {
        val data = repository.findAll(ctx)
        return data.map {
            CategoryResponse(
                CategoryID = it.CategoryID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                Style = it.Style,
                LastModified = it.LastModified,
                DateCreated = it.DateCreated,
                Version = it.Version
            )
        }
    }
}
