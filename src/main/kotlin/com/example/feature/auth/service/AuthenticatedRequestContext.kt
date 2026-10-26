package com.example.feature.auth.service

import com.example.core.RequestContext

data class AuthenticatedRequestContext(
    val dbName: String,
    val username: String,
    val userId: Int,
    val companyId: Int,
    val role: String
) {
    fun toRequestContext(): RequestContext = RequestContext(
        dbName = dbName,
        username = username,
        authUserId = userId
    )
}
