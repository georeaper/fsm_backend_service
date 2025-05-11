package com.example.routes

import com.example.JwtConfig
import com.example.json
import com.example.models.api.*
import com.example.models.authenticationModels.UserTable
import com.example.models.databaseModels.customerTable
import com.example.models.syncClientSchema
import com.example.services.DatabaseConnectionManager
import com.example.services.handlers.*
import com.google.gson.Gson
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


fun Route.protectedRoutes() {
    authenticate("auth-jwt") {
        get("/user/data") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username")?.asString()
            val databaseName = principal?.payload?.getClaim("databaseName")?.asString()
            val databaseHost = principal?.payload?.getClaim("databaseHost")?.asString()
            val databasePort = principal?.payload?.getClaim("databasePort")?.asInt()
            val databaseUser = principal?.payload?.getClaim("databaseUser")?.asString()
            val databasePassword = principal?.payload?.getClaim("databasePassword")?.asString()

            if (username != null && databaseName != null && databaseHost != null && databaseUser != null && databasePassword != null) {
                try {
                    Database.connect(
                        url = "jdbc:postgresql://$databaseHost:$databasePort/$databaseName",
                        driver = "org.postgresql.Driver",
                        user = databaseUser,
                        password = databasePassword
                    )

                    val userData = transaction {
                        UserTable.select { UserTable.username eq username }
                            .map {
                                mapOf(
                                    "username" to it[UserTable.username],
                                    "email" to it[UserTable.email],
                                    "role" to it[UserTable.role]
                                )
                            }
                            .firstOrNull()
                    }

                    if (userData != null) {
                        call.respond(HttpStatusCode.OK, userData)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User data not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error: ${e.localizedMessage}"))
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
            }
        }
    }
}


private val logger = LoggerFactory.getLogger("SyncRoutes")

@OptIn(InternalSerializationApi::class)
fun Route.syncRoutes() {
    route("/sync") {

        /**
         * Endpoint: POST /sync/fetchEntities
         * Description: Fetches entity data based on request parameters.
         * Requires: Bearer Token Authentication
         */
        post("/fetchEntities") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (token.isNullOrEmpty() || !isValidToken(token)) {
                logger.warn("Unauthorized access attempt with token: $token")
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }else{
                try {
                    logger.warn("Token matched: $token")
                    val request = call.receive<FetchRequest>()
                    val secretKey="super_secret_key"

                    // Log the received request and token
                    logger.info("Received Sync Request: Entity = ${request.entityName}, Timestamp = ${request.timestamp}")
                    logger.info("Token used: $token")
                    val dbManager = DatabaseConnectionManager(secretKey)
                    val connection = dbManager.getConnectionFromJwt(token)

                    if (connection != null) {
                        println("Database connected successfully!")
                        val data = fetchEntityDataFromDB()

                        call.respond(HttpStatusCode.OK, data)
                        connection.close()
                    } else {
                        println("Failed to connect to the database.")
                    }
                    // Fetch data from DB

                } catch (e: Exception) {
                    logger.error("Error processing sync request", e)
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                }
            }


        }
        post("/syncData"){
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

            if (token.isNullOrEmpty() || !isValidToken(token)) {
                logger.warn("Unauthorized access attempt with token: $token")
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }else{
                try {
                    logger.warn("Token matched: $token")
                    val request = call.receive<SyncDataRequest>()
                    val secretKey = "super_secret_key"

                    // Log the received request and token
                    logger.info("Received Sync Request: Entity = ${request.entityName}, Timestamp = ${request.timestamp}")
                    logger.info("Received Sync Data =${request.data}")
                    logger.info("Token used: $token")

                    val dbManager = DatabaseConnectionManager(secretKey)
                    val connection = dbManager.getConnectionFromJwt(token)
                    val syncTime = request.timestamp

                    if (connection != null) {
                        println("Database connected successfully!")
                        //syncClientSchema()

                        try {
//                              Version 1 working
                            // Fetch data based on the entity using the correct entity handler
                            val entityData: List<SynCable> = entityHandlers[request.entityName]?.invoke(connection, request.entityName)
                                ?: throw IllegalArgumentException("Unsupported entity name: ${request.entityName}")
                            println("Entity raw data : $entityData")

                            //---------------------------------------------------
                            //version 2 under testing

                            val filtered = filterServerDataForSync(
                                entityName = request.entityName,
                                clientData = request.data,
                                serverData = entityData,
                                json = json,
                                timestamp = request.timestamp
                            )


                            val response = SyncDataResponse(
                                entityName = request.entityName,
                                timestamp = syncTime,
                                data = filtered
                            )

                            call.respond(HttpStatusCode.OK, response)



                        } catch (e: Exception) {
                            logger.error("Error processing the sync request", e)
                            call.respond(HttpStatusCode.BadRequest, "Failed to fetch or process data")
                        } finally {
                            // Always close the connection after the task is completed
                            connection.close()
                        }
                    } else {
                        logger.error("Failed to connect to the database.")
                        call.respond(HttpStatusCode.InternalServerError, "Database connection failed")
                    }
                } catch (e: Exception) {
                    logger.error("Error processing sync request", e)
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                }
            }
        }
    }
}


