package com.example.feature.settings.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.settings.dto.CreateSettingsResponse
import com.example.feature.settings.dto.SettingsResponse
import com.example.feature.settings.repository.SettingsRepository
import com.example.models.api.Settings
import java.util.UUID

class CategorizedSettingsUseCase(private val repository: SettingsRepository) {
    fun get(ctx: RequestContext, key: String): List<SettingsResponse> = repository.getById(ctx, key).map {
        SettingsResponse(
            SettingsID = it.SettingsID,
            RemoteID = it.RemoteID,
            SettingsKey = it.SettingsKey,
            SettingsValue = it.SettingsValue,
            SettingsStyle = it.SettingsStyle,
            SettingsDescription = it.SettingsDescription,
            SystemCategory = it.SystemCategory,
            LastModified = DateUtils.storageToUi(it.LastModified),
            DateCreated = DateUtils.storageToUi(it.DateCreated),
            Version = it.Version
        )
    }

    fun create(ctx: RequestContext, key: String, request: CreateSettingsResponse): Settings {
        require(request.SettingsValue?.isNotBlank() == true) { "SettingsValue is required" }
        val now = DateUtils.nowStorage()
        return repository.save(ctx, Settings(
            SettingsID = UUID.randomUUID().toString(),
            SettingsKey = key,
            SettingsValue = request.SettingsValue.trim(),
            SettingsStyle = request.SettingsStyle,
            SettingsDescription = request.SettingsDescription,
            SystemCategory = validatedSystemCategory(key, request.SystemCategory),
            LastModified = now,
            DateCreated = now,
            Version = "1"
        ))
    }
}
