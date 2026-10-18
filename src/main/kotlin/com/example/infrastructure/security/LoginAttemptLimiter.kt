package com.example.infrastructure.security

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class LoginAttemptLimiter(
    private val maximumAttempts: Int = 5,
    private val window: Duration = Duration.ofMinutes(15),
    private val clock: Clock = Clock.systemUTC()
) {
    private data class Attempts(val count: Int, val windowStartedAt: Instant)

    private val attempts = ConcurrentHashMap<String, Attempts>()

    fun isAllowed(key: String): Boolean {
        val current = attempts[key] ?: return true
        if (isExpired(current)) {
            attempts.remove(key, current)
            return true
        }
        return current.count < maximumAttempts
    }

    fun recordFailure(key: String) {
        val now = clock.instant()
        attempts.compute(key) { _, current ->
            if (current == null || isExpired(current, now)) {
                Attempts(1, now)
            } else {
                current.copy(count = current.count + 1)
            }
        }
    }

    fun reset(key: String) {
        attempts.remove(key)
    }

    private fun isExpired(attempts: Attempts, now: Instant = clock.instant()): Boolean =
        !now.isBefore(attempts.windowStartedAt.plus(window))
}
