package com.example.feature.settings.usecase

import com.example.core.RequestContext

import com.example.feature.settings.repository.SettingsRepository

class DeleteSettingsUseCase(private val repository: SettingsRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}