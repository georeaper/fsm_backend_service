package com.example.feature.tasks.usecase

import com.example.core.RequestContext
import com.example.feature.tasks.repository.TaskRepository
import com.example.feature.tasks.dto.CreateTaskResponse
import com.example.models.api.Tasks
import com.example.core.DateUtils
import java.util.UUID

class CreateTaskUseCase (private val repository: TaskRepository){
    fun execute(ctx: RequestContext, request: CreateTaskResponse) : Tasks {
        val storageDate = DateUtils.nowStorage()
        val tasks = Tasks(
            TaskID = UUID.randomUUID().toString(),
            Title = request.Title,
            Description = request.Description,
            Status = request.Status,
            Priority = request.Priority,
            DateStart = DateUtils.uiToStorage(request.DateStart!!),
            DateDue = DateUtils.uiToStorage(request.DateDue!! ),
            DateCompleted = DateUtils.uiToStorage(request.DateCompleted!!),
            LastModified = storageDate,
            DateCreated = storageDate,
            TicketID = request.TicketID,
            UserID = request.UserID
        )
        return repository.save(ctx, tasks)
    }
}
