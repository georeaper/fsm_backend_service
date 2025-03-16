package com.example.repositories


import com.example.models.authenticationModels.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class User(val id: Int, val username: String, val password: String)


object UserDAO {
    fun getUserByUsername(username: String): User? {
        return transaction {
            UserTable.select { UserTable.username eq username }
                .map { User(it[UserTable.id], it[UserTable.username], it[UserTable.password]) }
                .singleOrNull()
        }
    }

    fun addUser(username: String, password: String): User? {
        return transaction {
            val id = UserTable.insert {
                it[UserTable.username] = username
                it[UserTable.password] = password
            }[UserTable.id] // Retrieve the ID manually

            User(id, username, password)
        }
    }
}