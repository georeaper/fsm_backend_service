package com.example.models.authenticationModels

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object CompanySubscriptionTable : Table("company_subscriptions") {
    val id = integer("id").autoIncrement()
    val companyId = integer("company_id").references(CompanyTable.id).uniqueIndex()
    val planId = integer("plan_id").references(SubscriptionPlanTable.id)
    val status = varchar("status", 30).default("active")
    val additionalUserSeats = integer("additional_user_seats").default(0)
    val currentPeriodStart = datetime("current_period_start").nullable()
    val currentPeriodEnd = datetime("current_period_end").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
