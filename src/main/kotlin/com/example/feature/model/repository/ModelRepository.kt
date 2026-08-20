package com.example.feature.model.repository

import com.example.core.RequestContext
import com.example.feature.model.dto.EditModelResponse
import com.example.feature.model.dto.ModelResponse
import com.example.models.api.ModelAsset

interface ModelRepository {
    fun save(ctx: RequestContext, data: ModelAsset): ModelAsset
    fun findAll(ctx: RequestContext): List<ModelResponse>
    fun edit(ctx: RequestContext, data: EditModelResponse): EditModelResponse
    fun delete(ctx: RequestContext, id: String): Boolean
}
