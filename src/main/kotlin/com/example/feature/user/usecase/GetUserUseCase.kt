package com.example.feature.user.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.dto.UsersResponse

class GetUserUseCase (private val repository: UserRepository){
    fun execute(ctx: RequestContext) :List<UsersResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            UsersResponse(
                UserID = it.UserID,
                RemoteID = it.RemoteID,
                Name = it.Name,
                Description = it.Description,
                Email = it.Email,
                Phone = it.Phone,
                ReportPrefix = it.ReportPrefix,
                TechnicalCasePrefix = it.TechnicalCasePrefix,
                LastReportNumber = it.LastReportNumber,
                LastTCNumber = it.LastTCNumber,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}
