package com.example.feature.settings

import com.example.core.RequestContext
import com.example.feature.settings.dto.CreateSettingsResponse
import com.example.feature.settings.usecase.CreateContractTypeUseCase
import com.example.feature.settings.usecase.CreateSettingsUseCase
import com.example.feature.settings.usecase.CreateTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.CreateWorkOrderTypeUseCase
import com.example.feature.settings.usecase.GetContractTypeUseCase
import com.example.feature.settings.usecase.GetSettingsUseCase
import com.example.feature.settings.usecase.GetTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.GetWorkOrderTypeUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive
/**Σε περίπτωση που ο constructor μεγαλωσει πολυ,να γινει η λογικη καπως ετσι
 * data class SettingsUseCases(
 *     val getSettings: GetSettingsUseCase,
 *     val createSettings: CreateSettingsUseCase,
 *     val getTechnicalCasePriority: GetTechnicalCasePriorityUseCase,
 *     val updateTechnicalCasePriority: UpdateTechnicalCasePriorityUseCase
 * )
 * fun Route.settingsRoute(
 *     useCases: SettingsUseCases
 * )
 *
 */
fun Route.settingsRoute(
    getSettingsUseCase: GetSettingsUseCase,
    createSettingsUseCase: CreateSettingsUseCase,
    getTechnicalCasePriorityUseCase: GetTechnicalCasePriorityUseCase,
    createTechnicalCasePriorityUseCase: CreateTechnicalCasePriorityUseCase,
    getWorkOrderTypeUseCase: GetWorkOrderTypeUseCase,
    createWorkOrderTypeUseCase: CreateWorkOrderTypeUseCase,
    getContractTypeUseCase: GetContractTypeUseCase,
    createContractTypeUseCase: CreateContractTypeUseCase
){

    authenticate("auth-jwt") {
        get("/settings"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getSettingsUseCase.execute(ctx)
            call.respond(result)
        }
        post("/settings"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateSettingsResponse>()
            val result=createSettingsUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/technical-case-priorities") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )

            val result = getTechnicalCasePriorityUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/technical-case-priorities"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateSettingsResponse>()
            val result=createTechnicalCasePriorityUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/work-order-types") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )

            val result = getWorkOrderTypeUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/work-order-types"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateSettingsResponse>()
            val result=createWorkOrderTypeUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/contract-types") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )

            val result = getContractTypeUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/contract-types"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateSettingsResponse>()
            val result=createContractTypeUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
