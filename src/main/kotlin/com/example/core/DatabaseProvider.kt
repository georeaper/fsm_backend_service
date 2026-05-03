package com.example.core

import com.example.loadDotenv
import org.jetbrains.exposed.sql.Database

class DatabaseProvider {

    fun getDatabase(dbName: String): Database {
        val dotenv = loadDotenv()

        val url = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/$dbName"

        return Database.Companion.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = dotenv["DB_USER"],
            password = dotenv["DB_PASSWORD"]
        )
    }
}