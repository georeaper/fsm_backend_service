package com.example.feature.auth.service

import com.example.feature.auth.repository.AuthContextRepository
import io.ktor.server.auth.jwt.JWTPrincipal

class AuthenticatedRequestContextResolver(
    private val repository: AuthContextRepository
) {
    fun resolve(principal: JWTPrincipal): AuthenticatedRequestContext? {
        val userId = principal.payload.subject?.toIntOrNull() ?: return null
        val companyId = principal.payload.getClaim("tenant_id").asInt() ?: return null
        val tokenRole = principal.payload.getClaim("role").asString()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return resolve(userId, companyId, tokenRole)
    }

    internal fun resolve(
        userId: Int,
        companyId: Int,
        tokenRole: String
    ): AuthenticatedRequestContext? {
        if (userId <= 0 || companyId <= 0 || tokenRole.isBlank()) return null

        val user = repository.findActiveUser(userId, companyId) ?: return null

        // Reject stale tokens after a role change instead of trusting the claim alone.
        if (user.role != tokenRole || user.databaseName.isBlank()) return null

        return AuthenticatedRequestContext(
            dbName = user.databaseName,
            username = user.username,
            userId = user.userId,
            companyId = user.companyId,
            role = user.role
        )
    }
}
