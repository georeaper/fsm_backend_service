package com.example.infrastructure.security

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginAttemptLimiterTest {
    @Test
    fun `blocks after maximum failures and reset allows requests`() {
        val limiter = LoginAttemptLimiter(maximumAttempts = 2)

        assertTrue(limiter.isAllowed("client:user"))
        limiter.recordFailure("client:user")
        assertTrue(limiter.isAllowed("client:user"))
        limiter.recordFailure("client:user")
        assertFalse(limiter.isAllowed("client:user"))

        limiter.reset("client:user")
        assertTrue(limiter.isAllowed("client:user"))
    }

    @Test
    fun `allows requests after window expires`() {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val clock = MutableClock(start)
        val limiter = LoginAttemptLimiter(
            maximumAttempts = 1,
            window = Duration.ofMinutes(15),
            clock = clock
        )

        limiter.recordFailure("client:user")
        assertFalse(limiter.isAllowed("client:user"))

        clock.current = start.plus(Duration.ofMinutes(16))
        assertTrue(limiter.isAllowed("client:user"))
    }

    private class MutableClock(var current: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = current
    }
}
