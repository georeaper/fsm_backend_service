package com.example.feature.model.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.model.repository.ModelRepository
import com.example.feature.model.dto.ModelResponse

class GetModelUseCase (private val repository: ModelRepository){
    fun execute(ctx: RequestContext) :List<ModelResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            ModelResponse(
                ModelID = it.ModelID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                Style = it.Style,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
