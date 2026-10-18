package com.example.feature.auth.service

import com.example.feature.auth.repository.AuthContextRepository
import com.example.feature.auth.repository.AuthContextUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthenticatedRequestContextResolverTest {
    private val activeUser = AuthContextUser(
        userId = 7,
        companyId = 11,
        username = "admin",
        role = "owner",
        databaseName = "tenant_11"
    )

    @Test
    fun `resolves an active user in the token tenant`() {
        val resolver = resolverReturning(activeUser)

        val result = resolver.resolve(userId = 7, companyId = 11, tokenRole = "owner")

        assertEquals(
            AuthenticatedRequestContext(
                dbName = "tenant_11",
                username = "admin",
                userId = 7,
                companyId = 11,
                role = "owner"
            ),
            result
        )
        assertEquals("tenant_11", result?.toRequestContext()?.dbName)
    }

    @Test
    fun `rejects missing user or tenant mismatch`() {
        val resolver = resolverReturning(null)

        assertNull(resolver.resolve(userId = 7, companyId = 99, tokenRole = "owner"))
    }

    @Test
    fun `rejects stale role claim`() {
        val resolver = resolverReturning(activeUser)

        assertNull(resolver.resolve(userId = 7, companyId = 11, tokenRole = "admin"))
    }

    @Test
    fun `rejects invalid identifiers and blank database name`() {
        val resolver = resolverReturning(activeUser)
        assertNull(resolver.resolve(userId = 0, companyId = 11, tokenRole = "owner"))
        assertNull(resolver.resolve(userId = 7, companyId = 0, tokenRole = "owner"))
        assertNull(resolver.resolve(userId = 7, companyId = 11, tokenRole = " "))

        val blankDatabaseResolver = resolverReturning(activeUser.copy(databaseName = " "))
        assertNull(blankDatabaseResolver.resolve(7, 11, "owner"))
    }

    private fun resolverReturning(user: AuthContextUser?) =
        AuthenticatedRequestContextResolver(
            object : AuthContextRepository {
                override fun findActiveUser(userId: Int, companyId: Int): AuthContextUser? = user
            }
        )
}
