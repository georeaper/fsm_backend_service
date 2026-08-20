package com.example.feature.customers.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.customers.dto.CustomersDto
import com.example.models.api.Customers
import com.example.models.databaseModels.customerTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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
            val map = customerTable
                .select { customerTable.isDeleted eq false }
                .map {
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

    override fun update(ctx: RequestContext, customer: Customers): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            customerTable.update({
                (customerTable.customerId eq customer.CustomerID) and
                    (customerTable.isDeleted eq false)
            }) {
                it[name] = customer.Name
                it[email] = customer.Email
                it[phone] = customer.Phone
                it[address] = customer.Address
                it[city] = customer.City
                it[zipCode] = customer.ZipCode
                it[notes] = customer.Notes
                it[description] = customer.Description
                it[customerStatus] = customer.CustomerStatus
                it[lastModified] = customer.LastModified
            } > 0
        }
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)

        return transaction(db) {
            customerTable.update({
                (customerTable.customerId eq id) and
                    (customerTable.isDeleted eq false)
            }) {
                it[isDeleted] = true
            } > 0
        }
    }
}
