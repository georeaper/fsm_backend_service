package com.example.feature.subscription

class UserLimitReachedException(
    val activeUsers: Long,
    val allowedUsers: Int
) : RuntimeException("Subscription user limit reached")

class SubscriptionInactiveException : RuntimeException("Subscription is not active")
