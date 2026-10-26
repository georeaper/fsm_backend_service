package com.example.core

import com.example.models.databaseModels.userTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DocumentNumberGeneratorTest {
    @Test
    fun `generates independent sequential work order and technical case numbers`() = withUser("WO-GP", "TC-GP") { ctx ->
        assertEquals("WO-GP-1", DocumentNumberGenerator.nextWorkOrderNumber(ctx))
        assertEquals("WO-GP-2", DocumentNumberGenerator.nextWorkOrderNumber(ctx))
        assertEquals("TC-GP-1", DocumentNumberGenerator.nextTechnicalCaseNumber(ctx))

        val user = userTable.select { userTable.authUserId eq 77 }.single()
        assertEquals(2, user[userTable.lastReportNumber])
        assertEquals(1, user[userTable.lastTCNumber])
    }

    @Test
    fun `does not duplicate a trailing prefix separator`() = withUser("WO-GP-", "TC-GP-") { ctx ->
        assertEquals("WO-GP-1", DocumentNumberGenerator.nextWorkOrderNumber(ctx))
        assertEquals("TC-GP-1", DocumentNumberGenerator.nextTechnicalCaseNumber(ctx))
    }

    @Test
    fun `rejects generation when creator prefix is missing`() = withUser(null, "TC-GP") { ctx ->
        assertFailsWith<DocumentNumberException> {
            DocumentNumberGenerator.nextWorkOrderNumber(ctx)
        }
    }

    private fun withUser(reportPrefix: String?, casePrefix: String?, block: (RequestContext) -> Unit) {
        val db = Database.connect(
            url = "jdbc:h2:mem:numbering-${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(userTable)
            userTable.insert {
                it[userId] = UUID.randomUUID().toString()
                it[authUserId] = 77
                it[userTable.reportPrefix] = reportPrefix
                it[technicalCasePrefix] = casePrefix
            }
            block(RequestContext("unused", "creator", authUserId = 77))
        }
    }
}
