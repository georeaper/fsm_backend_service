package com.example.feature.tools.usecase

import com.example.core.RequestContext
import com.example.feature.tools.repository.ToolRepository
import com.example.feature.tools.dto.CreateToolResponse
import com.example.models.api.Tools
import com.example.core.DateUtils
import java.util.UUID

class CreateToolUseCase (private val repository: ToolRepository){
    fun execute(ctx: RequestContext, request: CreateToolResponse) : Tools {
        val storageDate = DateUtils.nowStorage()
        val tools = Tools(
            ToolsID = UUID.randomUUID().toString(),
            RemoteID = null,
            Title = request.Title,
            Description = request.Description,
            Model = request.Model,
            Manufacturer = request.Manufacturer,
            SerialNumber = request.SerialNumber,
            CalibrationDate = DateUtils.uiToStorage(request.CalibrationDate!!),
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, tools)
    }
}
