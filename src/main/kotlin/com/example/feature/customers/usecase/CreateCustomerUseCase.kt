package com.example.feature.customers.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.customers.repository.CustomersRepository
import com.example.models.api.Customers
import java.util.UUID

class CreateCustomerUseCase(
    private val repository: CustomersRepository
) {

    fun execute(
        ctx: RequestContext,
        input: CreateCustomerRequest
    ): Customers {

        // ✅ validation
//        require(input.name.isNotBlank()) { "Name is required" }
//        require(input.email.isNotBlank()) { "Email is required" }

        // basic normalization
        val email = input.email?.trim()?.lowercase()

        // (optional simple validation)
//        require(email?.contains("@")) { "Invalid email format" }

//        val now = Instant.now()
        val storageDate = DateUtils.nowStorage()

        val customer = Customers(
            CustomerID = UUID.randomUUID().toString(),
            Name = input.name?.trim(),
            Email = email,
            Phone = input.phone?.trim(),
            Address = input.address?.trim(),
            City = input.city?.trim(),
            ZipCode = input.zipCode?.trim(),
            Notes = input.notes?.trim(),
            Description = input.description?.trim(),
            CustomerStatus = true,
            DateCreated = storageDate,
            LastModified = storageDate,
            Version = "",
            RemoteID = null
        )

        val saved = repository.save(ctx, customer)

        return customer
    }
}