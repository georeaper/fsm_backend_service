package com.example.feature.subscription.repository

import com.example.feature.subscription.dto.SubscriptionUsageResponse

interface SubscriptionRepository {
    fun usage(companyId: Int): SubscriptionUsageResponse?
}
