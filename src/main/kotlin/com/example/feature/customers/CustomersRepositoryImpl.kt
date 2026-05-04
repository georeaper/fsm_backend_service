package com.example.feature.customers

import com.example.core.DatabaseProvider
import com.example.feature.customers.dto.CustomersDto
import com.example.core.RequestContext
import com.example.models.api.Customers
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.models.databaseModels.customerTable
import org.jetbrains.exposed.sql.selectAll

class CustomerRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : CustomersRepository {


    override fun save(customer: Customers): Customers {
        TODO("Not yet implemented")
    }

    override fun findAll(ctx: RequestContext): List<CustomersDto> {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            // query εδώ
            val map = customerTable.selectAll().map {
                CustomersDto(
                    id = it[customerTable.customerId],
                    name = it[customerTable.name],
                    email = it[customerTable.email],
                    phone = it[customerTable.phone],
                    address = it[customerTable.address],
                    city = it[customerTable.city],
                    zipCode = it[customerTable.zipCode],
                    notes = it[customerTable.notes],
                    description = it[customerTable.description],
                    dateCreated = it[customerTable.dateCreated],
                    lastModified = it[customerTable.lastModified],
                    status = it[customerTable.customerStatus]?.toString()
                )
            }
            map
        }
    }
}