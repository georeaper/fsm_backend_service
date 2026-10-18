package com.example.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecurityUtilitiesTest {
    @Test
    fun `refresh token hashing is deterministic and does not expose token`() {
        val token = "a-sensitive-refresh-token"
        val hash = RefreshTokenHasher.hash(token)

        assertEquals(64, hash.length)
        assertEquals(hash, RefreshTokenHasher.hash(token))
        assertFalse(hash.contains(token))
    }

    @Test
    fun `password verifier accepts valid bcrypt password`() {
        val hash = BCrypt.withDefaults().hashToString(12, "correct-password".toCharArray())

        assertTrue(PasswordVerifier.verify("correct-password", hash))
        assertFalse(PasswordVerifier.verify("wrong-password", hash))
    }

    @Test
    fun `password verifier safely rejects malformed hash`() {
        assertFalse(PasswordVerifier.verify("password", "not-a-bcrypt-hash"))
        assertFalse(PasswordVerifier.verify("password", null))
    }
}