/**
 * Simulated function to validate token.
 * Replace this with actual authentication logic.
 */
fun isValidToken(token: String): Boolean {
    val isValid = JwtConfig.validateToken(token)
    println("Token is valid: $isValid")
    // Example: Token should be "valid_token" (Replace this with DB verification)
    return isValid
}

/**
 * Simulated function to fetch data from the database.
 */
fun fetchEntityDataFromDB(): List<String> {
    val list =ArrayList<String>()
    for (i in 1..5){
        list.add("list$i")

    }
    return list
}
fun filterServerDataForSync(
    entityName: String,
    clientData: List<JsonElement>,
    serverData: List<Any>,
    json: Json,
    timestamp: String
): List<JsonElement> {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    //val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    // Assuming timestamp is passed in the same format (from the last sync)
    val clientTimestamp = dateFormat.parse(timestamp) ?: Date(0) // Default to epoch time if parsing fails

    return when (entityName) {
        "Customer" -> {
            println("=== [Customer Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Customers.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.CustomerID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Customers>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.CustomerID]

                val serverModified = try {
                    // First, parse server string (which includes timezone offset)
                    val serverRawFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val parsedDate = serverRawFormat.parse(serverItem.LastModified)

                    // Then format it into your app's date string format
                    val appFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val formattedDateStr = parsedDate?.let { appFormat.format(it) }

                    // Finally, parse it back to a Date using your app's formatter
                    formattedDateStr?.let { appFormat.parse(it) }

                } catch (e: Exception) {
                    println("Failed to parse/convert server LastModified: ${serverItem.LastModified}")
                    null
                }


                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.CustomerID}")
                        println("Evaluating: ${serverItem.CustomerID}")
                        println("→ Server Modified: $serverModified")
                        println("→ Client Modified: $clientModified")
                        println("→ Last Sync Time: $clientTimestamp")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.CustomerID}")
                        println("Evaluating: ${serverItem.CustomerID}")
                        println("→ Server Modified: $serverModified")
                        println("→ Client Modified: $clientModified")
                        println("→ Last Sync Time: $clientTimestamp")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.CustomerID}")
                        println("Evaluating: ${serverItem.CustomerID}")
                        println("→ Server Modified: $serverModified")
                        println("→ Client Modified: $clientModified")
                        println("→ Last Sync Time: $clientTimestamp")
                        false
                    }
                }


                include // Return whether the record should be included in the response

               // include
            }
            transaction {
                println("Client -> Server Sync Started")
                upsertCustomerData(clientList)
                println("Client -> Server Sync Finished")
            }


            println("Filtered List to Return: $filtered")

            // Return the filtered list as JSON
            filtered.map { json.encodeToJsonElement(Customers.serializer(), it) }
        }


        "Equipment" -> {
            println("=== [Equipment Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Equipments.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.EquipmentID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Equipments>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.EquipmentID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.EquipmentID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.EquipmentID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.EquipmentID}")
                        false
                    }
                }

                include
            }
            transaction {
                println("Client -> Server Sync Started")
                upsertUEquipmentData(clientList)
                println("Client -> Server Sync Finished")
            }


            println("Filtered List to Return: $filtered")

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Equipments.serializer(), it)
            }
        }

        "Users" -> {
            println("=== [Users Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Users.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.UserID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Users>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.UserID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.UserID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.UserID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.UserID}")
                        false
                    }
                }

                include
            }

            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertUsersData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Users.serializer(), it)
            }
        }
        "Contracts" -> {
            println("=== [Contracts Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Contracts.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.ContractID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Contracts>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.ContractID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.ContractID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.ContractID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.ContractID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertContractsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Contracts.serializer(), it)
            }
        }

        "CategoryAsset" -> {
            println("=== [CategoryAsset Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(CategoryAsset.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.CategoryAssetID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<CategoryAsset>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.CategoryAssetID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.CategoryAssetID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.CategoryAssetID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.CategoryAssetID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertCategoryAssetData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(CategoryAsset.serializer(), it)
            }
        }
        "CheckForms" -> {
            println("=== [CheckForms Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(CheckForms.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.CheckFormID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<CheckForms>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.CheckFormID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.CheckFormID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.CheckFormID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.CheckFormID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertCheckFormsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(CheckForms.serializer(), it)
            }
        }
        "ContractEquipments" -> {
            println("=== [ContractEquipments Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(ContractEquipments.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.ContractEquipmentID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<ContractEquipments>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.ContractEquipmentID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.ContractEquipmentID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.ContractEquipmentID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.ContractEquipmentID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertContractEquipmentsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(ContractEquipments.serializer(), it)
            }
        }
        "Departments" -> {
            println("=== [Departments Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Departments.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.DepartmentID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Departments>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.DepartmentID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.DepartmentID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.DepartmentID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.DepartmentID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertDepartmentsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Departments.serializer(), it)
            }
        }
        "FieldReportEquipment" -> {
            println("=== [FieldReportEquipment Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(FieldReportEquipment.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.FieldReportEquipmentID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<FieldReportEquipment>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.FieldReportEquipmentID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.FieldReportEquipmentID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.FieldReportEquipmentID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.FieldReportEquipmentID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertFieldReportEquipmentData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(FieldReportEquipment.serializer(), it)
            }
        }
        "FieldReportInventory" -> {
            println("=== [FieldReportInventory Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(FieldReportInventory.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.FieldReportInventoryID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<FieldReportInventory>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.FieldReportInventoryID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.FieldReportInventoryID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.FieldReportInventoryID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.FieldReportInventoryID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertFieldReportInventoryData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(FieldReportInventory.serializer(), it)
            }
        }
        "FieldReports" -> {
            println("=== [FieldReports Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(FieldReports.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.FieldReportID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<FieldReports>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.FieldReportID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.FieldReportID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.FieldReportID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.FieldReportID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertFieldReportsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(FieldReports.serializer(), it)
            }
        }
        "FieldReportTools" -> {
            println("=== [FieldReportTools Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(FieldReportTools.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.FieldReportToolsID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<FieldReportTools>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.FieldReportToolsID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.FieldReportToolsID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.FieldReportToolsID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.FieldReportToolsID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertFieldReportToolsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(FieldReportTools.serializer(), it)
            }
        }
        "Inventory" -> {
            println("=== [Inventory Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Inventory.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.InventoryID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Inventory>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.InventoryID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.InventoryID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.InventoryID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.InventoryID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertInventoryData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Inventory.serializer(), it)
            }
        }
        "Maintenances" -> {
            println("=== [Maintenances Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Maintenances.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.MaintenanceID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Maintenances>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.MaintenanceID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.MaintenanceID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.MaintenanceID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.MaintenanceID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertMaintenancesData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Maintenances.serializer(), it)
            }
        }
        "Manufacturer" -> {
            println("=== [Manufacturer Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Manufacturer.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.ManufacturerID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Manufacturer>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.ManufacturerID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.ManufacturerID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.ManufacturerID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.ManufacturerID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertManufacturerData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Manufacturer.serializer(), it)
            }
        }
        "ModelAsset" -> {
            println("=== [ModelAsset Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(ModelAsset.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.ModelID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<ModelAsset>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.ModelID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.ModelID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.ModelID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.ModelID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertModelAssetData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(ModelAsset.serializer(), it)
            }
        }
        "Settings" -> {
            println("=== [Settings Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Settings.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.SettingsID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Settings>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.SettingsID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.SettingsID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.SettingsID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.SettingsID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertSettingsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Settings.serializer(), it)
            }
        }
        "Tasks" -> {
            println("=== [Tasks Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Tasks.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.TaskID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Tasks>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.TaskID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.TaskID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.TaskID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.TaskID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertTasksData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Tasks.serializer(), it)
            }
        }
        "TicketHistory" -> {
            println("=== [TicketHistory Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(TicketHistory.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.TicketHistoryID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<TicketHistory>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.TicketHistoryID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.TicketHistoryID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.TicketHistoryID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.TicketHistoryID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertTicketHistoryData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(TicketHistory.serializer(), it)
            }
        }
        "Tickets" -> {
            println("=== [Tickets Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Tickets.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.TicketID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Tickets>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.TicketID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.TicketID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.TicketID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.TicketID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertTicketsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Tickets.serializer(), it)
            }
        }
        "Tools" -> {
            println("=== [Tools Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(Tools.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.ToolsID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<Tools>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.ToolsID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.ToolsID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.ToolsID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.ToolsID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertToolsData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(Tools.serializer(), it)
            }
        }
        "FieldReportCheckForm" -> {
            println("=== [FieldReportCheckForm Sync] ===")
            println("Raw Client Data: $clientData")

            // Decode client data
            val clientList = clientData.map {
                json.decodeFromJsonElement(FieldReportCheckForm.serializer(), it)
            }
            println("Parsed Client List: $clientList")

            // Create a map for quick lookup
            val clientMap = clientList.associateBy { it.FieldReportCheckFormID }

            // Cast server data to Customers list
            val serverList = serverData.filterIsInstance<FieldReportCheckForm>()
            println("Server List: $serverList")

            // Compare and filter
            val filtered = serverList.filter { serverItem ->
                val clientItem = clientMap[serverItem.FieldReportCheckFormID]

                val serverModified = try {
                    dateFormat.parse(serverItem.LastModified)
                } catch (e: Exception) {
                    println("Failed to parse server LastModified: ${serverItem.LastModified}")
                    null
                }

                val clientModified = try {
                    clientItem?.LastModified?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    println("Failed to parse client LastModified: ${clientItem?.LastModified}")
                    null
                }

                val include = when {
                    // No matching client item, and server data is newer than client's last sync
                    clientItem == null && serverModified?.after(clientTimestamp) == true -> {
                        println("New server record to send: ${serverItem.FieldReportCheckFormID}")
                        true
                    }

                    // Item exists on both sides; compare modification times
                    serverModified != null && clientModified != null && serverModified.after(clientModified) -> {
                        println("Server has newer data for: ${serverItem.FieldReportCheckFormID}")
                        true
                    }

                    else -> {
                        println("Skipping: ${serverItem.FieldReportCheckFormID}")
                        false
                    }
                }

                include
            }


            println("Filtered List to Return: $filtered")

            transaction {
                println("Client -> Server Sync Started")
                upsertFieldReportCheckFormData(clientList)
                println("Client -> Server Sync Finished")
            }

            // Convert to JSON
            filtered.map {
                json.encodeToJsonElement(FieldReportCheckForm.serializer(), it)
            }
        }

        else -> emptyList()
    }
}



/**
 * Data class for sync requests
 */
@Serializable
data class FetchRequest(
    val entityName: String,
    val timestamp: String
)
@Serializable
data class SyncDataRequest(
    val entityName: String, // e.g., "User", "Product"
    val timestamp: String ,  // e.g., "2024-03-03T12:00:00Z"
    val data: List<JsonElement> // This will be a dynamic list, cast later eg . List <Customers> , List<Equipments> , List<WorkOrders
)
@Serializable
data class SyncDataResponse(
    val entityName: String, // e.g., "User", "Product"
    val timestamp: String ,  // e.g., "2024-03-03T12:00:00Z"
    val data: List<JsonElement> // This will be a dynamic list, cast later eg . List <Customers> , List<Equipments> , List<WorkOrders
)