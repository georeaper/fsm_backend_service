package com.example.feature.categories.repository

import com.example.core.RequestContext
import com.example.feature.categories.dto.CategoryResponse
import com.example.models.api.Categories

interface CategoryRepository {
    fun save(ctx: RequestContext, data: Categories): Categories
    fun findAll(ctx: RequestContext): List<CategoryResponse>
    fun delete(ctx: RequestContext, id: String): Boolean
}
