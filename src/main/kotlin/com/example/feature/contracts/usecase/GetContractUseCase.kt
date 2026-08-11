package com.example.feature.contracts.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.contracts.repository.ContractRepository
import com.example.feature.contracts.dto.ContractsResponse

class GetContractUseCase (private val repository: ContractRepository){
    fun execute(ctx: RequestContext) :List<ContractsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            ContractsResponse(
                ContractID = it.ContractID,
                RemoteID = it.RemoteID,
                Title = it.Title,
                DateStart = DateUtils.storageToUi(it.DateStart),
                DateEnd = DateUtils.storageToUi(it.DateEnd),
                Value = it.Value,
                Notes = it.Notes,
                Description = it.Description,
                ContractType = it.ContractType,
                ContractStatus = it.ContractStatus,
                ContactName = it.ContactName,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version,
                CustomerID = it.CustomerID,
                CustomerName = it.CustomerName
            )
        }
    }
}
