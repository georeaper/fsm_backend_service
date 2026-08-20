package com.example.feature.equipments.usecase

import com.example.core.RequestContext
import com.example.feature.equipments.repository.EquipmentRepository

class DeleteEquipmentUseCase(
    private val repository: EquipmentRepository
) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
