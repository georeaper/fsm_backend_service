package com.example.feature.settings.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.settings.dto.SettingsResponse
import com.example.feature.settings.repository.SettingsRepository

class GetWorkOrderTypeUseCase (private val repository: SettingsRepository){
    fun execute(ctx: RequestContext) :List<SettingsResponse>{
        val keyValue="WorkOrderType"
        val data =repository.getById(ctx,keyValue)
        println("$data")
        return data.map{
            SettingsResponse(
                SettingsID = it.SettingsID,
                RemoteID = it.RemoteID,
                SettingsKey = it.SettingsKey,
                SettingsValue = it.SettingsValue,
                SettingsStyle = it.SettingsStyle,
                SettingsDescription = it.SettingsDescription,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version
            )
        }
    }
}