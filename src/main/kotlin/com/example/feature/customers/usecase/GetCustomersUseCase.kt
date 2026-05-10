package com.example.feature.customers.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.customers.dto.CustomerResponse
import com.example.feature.customers.repository.CustomersRepository

class GetCustomersUseCase(
    private val repository: CustomersRepository
) {
    fun execute(ctx: RequestContext): List<CustomerResponse> {
        val customers = repository.findAll(ctx)
        return customers.map { it->
            CustomerResponse(
                id = it.id,
                name = it.name,
                email = it.email,
                phone = it.phone,
                address = it.address,
                city = it.city,
                zipCode = it.zipCode,
                notes = it.notes,
                description = it.description,
                dateCreated = DateUtils.storageToUi(it.dateCreated) ?: "",
                lastModified = DateUtils.storageToUi(it.lastModified) ?: "",
                status = it.status
            )

        }
    }
}