package com.example.feature.customers.repository

import com.example.core.RequestContext
import com.example.feature.customers.dto.CustomersDto
import com.example.models.api.Customers

interface CustomersRepository {
    fun save(ctx: RequestContext, customer: Customers): Customers
    fun findAll(ctx: RequestContext): List<CustomersDto>
    fun update(ctx: RequestContext, customer: Customers): Boolean
    fun delete(ctx: RequestContext, id: String): Boolean
}
