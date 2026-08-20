package com.example.feature.categories.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.categories.dto.CategoryResponse
import com.example.models.api.Categories
import com.example.models.databaseModels.CategoriesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepositoryImpl(private val dbProvider: DatabaseProvider) : CategoryRepository {
    override fun save(ctx: RequestContext, data: Categories): Categories {
        val db = dbProvider.getDatabase(ctx.dbName)
        transaction(db) {
            CategoriesTable.insert {
                it[categoryId] = data.CategoryID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Description
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        return data
    }

    override fun findAll(ctx: RequestContext): List<CategoryResponse> {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            CategoriesTable.selectAll().map {
                CategoryResponse(
                    CategoryID = it[CategoriesTable.categoryId],
                    RemoteID = it[CategoriesTable.remoteId],
                    Name = it[CategoriesTable.name],
                    Style = it[CategoriesTable.style],
                    LastModified = it[CategoriesTable.lastModified],
                    DateCreated = it[CategoriesTable.dateCreated],
                    Version = it[CategoriesTable.version]
                )
            }
        }
    }

    override fun delete(ctx: RequestContext, id: String): Boolean {
        val db = dbProvider.getDatabase(ctx.dbName)
        return transaction(db) {
            val deleted = CategoriesTable.deleteWhere { CategoriesTable.categoryId eq id }
            deleted > 0
        }
    }
}
