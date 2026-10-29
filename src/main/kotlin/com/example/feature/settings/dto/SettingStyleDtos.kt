package com.example.feature.settings.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSettingStyleRequest(val style: String)

@Serializable
data class UpdateSettingStyleResponse(val settingId: String, val style: String)

@Serializable
data class SettingApiError(val code: String, val message: String)

@Serializable
data class UpdateSystemCategoryRequest(val systemCategory: String)

@Serializable
data class UpdateSystemCategoryResponse(val settingId: String, val systemCategory: String)
