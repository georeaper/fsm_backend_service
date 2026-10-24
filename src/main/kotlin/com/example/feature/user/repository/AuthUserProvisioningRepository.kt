package com.example.feature.user.repository

data class AuthUserAccount(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val status: String
)

interface AuthUserProvisioningRepository {
    fun createActiveUser(
        companyId: Int,
        username: String,
        email: String,
        passwordHash: String,
        role: String
    ): Int

    fun activateUser(companyId: Int, authUserId: Int): Boolean
    fun deactivateUser(companyId: Int, authUserId: Int): Boolean
    fun deleteProvisionedUser(companyId: Int, authUserId: Int)
    fun findUsers(companyId: Int, authUserIds: Collection<Int>): Map<Int, AuthUserAccount>
    fun findUser(companyId: Int, authUserId: Int): AuthUserAccount?
    fun updateUser(
        companyId: Int,
        authUserId: Int,
        username: String,
        email: String,
        passwordHash: String?,
        role: String
    ): AuthUserAccount?
    fun restoreUser(companyId: Int, account: AuthUserAccount)
    fun updatePassword(companyId: Int, authUserId: Int, passwordHash: String): Boolean
}
