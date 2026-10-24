package com.example.feature.user.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.core.RequestContext
import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.user.repository.AuthUserProvisioningRepository
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.dto.CreateUserResponse
import com.example.feature.user.dto.UserResponse
import com.example.models.api.Users
import com.example.core.DateUtils
import java.util.UUID

class CreateUserUseCase(
    private val repository: UserRepository,
    private val authUserRepository: AuthUserProvisioningRepository
) {
    fun execute(authenticated: AuthenticatedRequestContext, request: CreateUserResponse): UserResponse {
        require(authenticated.role in setOf("owner", "admin")) { "Insufficient permissions" }
        val username = request.username.trim()
        val email = request.email.trim()
        val password = request.password
        val role = request.role.trim().lowercase()
        require(username.isNotEmpty() && username.length <= 255) { "Username is required" }
        require(email.isNotEmpty() && email.length <= 255) { "Email is required" }
        require(password.length in 8..1024) { "Password must contain at least 8 characters" }
        require(role in setOf("user", "admin")) { "Role must be user or admin" }

        val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val authUserId = authUserRepository.createActiveUser(
            authenticated.companyId,
            username,
            email,
            passwordHash,
            role
        )
        val storageDate = DateUtils.nowStorage()
        val users = Users(
            UserID = UUID.randomUUID().toString(),
            RemoteID = null,
            AuthUserID = authUserId,
            Name = request.name,
            Description = request.description,
            Email = email,
            Phone = request.phone,
            Signature = null,
            ReportPrefix = request.reportPrefix,
            TechnicalCasePrefix = request.technicalCasePrefix,
            LastReportNumber = null,
            LastTCNumber = null,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1"
        )
        return try {
            val saved = repository.save(authenticated.toRequestContext(), users)
            UserResponse(
                userId = saved.UserID,
                authUserId = authUserId,
                username = username,
                name = saved.Name,
                email = saved.Email,
                phone = saved.Phone,
                description = saved.Description,
                reportPrefix = saved.ReportPrefix,
                technicalCasePrefix = saved.TechnicalCasePrefix,
                role = role,
                status = "active"
            )
        } catch (error: Exception) {
            authUserRepository.deleteProvisionedUser(authenticated.companyId, authUserId)
            throw error
        }
    }
}
