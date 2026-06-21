package com.example.feature.onboarding.routes

import com.example.dto.RegisterUserDto
import com.example.feature.onboarding.repository.OnBoardingRepository
import com.example.feature.onboarding.repository.OnboardingRepositoryImpl
import com.example.feature.onboarding.usecase.RegisterTenantUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Onboarding HTTP routes. Keep route thin: delegate business logic to UseCase/Repository.
 * To enable, call `onboardingRoutes()` from your application's routing block.
 */
fun Route.onboardingRoutes(repository: OnBoardingRepository = OnboardingRepositoryImpl()) {
    val usecase = RegisterTenantUseCase(repository)

    route("/onboarding") {
        post("/register") {
            try {
                val req = call.receive<RegisterUserDto>()

                val result = usecase.execute(com.example.feature.onboarding.dto.RegisterTenantDto(
                    name = req.companyName,
                    contactEmail = req.companyEmail,
                    adminUsername = req.adminUsername,
                    adminEmail = req.adminEmail,
                    adminPassword = req.adminPassword
                ))

                // respond with a serializable DTO instead of a heterogeneous Map
                call.respond(HttpStatusCode.Created, result)
                println(result.toString())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "conflict")))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "error")))
            }
        }
    }
}

