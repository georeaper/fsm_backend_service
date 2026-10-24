package com.example.feature.user

import com.example.core.RequestContext
import com.example.feature.auth.service.AuthenticatedRequestContext
import com.example.feature.user.dto.CreateUserResponse
import com.example.feature.user.dto.EditUserResponse
import com.example.feature.user.dto.UpdateMyProfileRequest
import com.example.feature.user.dto.UsersResponse
import com.example.feature.user.repository.AuthUserAccount
import com.example.feature.user.repository.AuthUserProvisioningRepository
import com.example.feature.user.repository.UserProfileUpdate
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.usecase.CreateUserUseCase
import com.example.feature.user.usecase.GetUserUseCase
import com.example.feature.user.usecase.UpdateMyProfileUseCase
import com.example.models.api.Users
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserUseCasesTest {
    private val owner = AuthenticatedRequestContext("tenant_db", "owner", 1, 10, "owner")

    @Test
    fun `create provisions central and tenant user with AuthUserId`() {
        val tenant = FakeUserRepository()
        val central = FakeAuthUserRepository()
        val result = CreateUserUseCase(tenant, central).execute(
            owner,
            CreateUserResponse(
                username = " technician ",
                email = " tech@example.com ",
                password = "password-123",
                name = "Technician",
                role = "user"
            )
        )

        assertEquals(100, result.authUserId)
        assertEquals(100, tenant.saved?.AuthUserID)
        assertEquals(null, tenant.saved?.RemoteID)
        assertEquals("technician", central.createdUsername)
    }

    @Test
    fun `user listing is restricted to owner and admin`() {
        val useCase = GetUserUseCase(FakeUserRepository(), FakeAuthUserRepository())
        assertFailsWith<IllegalArgumentException> {
            useCase.execute(owner.copy(role = "user"))
        }
    }

    @Test
    fun `user listing merges tenant profile with central account`() {
        val tenant = FakeUserRepository().apply {
            users += tenantUser(authUserId = 100)
        }
        val central = FakeAuthUserRepository().apply {
            accounts[100] = authAccount(100)
        }

        val result = GetUserUseCase(tenant, central).execute(owner).single()

        assertEquals("tenant-user", result.userId)
        assertEquals("central-user", result.username)
        assertEquals("central@example.com", result.email)
        assertEquals("active", result.status)
    }

    @Test
    fun `self profile update resolves tenant user through AuthUserId`() {
        val tenant = FakeUserRepository().apply { tenantIdByAuthId[1] = "tenant-owner" }
        val updated = UpdateMyProfileUseCase(tenant).execute(
            owner,
            UpdateMyProfileRequest(name = "New name", phone = "6900000000")
        )

        assertEquals(true, updated)
        assertEquals("tenant-owner", tenant.updatedTenantUserId)
        assertEquals("New name", tenant.profileUpdate?.name)
        assertEquals(null, tenant.profileUpdate?.email)
    }

    private fun tenantUser(authUserId: Int) = UsersResponse(
        UserID = "tenant-user",
        RemoteID = null,
        AuthUserID = authUserId,
        Name = "Tenant name",
        Description = null,
        Email = "tenant@example.com",
        Phone = null,
        ReportPrefix = null,
        TechnicalCasePrefix = null,
        LastReportNumber = null,
        LastTCNumber = null,
        LastModified = null,
        DateCreated = null,
        Version = null
    )

    private fun authAccount(id: Int) = AuthUserAccount(
        id = id,
        username = "central-user",
        email = "central@example.com",
        passwordHash = "hash",
        role = "user",
        status = "active"
    )
}

private class FakeUserRepository : UserRepository {
    var saved: Users? = null
    val users = mutableListOf<UsersResponse>()
    val tenantIdByAuthId = mutableMapOf<Int, String>()
    var updatedTenantUserId: String? = null
    var profileUpdate: UserProfileUpdate? = null

    override fun save(ctx: RequestContext, data: Users): Users = data.also { saved = it }
    override fun findAll(ctx: RequestContext): List<UsersResponse> = users
    override fun edit(ctx: RequestContext, data: EditUserResponse) = data
    override fun findAuthUserId(ctx: RequestContext, tenantUserId: String): Int? =
        users.firstOrNull { it.UserID == tenantUserId }?.AuthUserID
    override fun findTenantUserId(ctx: RequestContext, authUserId: Int): String? = tenantIdByAuthId[authUserId]
    override fun updateProfile(ctx: RequestContext, tenantUserId: String, data: UserProfileUpdate): Boolean {
        updatedTenantUserId = tenantUserId
        profileUpdate = data
        return true
    }
}

private class FakeAuthUserRepository : AuthUserProvisioningRepository {
    val accounts = mutableMapOf<Int, AuthUserAccount>()
    var createdUsername: String? = null

    override fun createActiveUser(companyId: Int, username: String, email: String, passwordHash: String, role: String): Int {
        createdUsername = username
        return 100
    }
    override fun activateUser(companyId: Int, authUserId: Int) = true
    override fun deactivateUser(companyId: Int, authUserId: Int) = true
    override fun deleteProvisionedUser(companyId: Int, authUserId: Int) = Unit
    override fun findUsers(companyId: Int, authUserIds: Collection<Int>) =
        accounts.filterKeys { it in authUserIds }
    override fun findUser(companyId: Int, authUserId: Int) = accounts[authUserId]
    override fun updateUser(companyId: Int, authUserId: Int, username: String, email: String, passwordHash: String?, role: String) = accounts[authUserId]
    override fun restoreUser(companyId: Int, account: AuthUserAccount) = Unit
    override fun updatePassword(companyId: Int, authUserId: Int, passwordHash: String) = true
}
