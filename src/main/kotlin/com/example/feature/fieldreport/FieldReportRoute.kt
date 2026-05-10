package com.example.feature.fieldreport

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.CreateFieldReportResponse
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
    createFieldReportUseCase: CreateFieldReportUseCase
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
    }
}
