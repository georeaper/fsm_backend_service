package com.example.feature.notification.repository

import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.CompanySubscriptionTable
import com.example.models.authenticationModels.CompanyTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class NotificationTenantDirectoryImpl : NotificationTenantDirectory {
    override fun activeTenants(): List<TenantDescriptor> = transaction(DatabaseFactory.centralDb) {
        (CompanyTable innerJoin CompanySubscriptionTable)
            .slice(CompanyTable.id, CompanyTable.databaseName, CompanyTable.timeZone)
            .select { CompanySubscriptionTable.status inList listOf("active", "trial") }
            .map { TenantDescriptor(it[CompanyTable.id], it[CompanyTable.databaseName], it[CompanyTable.timeZone]) }
    }
}
