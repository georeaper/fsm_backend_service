package com.example.feature.settings.repository

import com.example.core.RequestContext
import com.example.feature.settings.dto.EditSettingsResponse
import com.example.feature.settings.dto.SettingsResponse
import com.example.models.api.Settings

interface SettingsRepository {
    fun save(ctx: RequestContext, data: Settings): Settings
    fun findAll(ctx: RequestContext): List<SettingsResponse>
    fun edit(ctx: RequestContext, data: EditSettingsResponse): EditSettingsResponse
    fun getById(ctx: RequestContext, settingsId: String): List<SettingsResponse>
}
