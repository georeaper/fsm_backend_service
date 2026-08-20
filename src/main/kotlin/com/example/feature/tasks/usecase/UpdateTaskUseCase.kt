package com.example.feature.tasks.usecase

import com.example.core.RequestContext
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.repository.TaskRepository

class UpdateTaskUseCase(private val repository: TaskRepository) {
    fun execute(ctx: RequestContext, input: EditTaskResponse): EditTaskResponse {
        return repository.edit(ctx, input)
    }
}
