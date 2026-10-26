package com.example.core

import com.example.loadDotenv
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ConcurrentHashMap

class DatabaseProvider {
    private val databases = ConcurrentHashMap<String, Database>()

    fun getDatabase(dbName: String): Database {
        require(dbName.matches(Regex("[A-Za-z0-9_]+"))) { "Invalid tenant database name" }
        return databases.computeIfAbsent(dbName) {
            val dotenv = loadDotenv()
            val url = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/$dbName"
            Database.connect(url = url, driver = "org.postgresql.Driver", user = dotenv["DB_USER"], password = dotenv["DB_PASSWORD"])
        }
    }
}
