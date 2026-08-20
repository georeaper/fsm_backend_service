package com.example


import com.example.core.AppContainer
import com.example.feature.contracts.contractsRoute
import com.example.feature.customers.customersRoute
import com.example.feature.onboarding.routes.onboardingRoutes
import com.example.infrastructure.DatabaseFactory
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.routes.authRoutes
import com.example.routes.customersRoute
import com.example.routes.dbCreation
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
import com.example.routes.protectedRoutes
import com.example.routes.seedCompanyARoute
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File


fun main(args: Array<String>) = EngineMain.main(args)

@OptIn(DelicateCoroutinesApi::class)
fun Application.module() {


    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
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
    }
    val container = AppContainer()
    val dotenv = loadDotenv()

    //configureRouting()
    routing {

        GlobalScope.launch {
            while (true) {
                if (!DatabaseFactory.isConnected) {
                    println("Trying to reconnect DB...")
                    DatabaseFactory.init(dotenv)
                }
                delay(5000)
            }
        }

        route("/") {
            static("/") {
           resources("static")
            defaultResource("static/index.html")
        }
            intercept(ApplicationCallPipeline.Call) {
                if (!DatabaseFactory.isConnected) {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        mapOf("error" to "Database not connected")
                    )
                    finish()
                }
            }

            // Όλα τα routes σου εδώ
            customersRoute(container.getCustomersUseCase ,
                container.createCustomerUseCase,
                container.updateCustomerUseCase,
                container.deleteCustomerUseCase)
            equipmentsRoute(container.getEquipmentUseCase ,
                container.createEquipmentUseCase,
                container.updateEquipmentUseCase,
                container.deleteEquipmentUseCase)
            inventoryRoute(container.getInventoryUseCase,
                container.createInventoryUseCase,
                container.updateInventoryUseCase,
                container.deleteInventoryUseCase)
            modelRoute(container.getModelUseCase ,
                container.createModelUseCase,
            container.deleteModelUseCase)
            manufacturerRoute(container.getManufacturerUseCase ,
                container.createManufacturerUseCase,
                container.deleteManufacturerUseCase)
            fieldReportRoute(container.getFieldReportUseCase,
                container.createFieldReportUseCase,
                container.updateFieldReportUseCase,
                container.deleteFieldReportUseCase)
            maintenanceRoute(container.getMaintenanceUseCase,
                container.createMaintenanceUseCase,
                container.updateMaintenanceUseCase,
                container.deleteMaintenanceUseCase)
            settingsRoute(
                container.getSettingsUseCase,
                container.createSettingsUseCase,
                container.getTechnicalCasePriorityUseCase,
                container.createTechnicalCasePriorityUseCase,
                container.getWorkOrderTypeUseCase,
                container.createWorkOrderTypeUseCase,
                container.getContractTypeUseCase,
                container.createContractTypeUseCase,
                container.deleteSettingsUseCase,
                container.getCategoryUseCase,
                container.createCategoryUseCase)
            categoriesRoute(container.getCategoryUseCase,container.createCategoryUseCase,container.deleteCategoryUseCase)
            tasksRoute(container.getTaskUseCase,
                container.createTaskUseCase,
                container.updateTaskUseCase,
                container.deleteTaskUseCase)
            ticketsRoute(container.getTicketUseCase,
                container.createTicketUseCase,
                container.updateTicketUseCase,
                container.deleteTicketUseCase)
            toolsRoute(container.getToolUseCase,
                container.createToolUseCase,
                container.updateToolUseCase,
                container.deleteToolUseCase)
            usersRoute(container.getUserUseCase,container.createUserUseCase)
            contractsRoute(container.getContractUseCase,
                container.createContractUseCase,
                container.updateContractUseCase,
                container.deleteContractUseCase)
            categoriesRoute(container.getCategoryUseCase,container.createCategoryUseCase,container.deleteCategoryUseCase)
            seedCompanyARoute()
            authRoutes()
            onboardingRoutes()
            dbCreation()
            authenticate("auth-jwt") {  // ✅ Wrap protectedRoutes inside authenticate
                protectedRoutes()
            }
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
