package com.example.feature.subscription.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionUsageResponse(
    val status: String,
    val plan: String,
    val monthlyPriceCents: Long,
    val extraUserPriceCents: Long?,
    val currency: String,
    val includedUsers: Int,
    val additionalUsers: Int,
    val allowedUsers: Int,
    val activeUsers: Long,
    val availableUsers: Long
)

@Serializable
data class SubscriptionErrorResponse(val code: String, val message: String)

@Serializable
data class UserLimitErrorResponse(
    val code: String = "USER_LIMIT_REACHED",
    val message: String,
    val activeUsers: Long,
    val allowedUsers: Int
)
