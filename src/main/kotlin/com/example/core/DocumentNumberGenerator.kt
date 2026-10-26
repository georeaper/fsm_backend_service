package com.example.core

import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

/** Generates per-creator document numbers inside the caller's tenant transaction. */
object DocumentNumberGenerator {
    fun nextWorkOrderNumber(ctx: RequestContext): String = next(
        ctx = ctx,
        prefixName = "Work Order",
        prefix = { it[userTable.reportPrefix] },
        current = { it[userTable.lastReportNumber] },
        updateCounter = { userId, value ->
            userTable.update({ userTable.userId eq userId }) { it[lastReportNumber] = value }
        }
    )

    fun nextTechnicalCaseNumber(ctx: RequestContext): String = next(
        ctx = ctx,
        prefixName = "Technical Case",
        prefix = { it[userTable.technicalCasePrefix] },
        current = { it[userTable.lastTCNumber] },
        updateCounter = { userId, value ->
            userTable.update({ userTable.userId eq userId }) { it[lastTCNumber] = value }
        }
    )

    private fun next(
        ctx: RequestContext,
        prefixName: String,
        prefix: (org.jetbrains.exposed.sql.ResultRow) -> String?,
        current: (org.jetbrains.exposed.sql.ResultRow) -> Int?,
        updateCounter: (String, Int) -> Unit
    ): String {
        val authUserId = ctx.authUserId
            ?: throw DocumentNumberException("Authenticated user is required to generate a $prefixName number")
        val user = userTable.select {
            (userTable.authUserId eq authUserId) and (userTable.isDeleted eq false)
        }.forUpdate().singleOrNull()
            ?: throw DocumentNumberException("Authenticated tenant user was not found")
        val configuredPrefix = prefix(user)?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw DocumentNumberException("$prefixName prefix is not configured for the authenticated user")
        val nextValue = Math.addExact(current(user) ?: 0, 1)
        updateCounter(user[userTable.userId], nextValue)
        return "${configuredPrefix.trimEnd('-')}-$nextValue"
    }
}

class DocumentNumberException(message: String) : IllegalArgumentException(message)
