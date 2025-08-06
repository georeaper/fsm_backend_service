package com.example.utils

import java.sql.DriverManager

fun createDatabaseIfNotExists(
dbName: String,
host: String = "localhost",
port: Int = 5432,
user: String = "postgres",
password: String = "Giorgos13"
) {
    val jdbcUrl = "jdbc:postgresql://$host:$port/postgres" // connect to default DB

    DriverManager.getConnection(jdbcUrl, user, password).use { conn ->
        conn.createStatement().use { stmt ->
            // Check if DB exists
            val rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '$dbName'")
            if (!rs.next()) {
                // Create DB if it does not exist
                stmt.executeUpdate("CREATE DATABASE \"$dbName\"")
                println("Database $dbName created")
            } else {
                println("Database $dbName already exists")
            }
        }
    }
}