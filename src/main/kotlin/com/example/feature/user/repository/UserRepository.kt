package com.example.feature.user.repository

import com.example.core.RequestContext
import com.example.feature.user.dto.EditUserResponse
import com.example.feature.user.dto.UsersResponse
import com.example.models.api.Users

interface UserRepository {
    fun save(ctx: RequestContext, data: Users): Users
    fun findAll(ctx: RequestContext): List<UsersResponse>
    fun edit(ctx: RequestContext, data: EditUserResponse): EditUserResponse
}
