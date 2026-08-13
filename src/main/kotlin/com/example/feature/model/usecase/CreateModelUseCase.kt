package com.example.feature.model.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.model.repository.ModelRepository
import com.example.feature.model.dto.CreateModelResponse
import com.example.models.api.ModelAsset
import java.util.UUID

class CreateModelUseCase (private val repository: ModelRepository
) {
    fun execute(ctx : RequestContext, input: CreateModelResponse): ModelAsset {
        val storageDate = DateUtils.nowStorage()

        val model = ModelAsset(
            ModelID = UUID.randomUUID().toString(),
            RemoteID = input.RemoteID,
            Name = input.Name,
            Style = input.Style,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = input.Version
        )
        val saved =repository.save(ctx,model)
        return saved
    }
}
