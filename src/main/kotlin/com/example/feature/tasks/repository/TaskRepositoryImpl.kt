package com.example.feature.tasks.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.tasks.dto.EditTaskResponse
import com.example.feature.tasks.dto.TasksResponse
import com.example.models.api.Tasks
import com.example.models.databaseModels.tasksTable
import com.example.models.databaseModels.ticketTable
import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TaskRepositoryImpl(private val dbProvider: DatabaseProvider) : TaskRepository {
    override fun save(ctx: RequestContext, data: Tasks): Tasks {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            tasksTable.insert {
                it[taskId] = data.TaskID
                it[title] = data.Title
                it[description] = data.Description
                it[status] = data.Status
                it[priority] = data.Priority
                it[dateStart] = data.DateStart
                it[dateDue] = data.DateDue
                it[dateCompleted] = data.DateCompleted
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[ticketId] = data.TicketID
                it[userId] = data.UserID
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<TasksResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
//            val joinQuery = tasksTable
//                .leftJoin(ticketTable)
//                .leftJoin(userTable)
            val joinQuery = tasksTable
                .join(
                    otherTable = ticketTable,
                    joinType = JoinType.LEFT,
                    onColumn = tasksTable.ticketId,
                    otherColumn = ticketTable.ticketId
                )
                .join(
                    otherTable = userTable,
                    joinType = JoinType.LEFT,
                    onColumn = tasksTable.userId,
                    otherColumn = userTable.userId
                )
            val map = joinQuery.selectAll().map {
                TasksResponse(
                    TaskID = it[tasksTable.taskId],
                    Title = it[tasksTable.title],
                    Description = it[tasksTable.description],
                    Status = it[tasksTable.status],
                    Priority = it[tasksTable.priority],
                    DateStart = it[tasksTable.dateStart],
                    DateDue = it[tasksTable.dateDue],
                    DateCompleted = it[tasksTable.dateCompleted],
                    LastModified = it[tasksTable.lastModified],
                    DateCreated = it[tasksTable.dateCreated],
                    TicketID = it[tasksTable.ticketId],
                    UserID = it[tasksTable.userId]
                )
            }
            map
        }
    }

    override fun edit(ctx: RequestContext, data: EditTaskResponse): EditTaskResponse {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            tasksTable.update({ tasksTable.taskId eq data.TaskID }) {
                it[taskId] = data.TaskID
                it[title] = data.Title
                it[description] = data.Description
                it[status] = data.Status
                it[priority] = data.Priority
                it[dateStart] = data.DateStart
                it[dateDue] = data.DateDue
                it[dateCompleted] = data.DateCompleted
                it[lastModified] = data.LastModified 
                it[dateCreated] = data.DateCreated
                it[ticketId] = data.TicketID
                it[userId] = data.UserID
            }
        }
        return data
    }
}
