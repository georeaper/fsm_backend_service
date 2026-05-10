package com.example.feature.user.usecase

import com.example.core.RequestContext
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.dto.CreateUserResponse
import com.example.models.api.Users
import com.example.core.DateUtils
import java.util.UUID

class CreateUserUseCase (private val repository: UserRepository){
    fun execute(ctx: RequestContext, request: CreateUserResponse) : Users {
        val storageDate = DateUtils.nowStorage()
        val users = Users(
            UserID = UUID.randomUUID().toString(),
            RemoteID = null,
            Name = request.Name,
            Description = request.Description,
            Email = request.Email,
            Phone = request.Phone,
            Signature = null,
            ReportPrefix = request.ReportPrefix,
            TechnicalCasePrefix = request.TechnicalCasePrefix,
            LastReportNumber = null,
            LastTCNumber = null,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, users)
    }
}
