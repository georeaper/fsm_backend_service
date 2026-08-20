package com.example.feature.customers.usecase

import com.example.core.RequestContext
import com.example.feature.customers.repository.CustomersRepository

class DeleteCustomerUseCase(
    private val repository: CustomersRepository
) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
