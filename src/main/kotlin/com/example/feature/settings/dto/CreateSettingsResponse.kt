package com.example.feature.settings.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSettingsResponse(
    val SettingsKey: String?,
    val SettingsValue: String?,
    val SettingsStyle: String?,
    val SettingsDescription: String?
)
