package com.example.feature.tasks.repository

import com.example.core.RequestContext
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.dto.TasksResponse
import com.example.models.api.Tasks

interface TaskRepository {
    fun save(ctx: RequestContext, data: Tasks): Tasks
    fun findAll(ctx: RequestContext): List<TasksResponse>
    fun edit(ctx: RequestContext, data: EditTaskResponse): EditTaskResponse
}
