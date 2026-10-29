package com.example.feature.tasks.usecase

import com.example.core.RequestContext
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.repository.TaskRepository
import com.example.core.DateUtils

class UpdateTaskUseCase(private val repository: TaskRepository) {
    fun execute(ctx: RequestContext, input: EditTaskResponse): EditTaskResponse {
        return repository.edit(
            ctx,
            input.copy(
                DateStart = input.DateStart?.let(DateUtils::uiToStorage),
                DateDue = input.DateDue?.let(DateUtils::uiToStorage),
                DateCompleted = input.DateCompleted?.let(DateUtils::uiToStorage),
                LastModified = DateUtils.nowStorage()
            )
        )
    }
}
