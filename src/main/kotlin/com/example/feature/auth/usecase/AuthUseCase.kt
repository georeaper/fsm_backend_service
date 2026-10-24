package com.example.feature.auth.usecase

import com.example.JwtConfigV2
import com.example.feature.auth.dto.AccessTokenResponse
import com.example.feature.auth.dto.IssuedTokenPair
import com.example.feature.auth.repository.AuthRepository
import com.example.feature.auth.repository.AuthenticatedUser
import com.example.infrastructure.security.PasswordVerifier
import com.example.infrastructure.security.RefreshTokenHasher

class AuthUseCase(
    private val repository: AuthRepository,
    private val accessTokenGenerator: (Int, Int, String) -> String = { userId, companyId, role ->
        JwtConfigV2.generateAccessToken(userId, companyId, role)
    },
    private val refreshTokenGenerator: () -> String = JwtConfigV2::generateRefreshToken
) {
    fun login(username: String, password: String): IssuedTokenPair? {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isEmpty() || normalizedUsername.length > 255 ||
            password.isEmpty() || password.length > 1024
        ) return null

        val user = repository.findActiveUser(normalizedUsername)
        val passwordMatches = PasswordVerifier.verify(password, user?.passwordHash)
        if (user == null || !passwordMatches) return null

        val refreshToken = refreshTokenGenerator()
        repository.createSession(user.id, RefreshTokenHasher.hash(refreshToken))
        return tokenPair(user, refreshToken)
    }

    fun refresh(refreshToken: String): IssuedTokenPair? {
        if (refreshToken.isBlank() || refreshToken.length > 512) return null

        val newRefreshToken = refreshTokenGenerator()
        val user = repository.rotateSession(
            RefreshTokenHasher.hash(refreshToken),
            RefreshTokenHasher.hash(newRefreshToken)
        ) ?: return null

        return tokenPair(user, newRefreshToken)
    }

    fun logout(refreshToken: String): Boolean {
        if (refreshToken.isBlank() || refreshToken.length > 512) return false
        return repository.revokeSession(RefreshTokenHasher.hash(refreshToken))
    }

    private fun tokenPair(user: AuthenticatedUser, refreshToken: String) =
        IssuedTokenPair(
            response = AccessTokenResponse(
                accessToken = accessTokenGenerator(user.id, user.companyId, user.role),
                username = user.username
            ),
            refreshToken = refreshToken
        )
}
