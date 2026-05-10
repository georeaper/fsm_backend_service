package com.example.feature.settings.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditSettingsResponse(
    val SettingsID: String,
    val SettingsKey: String?,
    val SettingsValue: String?,
    val SettingsStyle: String?,
    val SettingsDescription: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?
)
