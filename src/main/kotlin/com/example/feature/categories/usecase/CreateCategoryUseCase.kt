package com.example.feature.categories.usecase

import com.example.core.RequestContext
import com.example.core.DateUtils
import com.example.feature.categories.dto.CreateCategoryResponse
import com.example.feature.categories.repository.CategoryRepository
import com.example.models.api.Categories
import java.util.UUID

class CreateCategoryUseCase(private val repository: CategoryRepository) {
    fun execute(ctx: RequestContext, request: CreateCategoryResponse): Categories {
        val storageDate = DateUtils.nowStorage()
        val category = Categories(
            CategoryID = UUID.randomUUID().toString(),
            RemoteID = null,
            Name = request.Name,
            Description = request.Style,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, category)
    }
}
