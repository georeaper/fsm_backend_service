package com.example.feature.settings

import com.example.feature.auth.service.AuthenticatedRequestContextResolver
import com.example.feature.auth.service.authenticatedRequestContext
import com.example.feature.settings.dto.CreateSettingsResponse
import com.example.feature.settings.usecase.CreateContractTypeUseCase
import com.example.feature.settings.usecase.CreateSettingsUseCase
import com.example.feature.settings.usecase.CreateTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.CreateWorkOrderTypeUseCase
import com.example.feature.settings.usecase.GetContractTypeUseCase
import com.example.feature.settings.usecase.GetSettingsUseCase
import com.example.feature.settings.usecase.GetTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.GetWorkOrderTypeUseCase
import com.example.feature.categories.usecase.GetCategoryUseCase
import com.example.feature.categories.usecase.CreateCategoryUseCase
import com.example.feature.categories.dto.CreateCategoryResponse
import com.example.feature.settings.usecase.DeleteSettingsUseCase
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
    createContractTypeUseCase: CreateContractTypeUseCase,
    deleteSettingsUseCase: DeleteSettingsUseCase,
    getCategoryUseCase: GetCategoryUseCase,
    createCategoryUseCase: CreateCategoryUseCase,
    contextResolver: AuthenticatedRequestContextResolver
){

    authenticate("auth-jwt-v2") {
        get("/settings"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val result=getSettingsUseCase.execute(ctx)
            call.respond(result)
        }
        post("/settings"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateSettingsResponse>()
            val result=createSettingsUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/technical-case-priorities") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val result = getTechnicalCasePriorityUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/technical-case-priorities"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateSettingsResponse>()
            val result=createTechnicalCasePriorityUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/work-order-types") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val result = getWorkOrderTypeUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/work-order-types"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateSettingsResponse>()
            val result=createWorkOrderTypeUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        get("/settings/contract-types") {
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val result = getContractTypeUseCase.execute(ctx)

            call.respond(result)
        }
        post("/settings/contract-types"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request=call.receive<CreateSettingsResponse>()
            val result=createContractTypeUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
    delete("/settings/delete/{id}"){
            val ctx = call.authenticatedRequestContext(contextResolver)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = deleteSettingsUseCase.execute(ctx, id)
            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}

