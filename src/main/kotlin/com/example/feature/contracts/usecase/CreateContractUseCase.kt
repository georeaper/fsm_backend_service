package com.example.feature.contracts.usecase

import com.example.core.RequestContext
import com.example.feature.contracts.repository.ContractRepository
import com.example.feature.contracts.dto.CreateContractResponse
import com.example.models.api.Contracts
import com.example.core.DateUtils
import java.util.UUID

class CreateContractUseCase (private val repository: ContractRepository){
    fun execute(ctx: RequestContext, request: CreateContractResponse) : Contracts {
        val storageDate = DateUtils.nowStorage()
        val contracts = Contracts(
            ContractID = UUID.randomUUID().toString(),
            RemoteID = null,
            Title = request.Title,
            DateStart = DateUtils.uiToStorage(request.DateStart!!),
            DateEnd = DateUtils.uiToStorage(request.DateEnd!!),
            Value = request.Value,
            Notes = request.Notes,
            Description = request.Description,
            ContractType = request.ContractType,
            ContractStatus = request.ContractStatus,
            ContactName = request.ContactName,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1",
            CustomerID = request.CustomerID
        )
        return repository.save(ctx, contracts)
    }
}
