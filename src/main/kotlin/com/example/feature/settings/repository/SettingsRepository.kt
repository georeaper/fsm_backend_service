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
    fun findSettingById(ctx: RequestContext, settingId: String): SettingsResponse?
    fun updateStyle(ctx: RequestContext, settingId: String, style: String, lastModified: String): Boolean
    fun updateSystemCategory(ctx: RequestContext, settingId: String, systemCategory: String, lastModified: String): Boolean
    fun delete(ctx: RequestContext, id: String): Boolean
}
