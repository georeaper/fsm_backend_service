package com.example.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.sql.Connection
import java.sql.DriverManager

class DatabaseConnectionManager(private val secretKey: String) {

    fun getConnectionFromJwt(token: String): Connection? {
        return try {
            // Decode the JWT token
            val decodedJwt = decodeJwt(token)

            // Extract database credentials
            val dbName = decodedJwt.getClaim("databaseName").asString()
            val dbHost = decodedJwt.getClaim("databaseHost").asString()
            val dbPort = decodedJwt.getClaim("databasePort").asInt()
            val dbUser = decodedJwt.getClaim("databaseUser").asString()
            val dbPassword = decodedJwt.getClaim("databasePassword").asString()

            // Construct JDBC URL
            val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

            // Connect to database
            DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)
        } catch (e: Exception) {
            println("Database Connection Error: ${e.message}")
            null
        }
    }

    private fun decodeJwt(token: String): DecodedJWT {
        val algorithm = Algorithm.HMAC256(secretKey)
        return JWT.require(algorithm).build().verify(token)
    }
}