package com.example.feature.user.usecase

import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.user.repository.AuthUserProvisioningRepository
import com.example.feature.user.repository.UserRepository

class ChangeUserStatusUseCase(
    private val userRepository: UserRepository,
    private val authUserRepository: AuthUserProvisioningRepository
) {
    fun activate(authenticated: AuthenticatedRequestContext, tenantUserId: String): Boolean {
        requireAdmin(authenticated)
        val authUserId = userRepository.findAuthUserId(authenticated.toRequestContext(), tenantUserId)
            ?: return false
        return authUserRepository.activateUser(authenticated.companyId, authUserId)
    }

    fun deactivate(authenticated: AuthenticatedRequestContext, tenantUserId: String): Boolean {
        requireAdmin(authenticated)
        val authUserId = userRepository.findAuthUserId(authenticated.toRequestContext(), tenantUserId)
            ?: return false
        require(authUserId != authenticated.userId) { "You cannot deactivate your own account" }
        return authUserRepository.deactivateUser(authenticated.companyId, authUserId)
    }

    private fun requireAdmin(authenticated: AuthenticatedRequestContext) {
        require(authenticated.role in setOf("owner", "admin")) { "Insufficient permissions" }
    }
}
