package com.example.feature.tools.usecase

import com.example.core.RequestContext
import com.example.feature.tools.repository.ToolRepository

class DeleteToolUseCase(private val repository: ToolRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
