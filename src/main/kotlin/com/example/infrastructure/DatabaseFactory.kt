package com.example.infrastructure

import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    var isConnected = false
        private set

    fun init(dotenv: io.github.cdimascio.dotenv.Dotenv) {
        try {
            val url = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DB_NAME"]}"

            Database.connect(
                url = url,
                driver = "org.postgresql.Driver",
                user = dotenv["DB_USER"],
                password = dotenv["DB_PASSWORD"]
            )

            transaction {
                SchemaUtils.createMissingTablesAndColumns(CompanyTable, UserTable)
            }

            isConnected = true
            println("✅ Database connected")

        } catch (e: Exception) {
            isConnected = false
            println("❌ Database NOT connected: ${e.message}")
        }
    }
}