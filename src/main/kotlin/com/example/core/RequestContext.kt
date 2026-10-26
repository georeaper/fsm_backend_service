package com.example.core

data class RequestContext(
    val dbName: String,
    val username: String?,
    val authUserId: Int? = null
)
