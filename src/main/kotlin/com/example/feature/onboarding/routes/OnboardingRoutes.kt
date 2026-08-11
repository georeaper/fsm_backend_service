package com.example.feature.onboarding.routes

import com.example.feature.onboarding.dto.RegisterUserDto
import com.example.feature.onboarding.dto.RegisterTenantDto
import com.example.feature.onboarding.repository.OnBoardingRepository
import com.example.feature.onboarding.repository.OnboardingRepositoryImpl
import com.example.feature.onboarding.repository.PopulateDemoRepository
import com.example.feature.onboarding.repository.PopulateDemoRepositoryImpl
import com.example.feature.onboarding.usecase.PopulateDemoUseCase
import com.example.feature.onboarding.usecase.RegisterTenantUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Onboarding HTTP routes. Keep route thin: delegate business logic to UseCase/Repository.
 * To enable, call `onboardingRoutes()` from your application's routing block.
 */
fun Route.onboardingRoutes(repository: OnBoardingRepository = OnboardingRepositoryImpl(),demoRepository: PopulateDemoRepository = PopulateDemoRepositoryImpl()) {

    val useCase = RegisterTenantUseCase(repository)

    val populateDemoUseCase = PopulateDemoUseCase(demoRepository)


    route("/onboarding") {
        post("/register") {
            try {
                val req = call.receive<RegisterUserDto>()



                val result = useCase.execute(
                    RegisterTenantDto(
                        name = req.companyName,
                        contactEmail = req.companyEmail,
                        adminUsername = req.adminUsername,
                        adminEmail = req.adminEmail,
                        adminPassword = req.adminPassword
                    )
                )
                print("*/////////////////////////*")
                print("*/////////////////////////*")
                print("*/////////////////////////*")
                println("Token : ${result.token}")
                print("*/////////////////////////*")
                print("*/////////////////////////*")
                print("*/////////////////////////*")
                // respond with a serializable DTO instead of a heterogeneous Map
                call.respond(HttpStatusCode.Created, result)
                if (req.populateDemo){
                    populateDemoUseCase.execute(result.databaseName)
                }
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

