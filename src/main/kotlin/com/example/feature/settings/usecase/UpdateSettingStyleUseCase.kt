package com.example.feature.settings.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.settings.dto.UpdateSettingStyleRequest
import com.example.feature.settings.dto.UpdateSettingStyleResponse
import com.example.feature.settings.repository.SettingsRepository

class UpdateSettingStyleUseCase(private val repository: SettingsRepository) {
    fun execute(ctx: RequestContext, settingId: String, request: UpdateSettingStyleRequest): UpdateSettingStyleResponse? {
        val style = validatedSettingStyle(request.style)
        return if (repository.updateStyle(ctx, settingId, style, DateUtils.nowStorage())) {
            UpdateSettingStyleResponse(settingId, style)
        } else null
    }
}

internal fun validatedSettingStyle(style: String): String {
    val normalized = style.trim()
    require(HEX_COLOR.matches(normalized)) {
        "style must be a hex color in #RGB, #RRGGBB, or #RRGGBBAA format"
    }
    return normalized
}

private val HEX_COLOR = Regex("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
