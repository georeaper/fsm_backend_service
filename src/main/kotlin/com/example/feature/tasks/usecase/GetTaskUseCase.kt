package com.example.feature.tasks.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.tasks.repository.TaskRepository
import com.example.feature.tasks.dto.TasksResponse

class GetTaskUseCase (private val repository: TaskRepository){
    fun execute(ctx: RequestContext) :List<TasksResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            TasksResponse(
                TaskID = it.TaskID,
                Title = it.Title,
                Description = it.Description,
                Status = it.Status,
                Priority = it.Priority,
                DateStart = DateUtils.storageToUi(it.DateStart),
                DateDue = DateUtils.storageToUi(it.DateDue),
                DateCompleted = DateUtils.storageToUi(it.DateCompleted),
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                TicketID = it.TicketID,
                UserID = it.UserID
            )
        }
    }
}
