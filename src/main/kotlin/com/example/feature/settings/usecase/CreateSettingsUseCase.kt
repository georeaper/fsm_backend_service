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
            SystemCategory = validatedSystemCategory(request.SettingsKey, request.SystemCategory),
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return repository.save(ctx, settings)
    }
}

internal fun validatedSystemCategory(settingsKey: String?, value: String?): String? {
    if (!settingsKey.orEmpty().endsWith("Status")) return null
    val normalized = value?.trim()?.uppercase()
        ?: throw IllegalArgumentException("SystemCategory is required for status settings")
    require(normalized in setOf("OPEN", "CLOSED", "CANCELED")) {
        "SystemCategory must be OPEN, CLOSED, or CANCELED"
    }
    return normalized
}
