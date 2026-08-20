package com.example.feature.contracts.usecase

import com.example.core.RequestContext
import com.example.feature.contracts.dto.EditContractResponse
import com.example.feature.contracts.repository.ContractRepository

class UpdateContractUseCase(private val repository: ContractRepository) {
    fun execute(ctx: RequestContext, input: EditContractResponse): EditContractResponse {
        return repository.edit(ctx, input)
    }
}
