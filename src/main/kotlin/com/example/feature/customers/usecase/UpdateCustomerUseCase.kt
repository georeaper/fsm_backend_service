package com.example.feature.customers.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.customers.repository.CustomersRepository
import com.example.models.api.Customers

class UpdateCustomerUseCase(
    private val repository: CustomersRepository
) {
    fun execute(ctx: RequestContext, id: String, input: CreateCustomerRequest): Customers? {
        val customer = Customers(
            CustomerID = id,
            Name = input.name?.trim(),
            Email = input.email?.trim()?.lowercase(),
            Phone = input.phone?.trim(),
            Address = input.address?.trim(),
            City = input.city?.trim(),
            ZipCode = input.zipCode?.trim(),
            Notes = input.notes?.trim(),
            Description = input.description?.trim(),
            CustomerStatus = input.status,
            LastModified = DateUtils.nowStorage()
        )

        return if (repository.update(ctx, customer)) customer else null
    }
}
