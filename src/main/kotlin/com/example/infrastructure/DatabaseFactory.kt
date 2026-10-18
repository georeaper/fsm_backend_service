package com.example.infrastructure

import com.example.models.authenticationModels.AuthSessionTable
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.models.authenticationModels.PermissionTable
import com.example.models.authenticationModels.RolePermissionTable
import com.example.models.authenticationModels.RoleTable
import com.example.models.authenticationModels.UserRoleTable
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction



//object DatabaseFactory {
//
//    lateinit var centralDb: Database
//        private set
//
//    var isConnected = false
//        private set
//
//    fun init(config: ApplicationConfig) {
//
//        val dbConfig = config.config("ktor.database.centralDatabase")
//
//        try {
//            centralDb = Database.connect(
//                url = dbConfig.property("url").getString(),
//                driver = dbConfig.property("driver").getString(),
//                user = dbConfig.property("user").getString(),
//                password = dbConfig.property("password").getString()
//            )
//
//            transaction(centralDb) {
//                SchemaUtils.createMissingTablesAndColumns(
//                    CompanyTable,
//                    UserTable,
//                    AuthSessionTable
//                )
//            }
//
//            isConnected = true
//            println("✅ Database connected")
//
//        } catch (e: Exception) {
//            isConnected = false
//            throw IllegalStateException(
//                "Failed to initialize Central Database",
//                e
//            )
//        }
//    }
//}

object DatabaseFactory {

    lateinit var centralDb: Database
        private set

    var isConnected = false
        private set

    fun init(config: ApplicationConfig) {

        val dbConfig = config.config("ktor.database.centralDatabase")

        centralDb = Database.connect(
            url = dbConfig.property("url").getString(),
            driver = dbConfig.property("driver").getString(),
            user = dbConfig.property("user").getString(),
            password = dbConfig.property("password").getString()
        )

        println("Central Database configured")
    }

    fun isAvailable(): Boolean {

        if (!::centralDb.isInitialized) {
            return false
        }

        return try {
            transaction(centralDb) {
                exec("SELECT 1") { result ->
                    result.next() && result.getInt(1) == 1
                } ?: false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun ensureTables(): Boolean {

        if (!::centralDb.isInitialized) {
            return false
        }

        return try {
            transaction(centralDb) {
                SchemaUtils.createMissingTablesAndColumns(
                    CompanyTable,
                    UserTable,
                    AuthSessionTable,
                    RoleTable,
                    PermissionTable,
                    RolePermissionTable,
                    UserRoleTable
                )
            }

            println("Central Database tables verified")
            true

        } catch (e: Exception) {
            System.err.println(
                "Central Database tables initialization failed: ${e.message}"
            )
            false
        }
    }


}
