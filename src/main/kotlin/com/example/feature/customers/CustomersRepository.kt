package com.example.feature.customers

import com.example.feature.customers.dto.CustomersDto
import com.example.core.RequestContext
import com.example.models.api.Customers

interface CustomersRepository {
    fun save(customer: Customers): Customers
    fun findAll(ctx: RequestContext): List<CustomersDto>
}