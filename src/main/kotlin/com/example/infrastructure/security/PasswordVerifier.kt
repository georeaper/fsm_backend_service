package com.example.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordVerifier {
    private val dummyHash by lazy {
        BCrypt.withDefaults().hashToString(12, "dummy-password".toCharArray())
    }

    fun verify(password: String, passwordHash: String?): Boolean =
        try {
            BCrypt.verifyer()
                .verify(password.toCharArray(), (passwordHash ?: dummyHash).toCharArray())
                .verified
        } catch (_: IllegalArgumentException) {
            false
        }
}
