package com.example


import com.example.core.AppContainer
import com.example.feature.contracts.contractsRoute
import com.example.feature.customers.customersRoute
import com.example.feature.onboarding.routes.onboardingRoutes
import com.example.feature.auth.authV2Routes
import com.example.feature.subscription.subscriptionRoutes
import com.example.infrastructure.DatabaseFactory
import com.example.routes.authRoutes
import com.example.feature.equipments.equipmentsRoute
import com.example.feature.inventory.inventoryRoute
import com.example.feature.model.modelRoute
import com.example.feature.manufacturer.manufacturerRoute
import com.example.feature.fieldreport.fieldReportRoute
import com.example.feature.maintenance.maintenanceRoute
import com.example.feature.settings.settingsRoute
import com.example.feature.categories.categoriesRoute
import com.example.feature.tasks.tasksRoute
import com.example.feature.ticket.ticketsRoute
import com.example.feature.tools.toolsRoute
import com.example.feature.user.usersRoute
import com.example.feature.dashboard.dashboardRoute
import com.example.feature.notification.notificationRoutes
import com.example.feature.pdf.routes.pdfRoutes
import com.example.routes.syncRoutes
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.respond

import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File


fun main(args: Array<String>) = EngineMain.main(args)

@OptIn(DelicateCoroutinesApi::class)
fun Application.module() {


    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        if ((System.getenv("ENV") ?: "dev") == "dev") {
            allowHost("localhost:5173", schemes = listOf("http"))
            allowHost("127.0.0.1:5173", schemes = listOf("http"))
            allowHost("192.168.1.74:5173", schemes = listOf("http"))
        }
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)  // <-- Add this line!
        allowHeader("X-Custom-Header")
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)  // <-- Also add OPTIONS to handle preflight
        allowCredentials = true
    }

    install(Authentication) { // ✅ Install Authentication
        jwt("auth-jwt") {
            realm = "ktor app"
            verifier(JwtConfig.verifier) // Ensure you have a proper JWT verifier
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty())
                    JWTPrincipal(credential.payload)
                else null
            }
        }
        jwt("auth-jwt-v2") {
            realm = "fsm-api"
            verifier(JwtConfigV2.verifier)
            validate { credential ->
                val userId = credential.payload.subject?.toIntOrNull()
                val companyId = credential.payload.getClaim("tenant_id").asInt()
                val role = credential.payload.getClaim("role").asString()
                if (userId != null && companyId != null && !role.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    val container = AppContainer()
    val dotenv = loadDotenv()

    DatabaseFactory.init(environment.config)

    val dbInitializationJob = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    ).launch {
        while (isActive) {
            if (DatabaseFactory.ensureTables()) {
                println("Central Database schema ready")
                break
            }

            println("Central Database unavailable. Retrying in 5 seconds...")
            delay(5_000)
        }
    }

    val notificationSchedulerJob = container.notificationScheduler.start(
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )

    environment.monitor.subscribe(ApplicationStopped) {
        dbInitializationJob.cancel()
        notificationSchedulerJob.cancel()
    }


    routing {

//        GlobalScope.launch {
//            while (true) {
//                if (!DatabaseFactory.isConnected) {
//                    println("Trying to reconnect DB...")
//                    DatabaseFactory.init(dotenv)
//                }
//                delay(5000)
//            }
//        }

        route("/") {
            static("/") {
           resources("static")
            defaultResource("static/index.html")
        }
//            intercept(ApplicationCallPipeline.Call) {
//                if (!DatabaseFactory.isConnected) {
//                    call.respond(
//                        HttpStatusCode.ServiceUnavailable,
//                        mapOf("error" to "Database not connected")
//                    )
//                    finish()
//                }
//            }
            get("/health/ready") {
                val available = DatabaseFactory.isAvailable()

                if (available) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("database" to "UP")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        mapOf("database" to "DOWN")
                    )
                }
            }

            // Όλα τα routes σου εδώ
            customersRoute(
                container.getCustomersUseCase ,
                container.createCustomerUseCase,
                container.updateCustomerUseCase,
                container.deleteCustomerUseCase,
                container.authenticatedRequestContextResolver)
            equipmentsRoute(
                container.getEquipmentUseCase ,
                container.createEquipmentUseCase,
                container.updateEquipmentUseCase,
                container.deleteEquipmentUseCase,
                container.authenticatedRequestContextResolver)
            inventoryRoute(
                container.getInventoryUseCase,
                container.createInventoryUseCase,
                container.updateInventoryUseCase,
                container.deleteInventoryUseCase,
                container.authenticatedRequestContextResolver)
            modelRoute(
                container.getModelUseCase ,
                container.createModelUseCase,
            container.deleteModelUseCase,
                container.authenticatedRequestContextResolver)
            manufacturerRoute(
                container.getManufacturerUseCase ,
                container.createManufacturerUseCase,
                container.deleteManufacturerUseCase,
                container.authenticatedRequestContextResolver)
            fieldReportRoute(
                container.fieldReportApiRepository,
                container.authenticatedRequestContextResolver,
                container.notificationCoordinator)
            pdfRoutes(container.generateFieldReportPdfUseCase, container.authenticatedRequestContextResolver)
            maintenanceRoute(
                container.getMaintenanceUseCase,
                container.createMaintenanceUseCase,
                container.updateMaintenanceUseCase,
                container.deleteMaintenanceUseCase,
                container.authenticatedRequestContextResolver)
            settingsRoute(
                container.getSettingsUseCase,
                container.createSettingsUseCase,
                container.getTechnicalCasePriorityUseCase,
                container.createTechnicalCasePriorityUseCase,
                container.getWorkOrderTypeUseCase,
                container.createWorkOrderTypeUseCase,
                container.getContractTypeUseCase,
                container.createContractTypeUseCase,
                container.categorizedSettingsUseCase,
                container.deleteSettingsUseCase,
                container.updateSettingStyleUseCase,
                container.updateSystemCategoryUseCase,
                container.getCategoryUseCase,
                container.createCategoryUseCase,
                container.authenticatedRequestContextResolver)
            categoriesRoute(
                container.getCategoryUseCase,
                container.createCategoryUseCase,
                container.deleteCategoryUseCase,
                container.authenticatedRequestContextResolver)
            tasksRoute(
                container.getTaskUseCase,
                container.createTaskUseCase,
                container.updateTaskUseCase,
                container.deleteTaskUseCase,
                container.authenticatedRequestContextResolver,
                container.notificationCoordinator)
            ticketsRoute(
                container.getTicketUseCase,
                container.createTicketUseCase,
                container.updateTicketUseCase,
                container.deleteTicketUseCase,
                container.authenticatedRequestContextResolver,
                container.notificationCoordinator)
            toolsRoute(
                container.getToolUseCase,
                container.createToolUseCase,
                container.updateToolUseCase,
                container.deleteToolUseCase,
                container.authenticatedRequestContextResolver)
            usersRoute(
                container.getUserUseCase,
                container.createUserUseCase,
                container.changeUserStatusUseCase,
                container.updateUserUseCase,
                container.getMyProfileUseCase,
                container.updateMyProfileUseCase,
                container.changeMyPasswordUseCase,
                container.authenticatedRequestContextResolver)
            contractsRoute(
                container.getContractUseCase,
                container.createContractUseCase,
                container.updateContractUseCase,
                container.deleteContractUseCase,
                container.authenticatedRequestContextResolver,
                container.notificationCoordinator)
            dashboardRoute(
                container.dashboardUseCase,
                container.authenticatedRequestContextResolver
            )
            notificationRoutes(
                container.notificationUseCases,
                container.authenticatedRequestContextResolver
            )
            categoriesRoute(
                container.getCategoryUseCase,
                container.createCategoryUseCase,
                container.deleteCategoryUseCase,
                container.authenticatedRequestContextResolver)

            // Legacy /auth/login was used only by Android. It is disabled in favor of
            // /auth/v2/mobile/login, which uses the central session and subscription flow.
            // authRoutes()
            authV2Routes(container.authUseCase)
            subscriptionRoutes(
                container.subscriptionRepository,
                container.authenticatedRequestContextResolver
            )
            onboardingRoutes()
//            dbCreation()

            syncRoutes()
        }
    }
}


fun loadDotenv(): io.github.cdimascio.dotenv.Dotenv {
    val env = System.getenv("ENV") ?: "dev"

    val fileName = when (env) {
        "dev" -> "env.dev"
        "prod" -> "env.prod"
        else -> error("Invalid ENV value: $env (use dev or prod)")
    }

    val file = File("assets/$fileName")
    require(file.exists()) {
        "Missing environment file: assets/$fileName"
    }

    println("Running in ENV=$env")

    return dotenv {
        directory = "./assets"
        filename = fileName
    }


}
