package com.example.feature.user.usecase

import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.user.repository.AuthUserProvisioningRepository
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.dto.UserResponse

class GetUserUseCase(
    private val repository: UserRepository,
    private val authUserRepository: AuthUserProvisioningRepository
) {
    fun execute(authenticated: AuthenticatedRequestContext): List<UserResponse> {
        require(authenticated.role in setOf("owner", "admin")) { "Insufficient permissions" }
        val tenantUsers = repository.findAll(authenticated.toRequestContext())
        val authUsers = authUserRepository.findUsers(
            authenticated.companyId,
            tenantUsers.mapNotNull { it.AuthUserID }
        )
        return tenantUsers.map { tenant ->
            val auth = tenant.AuthUserID?.let(authUsers::get)
            UserResponse(
                userId = tenant.UserID,
                authUserId = tenant.AuthUserID,
                username = auth?.username,
                name = tenant.Name,
                email = auth?.email ?: tenant.Email,
                phone = tenant.Phone,
                description = tenant.Description,
                reportPrefix = tenant.ReportPrefix,
                technicalCasePrefix = tenant.TechnicalCasePrefix,
                role = auth?.role,
                status = auth?.status
            )
        }
    }
}
