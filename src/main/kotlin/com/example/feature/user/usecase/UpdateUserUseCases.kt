package com.example.feature.user.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.user.dto.ChangePasswordRequest
import com.example.feature.user.dto.UpdateMyProfileRequest
import com.example.feature.user.dto.UpdateUserRequest
import com.example.feature.user.dto.UserResponse
import com.example.feature.user.repository.AuthUserProvisioningRepository
import com.example.feature.user.repository.UserProfileUpdate
import com.example.feature.user.repository.UserRepository
import com.example.infrastructure.security.PasswordVerifier

class UpdateUserUseCase(
    private val userRepository: UserRepository,
    private val authUserRepository: AuthUserProvisioningRepository
) {
    fun execute(
        authenticated: AuthenticatedRequestContext,
        tenantUserId: String,
        request: UpdateUserRequest
    ): UserResponse? {
        require(authenticated.role in setOf("owner", "admin")) { "Insufficient permissions" }
        val authUserId = userRepository.findAuthUserId(authenticated.toRequestContext(), tenantUserId)
            ?: return null
        val existing = authUserRepository.findUser(authenticated.companyId, authUserId) ?: return null
        if (existing.role == "owner") {
            require(authenticated.userId == authUserId && request.role.trim().lowercase() == "owner") {
                "The owner account cannot be reassigned"
            }
        } else if (authenticated.role == "admin") {
            require(existing.role == "user") { "Admins cannot modify owner or admin accounts" }
        }

        val username = request.username.trim()
        val email = request.email.trim()
        val role = request.role.trim().lowercase()
        require(username.isNotEmpty() && username.length <= 255) { "Username is required" }
        require(email.isNotEmpty() && email.length <= 255 && '@' in email) { "Valid email is required" }
        require(role in setOf("user", "admin", "owner")) { "Invalid role" }
        require(role != "owner" || existing.role == "owner") { "A second owner cannot be assigned" }
        request.password?.let {
            require(it.length in 8..1024) { "Password must contain at least 8 characters" }
        }

        val newPasswordHash = request.password
            ?.takeIf { it.isNotEmpty() }
            ?.let { BCrypt.withDefaults().hashToString(12, it.toCharArray()) }
        val previous = authUserRepository.updateUser(
            authenticated.companyId,
            authUserId,
            username,
            email,
            newPasswordHash,
            role
        ) ?: return null

        val updated = try {
            userRepository.updateProfile(
                authenticated.toRequestContext(),
                tenantUserId,
                UserProfileUpdate(
                    name = request.name,
                    description = request.description,
                    email = email,
                    phone = request.phone,
                    reportPrefix = request.reportPrefix,
                    technicalCasePrefix = request.technicalCasePrefix
                )
            )
        } catch (error: Exception) {
            authUserRepository.restoreUser(authenticated.companyId, previous)
            throw error
        }
        if (!updated) {
            authUserRepository.restoreUser(authenticated.companyId, previous)
            return null
        }

        return UserResponse(
            userId = tenantUserId,
            authUserId = authUserId,
            username = username,
            name = request.name,
            email = email,
            phone = request.phone,
            description = request.description,
            reportPrefix = request.reportPrefix,
            technicalCasePrefix = request.technicalCasePrefix,
            role = role,
            status = existing.status
        )
    }
}

class UpdateMyProfileUseCase(private val userRepository: UserRepository) {
    fun execute(
        authenticated: AuthenticatedRequestContext,
        request: UpdateMyProfileRequest
    ): Boolean {
        val tenantUserId = userRepository.findTenantUserId(
            authenticated.toRequestContext(),
            authenticated.userId
        ) ?: return false
        return userRepository.updateProfile(
            authenticated.toRequestContext(),
            tenantUserId,
            UserProfileUpdate(
                name = request.name,
                description = request.description,
                email = null,
                phone = request.phone,
                reportPrefix = request.reportPrefix,
                technicalCasePrefix = request.technicalCasePrefix
            )
        )
    }
}

class ChangeMyPasswordUseCase(
    private val authUserRepository: AuthUserProvisioningRepository
) {
    fun execute(
        authenticated: AuthenticatedRequestContext,
        request: ChangePasswordRequest
    ): Boolean {
        require(request.newPassword.length in 8..1024) {
            "New password must contain at least 8 characters"
        }
        val account = authUserRepository.findUser(authenticated.companyId, authenticated.userId)
            ?: return false
        if (!PasswordVerifier.verify(request.currentPassword, account.passwordHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }
        val passwordHash = BCrypt.withDefaults()
            .hashToString(12, request.newPassword.toCharArray())
        return authUserRepository.updatePassword(
            authenticated.companyId,
            authenticated.userId,
            passwordHash
        )
    }
}
