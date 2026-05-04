package com.example.feature.customers

import com.example.core.DatabaseProvider
import com.example.core.DateUtils
import com.example.feature.customers.dto.CustomersDto
import com.example.core.RequestContext
import com.example.models.api.Customers
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.models.databaseModels.customerTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.Date

class CustomerRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : CustomersRepository {


    override fun save(ctx: RequestContext, customer: Customers): Customers {
        val db = dbProvider.getDatabase(ctx.dbName)



        transaction(db) {
            customerTable.insert {
                it[customerId] = customer.CustomerID
                it[name] = customer.Name
                it[email] = customer.Email
                it[phone] = customer.Phone
                it[address] = customer.Address
                it[city] = customer.City
                it[zipCode] = customer.ZipCode
                it[notes] = customer.Notes
                it[description] = customer.Description
                it[customerStatus] = customer.CustomerStatus
                it[dateCreated] = customer.DateCreated
                it[lastModified] = customer.LastModified
                it[version] = customer.Version
                it[remoteId] = customer.RemoteID
            }
        }

        return customer
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

