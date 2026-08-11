package com.example.feature.onboarding.repository

import com.example.feature.onboarding.dto.RegisterTenantDto
import com.example.models.authenticationModels.CompanyTable
import com.example.models.authenticationModels.UserTable
import com.example.utils.createDatabaseIfNotExists
import com.example.loadDotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.models.databaseModels.categoryAssetTable
import com.example.models.databaseModels.checkFormsTable
import com.example.models.databaseModels.contractEquipmentsTable
import com.example.models.databaseModels.contractsTable
import com.example.models.databaseModels.customerTable
import com.example.models.databaseModels.equipmentTable
import com.example.models.databaseModels.fieldReportCheckformTable
import com.example.models.databaseModels.fieldReportEquipmentTable
import com.example.models.databaseModels.fieldReportInventoryTable
import com.example.models.databaseModels.fieldReportToolsTable
import com.example.models.databaseModels.fieldReportsTable
import com.example.models.databaseModels.inventoryTable
import com.example.models.databaseModels.maintenancesTable
import com.example.models.databaseModels.manufacturerTable
import com.example.models.databaseModels.modelAssetTable
import com.example.models.databaseModels.notificationsTable
import com.example.models.databaseModels.settingsTable
import com.example.models.databaseModels.tasksTable
import com.example.models.databaseModels.ticketHistoryTable
import com.example.models.databaseModels.ticketTable
import com.example.models.databaseModels.toolsTable
import com.example.models.databaseModels.userTable

class OnboardingRepositoryImpl : OnBoardingRepository {

    override fun createTenantDB(data: RegisterTenantDto): Int {
        // load env (host/port/user/password for DB server and central DB name)
        val dotenv = loadDotenv()

        val centralUrl = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DB_NAME"]}"
        Database.connect(
            url = centralUrl,
            driver = "org.postgresql.Driver",
            user = dotenv["DB_USER"],
            password = dotenv["DB_PASSWORD"]
        )

        // derive DB name
        val derivedDbName = data.name.trim().lowercase().replace("\\s+".toRegex(), "_") + "_db"

        return transaction {
            // basic validation: company name / contact email / username uniqueness
            val existingCompany = CompanyTable.select { CompanyTable.name eq data.name }.firstOrNull()
            if (existingCompany != null) {
                throw IllegalArgumentException("Company with this name already exists")
            }

            val existingUser = UserTable.select { UserTable.username eq data.adminUsername }.firstOrNull()
            if (existingUser != null) {
                throw IllegalArgumentException("Username already exists")
            }

            // insert company
            val insertedCompanyId = CompanyTable.insert {
                it[name] = data.name
                it[contactEmail] = data.contactEmail
                it[databaseName] = derivedDbName
                it[databaseHost] = dotenv["DB_HOST"] ?: "localhost"
                it[databasePort] = dotenv["DB_PORT"]?.toInt() ?: 5432
                it[databaseUser] = dotenv["DB_USER"] ?: "postgres"
                it[databasePassword] = dotenv["DB_PASSWORD"] ?: ""
            }[CompanyTable.id]

            // hash password and insert master user
            val hashed = BCrypt.withDefaults().hashToString(12, data.adminPassword.toCharArray())

            UserTable.insert {
                it[username] = data.adminUsername
                it[email] = data.adminEmail
                it[password] = hashed
                it[companyId] = insertedCompanyId
                it[role] = "owner"
            }

            insertedCompanyId
        }
            .also { companyId ->
                // create tenant DB and schema outside the master transaction
                val dbHost = dotenv["DB_HOST"] ?: "localhost"
                val dbPort = dotenv["DB_PORT"]?.toInt() ?: 5432
                val dbUser = dotenv["DB_USER"] ?: "postgres"
                val dbPassword = dotenv["DB_PASSWORD"] ?: ""

                // create database if not exists
                createDatabaseIfNotExists(derivedDbName, dbHost, dbPort, dbUser, dbPassword)

                // connect to tenant DB and create schema (tables)
                val tenantJdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$derivedDbName"
                val tenantDb = Database.connect(
                    url = tenantJdbcUrl,
                    driver = "org.postgresql.Driver",
                    user = dbUser,
                    password = dbPassword
                )

                transaction(tenantDb) {
                    // create tables — these are tenant models; schema creation is centralized here
                    SchemaUtils.createMissingTablesAndColumns(
                        userTable,
                        customerTable,
                        equipmentTable,
                        ticketHistoryTable,
                        categoryAssetTable,
                        checkFormsTable,
                        contractEquipmentsTable,
                        contractsTable,
                        fieldReportCheckformTable,
                        fieldReportToolsTable,
                        fieldReportsTable,
                        fieldReportInventoryTable,
                        fieldReportEquipmentTable,
                        inventoryTable,
                        manufacturerTable,
                        maintenancesTable,
                        modelAssetTable,
                        settingsTable,
                        tasksTable,
                        ticketTable,
                        toolsTable,
                        notificationsTable
                    )

                    // create admin user in tenant DB
                    //val hashedTenant = BCrypt.withDefaults().hashToString(12, data.adminPassword.toCharArray())
                    userTable.insert {

                        it[name] = data.name
                        it[email] = data.adminEmail
                        it[description]=data.adminUsername

                    }

                }
            }

    }


}