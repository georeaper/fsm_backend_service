package com.example.feature.subscription.repository

import com.example.feature.subscription.dto.SubscriptionUsageResponse
import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.CompanySubscriptionTable
import com.example.models.authenticationModels.SubscriptionPlanTable
import com.example.models.authenticationModels.UserTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SubscriptionRepositoryImpl : SubscriptionRepository {
    override fun usage(companyId: Int): SubscriptionUsageResponse? =
        transaction(DatabaseFactory.centralDb) {
            val row = (CompanySubscriptionTable innerJoin SubscriptionPlanTable)
                .select { CompanySubscriptionTable.companyId eq companyId }
                .limit(1)
                .firstOrNull() ?: return@transaction null

            val activeUsers = UserTable
                .select {
                    (UserTable.companyId eq companyId) and
                        (UserTable.status eq "active")
                }
                .count()
            val included = row[SubscriptionPlanTable.includedUsers]
            val additional = row[CompanySubscriptionTable.additionalUserSeats]
            val allowed = included + additional

            SubscriptionUsageResponse(
                status = row[CompanySubscriptionTable.status],
                plan = row[SubscriptionPlanTable.code],
                monthlyPriceCents = row[SubscriptionPlanTable.monthlyPriceCents],
                extraUserPriceCents = row[SubscriptionPlanTable.extraUserPriceCents],
                currency = row[SubscriptionPlanTable.currency],
                includedUsers = included,
                additionalUsers = additional,
                allowedUsers = allowed,
                activeUsers = activeUsers,
                availableUsers = (allowed.toLong() - activeUsers).coerceAtLeast(0)
            )
        }
}
