package com.example.feature.settings.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.settings.dto.UpdateSystemCategoryRequest
import com.example.feature.settings.dto.UpdateSystemCategoryResponse
import com.example.feature.settings.repository.SettingsRepository

class UpdateSystemCategoryUseCase(private val repository: SettingsRepository) {
    fun execute(ctx: RequestContext, settingId: String, request: UpdateSystemCategoryRequest): UpdateSystemCategoryResponse? {
        val setting = repository.findSettingById(ctx, settingId) ?: return null
        val category = validatedSystemCategory(setting.SettingsKey, request.systemCategory)
            ?: throw IllegalArgumentException("This setting type does not support SystemCategory")
        return if (repository.updateSystemCategory(ctx, settingId, category, DateUtils.nowStorage())) {
            UpdateSystemCategoryResponse(settingId, category)
        } else null
    }
}
