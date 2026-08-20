package com.example.feature.contracts.usecase

import com.example.core.RequestContext
import com.example.feature.contracts.repository.ContractRepository

class DeleteContractUseCase(private val repository: ContractRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
