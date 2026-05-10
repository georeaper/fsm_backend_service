package com.example.feature.contracts.repository

import com.example.core.RequestContext
import com.example.feature.contracts.dto.EditContractResponse
import com.example.feature.contracts.dto.ContractsResponse
import com.example.models.api.Contracts

interface ContractRepository {
    fun save(ctx: RequestContext, data: Contracts): Contracts
    fun findAll(ctx: RequestContext): List<ContractsResponse>
    fun edit(ctx: RequestContext, data: EditContractResponse): EditContractResponse
}
