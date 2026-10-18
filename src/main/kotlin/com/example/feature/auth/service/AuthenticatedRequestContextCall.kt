package com.example.feature.auth.service

import com.example.core.RequestContext
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.authenticatedRequestContext(
    resolver: AuthenticatedRequestContextResolver
): RequestContext? {
    val principal = principal<JWTPrincipal>() ?: return null
    return resolver.resolve(principal)?.toRequestContext()
}
