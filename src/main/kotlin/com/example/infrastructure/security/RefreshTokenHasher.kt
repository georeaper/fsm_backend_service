package com.example.infrastructure.security

import java.security.MessageDigest

object RefreshTokenHasher {
    fun hash(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}
