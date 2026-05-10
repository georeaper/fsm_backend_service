package com.example.feature.settings.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.settings.repository.SettingsRepository
import com.example.feature.settings.dto.SettingsResponse

class GetSettingsUseCase (private val repository: SettingsRepository){
    fun execute(ctx: RequestContext) :List<SettingsResponse>{
        val data =repository.findAll(ctx)
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
