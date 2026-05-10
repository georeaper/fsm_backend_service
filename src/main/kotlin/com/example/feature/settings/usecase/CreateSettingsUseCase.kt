package com.example.feature.settings.usecase

import com.example.core.RequestContext
import com.example.feature.settings.repository.SettingsRepository
import com.example.feature.settings.dto.CreateSettingsResponse
import com.example.models.api.Settings
import com.example.core.DateUtils
import java.util.UUID

class CreateSettingsUseCase (private val repository: SettingsRepository){
    fun execute(ctx: RequestContext, request: CreateSettingsResponse) : Settings {
        val storageDate = DateUtils.nowStorage()
        val settings = Settings(
            SettingsID = UUID.randomUUID().toString(),
            RemoteID = null,
            SettingsKey = request.SettingsKey,
            SettingsValue = request.SettingsValue,
            SettingsStyle = request.SettingsStyle,
            SettingsDescription = request.SettingsDescription,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, settings)
    }
}
