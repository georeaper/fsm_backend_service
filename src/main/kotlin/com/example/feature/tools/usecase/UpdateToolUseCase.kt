package com.example.feature.tools.usecase

import com.example.core.RequestContext
import com.example.feature.tools.dto.EditToolResponse
import com.example.feature.tools.repository.ToolRepository

class UpdateToolUseCase(private val repository: ToolRepository) {
    fun execute(ctx: RequestContext, input: EditToolResponse): EditToolResponse {
        return repository.edit(ctx, input)
    }
}
