package com.example.feature.user.repository

import com.example.core.RequestContext
import com.example.feature.user.dto.EditUserResponse
import com.example.feature.user.dto.UsersResponse
import com.example.models.api.Users

data class UserProfileUpdate(
    val name: String?,
    val description: String?,
    val email: String?,
    val phone: String?,
    val reportPrefix: String?,
    val technicalCasePrefix: String?
)

interface UserRepository {
    fun save(ctx: RequestContext, data: Users): Users
    fun findAll(ctx: RequestContext): List<UsersResponse>
    fun edit(ctx: RequestContext, data: EditUserResponse): EditUserResponse
    fun findAuthUserId(ctx: RequestContext, tenantUserId: String): Int?
    fun findTenantUserId(ctx: RequestContext, authUserId: Int): String?
    fun findByAuthUserId(ctx: RequestContext, authUserId: Int): UsersResponse?
    fun updateProfile(ctx: RequestContext, tenantUserId: String, data: UserProfileUpdate): Boolean
}
