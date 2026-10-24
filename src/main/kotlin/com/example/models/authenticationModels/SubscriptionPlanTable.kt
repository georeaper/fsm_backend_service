package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object SubscriptionPlanTable : Table("subscription_plans") {
    val id = integer("id").autoIncrement()
    val code = varchar("code", 50).uniqueIndex()
    val name = varchar("name", 100)
    val monthlyPriceCents = long("monthly_price_cents")
    val includedUsers = integer("included_users")
    val extraUserPriceCents = long("extra_user_price_cents").nullable()
    val currency = varchar("currency", 3).default("EUR")
    val active = bool("active").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
