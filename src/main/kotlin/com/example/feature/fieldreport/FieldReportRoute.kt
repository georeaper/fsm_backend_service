package com.example.feature.fieldreport

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.CreateFieldReportResponse
import com.example.feature.fieldreport.dto.EditFieldReportResponse
import com.example.feature.fieldreport.usecase.UpdateFieldReportUseCase
import com.example.feature.fieldreport.usecase.DeleteFieldReportUseCase
import com.example.feature.fieldreport.usecase.CreateFieldReportUseCase
import com.example.feature.fieldreport.usecase.GetFieldReportUseCase
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

fun Route.fieldReportRoute(
    getFieldReportUseCase: GetFieldReportUseCase ,
    createFieldReportUseCase: CreateFieldReportUseCase,
    updateFieldReportUseCase: UpdateFieldReportUseCase,
    deleteFieldReportUseCase: DeleteFieldReportUseCase
){
    authenticate("auth-jwt") {
        get("/fieldreports"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            val result=getFieldReportUseCase.execute(ctx)
            println("Field Report Result: $result")
            call.respond(result)
        }
        post("/fieldreports"){
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = RequestContext(
                username = principal.getClaim("username", String::class),
                dbName = dbName
            )
            val request=call.receive<CreateFieldReportResponse>()
            val result=createFieldReportUseCase.execute(ctx,request)
            call.respond(HttpStatusCode.Created, result)
        }
        put("/fieldreports/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            val request = call.receive<EditFieldReportResponse>().copy(FieldReportID = id)
            call.respond(HttpStatusCode.OK, updateFieldReportUseCase.execute(ctx, request))
        }

        delete("/fieldreports/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val ctx = RequestContext(dbName, principal.getClaim("username", String::class))
            if (deleteFieldReportUseCase.execute(ctx, id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
