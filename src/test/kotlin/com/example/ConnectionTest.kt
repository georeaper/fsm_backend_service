package com.example

 import com.example.services.api.LoginRequest
 import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
 import io.ktor.server.config.*
 import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionTest {

    @Test
    fun `test login with dynamic database connection`() = testApplication {

        environment {
            config = ApplicationConfig("application.conf") // Explicitly load config
        }

        application {
            module()
        }

        // Simulate a login request
        val loginRequest = LoginRequest(username = "postgres", password = "Giorgos13", company = "CompanyA")
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        println("Response: ${response.bodyAsText()}")
    }

}