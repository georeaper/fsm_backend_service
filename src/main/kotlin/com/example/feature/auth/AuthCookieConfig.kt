package com.example.feature.auth

data class AuthCookieConfig(
    val secure: Boolean,
    val sameSite: String = "Strict"
) {
    companion object {
        fun fromEnvironment(): AuthCookieConfig = AuthCookieConfig(
            secure = System.getenv("ENV")?.equals("prod", ignoreCase = true) == true
        )
    }
}
