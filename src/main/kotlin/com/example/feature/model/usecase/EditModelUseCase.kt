package com.example.feature.model.usecase

import com.example.core.RequestContext
import com.example.feature.model.repository.ModelRepository
import com.example.feature.model.dto.EditModelResponse

class EditModelUseCase(private val repository: ModelRepository) {
    fun execute(ctx: RequestContext, input: EditModelResponse): EditModelResponse {
        val model = EditModelResponse(
            ModelID = input.ModelID,
            RemoteID = input.RemoteID,
            Name = input.Name,
            Style = input.Style,
            LastModified = input.LastModified,
            DateCreated = input.DateCreated,
            Version = input.Version
        )
        return repository.edit(ctx, model)
    }
}
