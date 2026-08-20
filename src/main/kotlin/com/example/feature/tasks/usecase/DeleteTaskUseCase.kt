package com.example.feature.tasks.usecase

import com.example.core.RequestContext
import com.example.feature.tasks.repository.TaskRepository

class DeleteTaskUseCase(private val repository: TaskRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
