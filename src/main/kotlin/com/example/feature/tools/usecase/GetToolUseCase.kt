package com.example.feature.tools.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.tools.repository.ToolRepository
import com.example.feature.tools.dto.ToolsResponse

class GetToolUseCase (private val repository: ToolRepository){
    fun execute(ctx: RequestContext) :List<ToolsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            ToolsResponse(
                ToolID = it.ToolID,
                RemoteID = it.RemoteID,
                Title = it.Title,
                Description = it.Description,
                Model = it.Model,
                Manufacturer = it.Manufacturer,
                SerialNumber = it.SerialNumber,
                CalibrationDate = DateUtils.storageToUi(it.CalibrationDate),
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
