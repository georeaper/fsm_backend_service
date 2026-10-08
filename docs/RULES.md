# Date Format Guidelines

## Σκοπός
Όλες οι εφαρμογές και τα συστήματα της FSM Backend Service πρέπει να ακολουθούν αυστηρούς κανόνες για την αποθήκευση, μεταφορά και παρουσίαση ημερομηνιών. Αυτό εξασφαλίζει συνέπεια, αποφυγή σφαλμάτων και διαλειτουργικότητα.

---

## Υποχρεωτική Χρήση DateUtils

**Κανόνας:** ✅ **ΥΠΟΧΡΕΩΤΙΚΗ** χρήση του `DateUtils` utility για όλη την επεξεργασία ημερομηνιών.

Όλες οι μετατροπές, υπολογισμοί και μετασχηματισμοί ημερομηνιών πρέπει να γίνονται μέσω του `com.example.core.DateUtils` object.

### Import
```kotlin
import com.example.core.DateUtils
```

---

## Διαθέσιμα Formats

### 1. STORAGE_FORMAT (Αποθήκευση στΒΔ)
```
Format: yyyyMMddHHmmss
Παράδειγμα: 20260917130649
```

**Χρήση:**
- Αποθήκευση ημερομηνιών στη βάση δεδομένων
- Σύγκριση και ταξινόμηση ημερομηνιών
- Εσωτερικές μεταφορές δεδομένων
- Audit logs και tracking

**Πλεονεκτήματα:**
- ✅ Σειριακή ταξινόμηση (sortable)
- ✅ Χωρίς ειδικούς χαρακτήρες
- ✅ Χρονολογική σειρά ως αλφαριθμητική
- ✅ UTC/Timezone independent

### 2. UI_FORMAT (Εμφάνιση στο UI)
```
Format: dd/MM/yyyy
Παράδειγμα: 17/09/2026
```

**Χρήση:**
- Εμφάνιση ημερομηνιών στις σελίδες εφαρμογής
- Input fields και form validations
- Reports και εκτυπώσιμα έγγραφα
- Επικοινωνία με τον τελικό χρήστη

**Σημείωση:** Το format χρησιμοποιεί `dd/MM/yyyy` για αναγνωσιμότητα στα ελληνικά (ημέρα/μήνα/έτος).

---

## Διαθέσιμες Μέθοδοι

### 1. uiToStorage(input: String): String?
Μετατροπή από μορφή UI → Μορφή Αποθήκευσης

```kotlin
// Input από χρήστη ή API
val uiDate = "17/09/2026"  // dd/MM/yyyy

// Μετατροπή
val storageDate = DateUtils.uiToStorage(uiDate)
// Result: "20260917000000" (θέτει ώρα = 00:00:00)

// Αποθήκευση στη ΒΔ
database.update { 
    dateCreated = storageDate
}
```

**Ιδιότητες:**
- Εισάγει ώρα `00:00:00` αν δεν ορίζεται ώρα
- Επιστρέφει `null` αν το format είναι άκυρο
- Strict validation (isLenient = false)

---

### 2. storageToUi(value: String?): String?
Μετατροπή από Μορφή Αποθήκευσης → Μορφή UI

```kotlin
// Ανάγνωση από ΒΔ
val storageDate = "20260917130649"  // yyyyMMddHHmmss

// Μετατροπή για εμφάνιση
val uiDate = DateUtils.storageToUi(storageDate)
// Result: "17/09/2026"

// Εμφάνιση στο UI
Text(uiDate)
```

**Ιδιότητες:**
- Αγνοεί την ώρα (εμφανίζει μόνο την ημερομηνία)
- Επιστρέφει `null` αν το format είναι άκυρο
- Safe για null inputs

---

### 3. nowStorage(): String
Τρέχουσα ημερομηνία/ώρα στη μορφή αποθήκευσης

```kotlin
// Για audit fields
database.insert {
    dateCreated = DateUtils.nowStorage()  // "20260917130649"
    lastModified = DateUtils.nowStorage()
}
```

**Χρήση:**
- `DateCreated` fields
- `LastModified` fields
- Timestamps σε logs
- Audit trails

---

## Παραδείγματα Χρήσης

### Παράδειγμα 1: Αποθήκευση νέας εγγραφής

```kotlin
fun createTicket(
    title: String,
    dateStart: String  // από user input, format: "dd/MM/yyyy"
): Boolean {
    return try {
        // Μετατροπή ημερομηνίας
        val storageDateStart = DateUtils.uiToStorage(dateStart)
            ?: return false  // Invalid date format
        
        // Τρέχουσα ημερομηνία
        val now = DateUtils.nowStorage()
        
        // Αποθήκευση
        database.insertInto(ticketTable).values(
            ticketId = UUID.randomUUID().toString(),
            dateStart = storageDateStart,
            dateCreated = now,
            lastModified = now
        )
        true
    } catch (e: Exception) {
        false
    }
}
```

### Παράδειγμα 2: Ανάγνωση και εμφάνιση

```kotlin
fun displayTicketDetails(ticketId: String) {
    val ticket = database.select {
        ticketTable.where { ticketTable.ticketId eq ticketId }
    }.firstOrNull()
    
    ticket?.let {
        // Μετατροπή για εμφάνιση
        val displayDate = DateUtils.storageToUi(it.dateStart)
        println("Ημερομηνία Έναρξης: $displayDate")
    }
}
```

### Παράδειγμα 3: Ενημέρωση εγγραφής

```kotlin
fun updateTicket(ticketId: String, newDate: String) {
    val storageDate = DateUtils.uiToStorage(newDate)
        ?: throw IllegalArgumentException("Invalid date format")
    
    database.update(ticketTable) {
        set(ticketTable.dateStart, storageDate)
        set(ticketTable.lastModified, DateUtils.nowStorage())
        where { ticketTable.ticketId eq ticketId }
    }
}
```

---

## Κανόνες και Περιορισμοί

### ✅ ΝΑΙ - Allowed

```kotlin
// ✅ Σωστή χρήση
val storageDateStart = DateUtils.uiToStorage(userInput)
val uiDate = DateUtils.storageToUi(dbValue)
val now = DateUtils.nowStorage()
```

### ❌ ΟΧΙ - Forbidden

```kotlin
// ❌ ΛΑΘΟΣ: Χρήση SimpleDateFormat απευθείας
val formatter = SimpleDateFormat("dd/MM/yyyy")
val date = formatter.parse(userInput)

// ❌ ΛΑΘΟΣ: Hardcoded date strings
database.insert {
    dateCreated = "2026-09-17"  // Δεν έχει consistency
}

// ❌ ΛΑΘΟΣ: Διαφορετικά formats
val date1 = "17/09/2026"   // dd/MM/yyyy
val date2 = "2026-09-17"   // yyyy-MM-dd
val date3 = "09/17/2026"   // MM/dd/yyyy
// Mixing formats = σύγχυση και σφάλματα!

// ❌ ΛΑΘΟΣ: Χρήση System.currentTimeMillis()
val timestamp = System.currentTimeMillis()  // Αντί για DateUtils.nowStorage()
```

---

## Timezone Considerations

**Σημαντικό:** Το DateUtils χρησιμοποιεί `Locale.US` για consistency, αλλά:

- Η ώρα αποθηκεύεται χωρίς timezone information
- Υποθέτει UTC/Server timezone
- Για timezone-aware applications, χρησιμοποιήστε `Calendar` με ειδικές ρυθμίσεις

```kotlin
// Αν χρειάζεται timezone
val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Athens"))
// Στη συνέχεια χρησιμοποιήστε το Calendar object
```

---

## Error Handling

Όλες οι μέθοδοι του DateUtils επιστρέφουν `null` σε σφάλματα:

```kotlin
val result = DateUtils.uiToStorage(userInput)
if (result == null) {
    // Invalid date format - ενημέρωση χρήστη
    showError("Λανθασμένη μορφή ημερομηνίας. Χρησιμοποιήστε dd/MM/yyyy")
}
```

---

## Database Schema Integration

Όλα τα date fields στη βάση δεδομένων πρέπει να είναι:

```kotlin
// VARCHAR(50) για αποθήκευση
val dateStart = varchar("DateStart", 50).nullable()
val dateCreated = varchar("DateCreated", 50).nullable()
val lastModified = varchar("LastModified", 50).nullable()
```

**Όχι TIMESTAMP ή DATETIME** - χρησιμοποιείστε VARCHAR με format `yyyyMMddHHmmss`

---

## API Endpoints

### Input (Request Body)
Ημερομηνίες στο API request πρέπει να είναι σε μορφή UI:

```json
{
  "dateStart": "17/09/2026",
  "dateDue": "30/09/2026"
}
```

**Στον controller:**
```kotlin
@PostMapping("/ticket")
fun createTicket(@RequestBody request: TicketRequest) {
    val storageDate = DateUtils.uiToStorage(request.dateStart)
        ?: return ResponseEntity.badRequest().body("Invalid date format")
    
    // Process...
}
```

### Output (Response Body)
Ημερομηνίες στο API response πρέπει να είναι σε μορφή UI:

```json
{
  "ticketId": "abc-123",
  "dateStart": "17/09/2026",
  "dateCreated": "17/09/2026"
}
```

**Στον controller:**
```kotlin
@GetMapping("/ticket/{id}")
fun getTicket(@PathVariable id: String): TicketResponse {
    val ticket = ticketRepository.findById(id)
    return TicketResponse(
        ticketId = ticket.ticketId,
        dateStart = DateUtils.storageToUi(ticket.dateStart),
        dateCreated = DateUtils.storageToUi(ticket.dateCreated)
    )
}
```

---

## Checklist για Developers

Όταν δουλεύετε με ημερομηνίες:

- [ ] Χρησιμοποιώ `DateUtils` για ΌΛΕΣ τις μετατροπές
- [ ] Τα DB fields χρησιμοποιούν `STORAGE_FORMAT` (yyyyMMddHHmmss)
- [ ] Τα UI fields χρησιμοποιούν `UI_FORMAT` (dd/MM/yyyy)
- [ ] Έχω validation για invalid dates
- [ ] Ενημερώνω `lastModified` με `DateUtils.nowStorage()` σε κάθε update
- [ ] Τα API responses εμφανίζουν ημερομηνίες σε UI format
- [ ] Τα API requests δέχονται ημερομηνίες σε UI format
- [ ] Δεν χρησιμοποιώ hardcoded date strings
- [ ] Δεν χρησιμοποιώ System.currentTimeMillis()
- [ ] Δεν χρησιμοποιώ SimpleDateFormat απευθείας

---

## Ενημερώσεις και Μετακινήσεις

Αν χρειαστεί να αλλάξουν τα formats, τότε:

1. **Ενημερώστε το DateUtils.kt**
2. **Ενημερώστε αυτό το document**
3. **Ενημερώστε όλες τις αποθηκευμένες ημερομηνίες** (migration script)
4. **Ενημερώστε όλα τα tests**
5. **Ειδοποιήστε το development team**

---

## Reference

- **DateUtils Location:** `src/main/kotlin/com/example/core/DateUtils.kt`
- **Database Schema:** `docs/DATABASE_STRUCTURE.md`
- **Related Docs:** `CODING_STANDARDS.md`, `API_GUIDELINES.md`

---

**Ημερομηνία Τελευταίας Ενημέρωσης:** 2026-09-17

**Κατάσταση:** ✅ Active

---

# Feature Architecture Guidelines

## Σκοπός
Αυτή η ενότητα περιγράφει την αρχιτεκτονική και δομή για τη δημιουργία νέων features στο FSM Backend Service.

---

## Feature Structure (Δομή Φακέλου)

### Υποχρεωτική Δομή Feature

Κάθε feature πρέπει να ακολουθεί την παρακάτω δομή φακέλου:

```
src/main/kotlin/com/example/feature/{featureName}/
├── {FeatureName}Route.kt          ✅ Router endpoints
├── dto/
│   ├── Create{Feature}Request.kt  ✅ Request DTO
│   ├── {Feature}Response.kt       ✅ Response DTO
│   ├── Create{Feature}Response.kt ✅ Create response
│   └── Edit{Feature}Response.kt   ✅ Edit response
├── usecase/
│   ├── Create{Feature}UseCase.kt  ✅ Create business logic
│   ├── Get{Feature}UseCase.kt     ✅ Read business logic
│   ├── Update{Feature}UseCase.kt  ✅ Update business logic
│   └── Delete{Feature}UseCase.kt  ✅ Delete business logic
└── repository/
    ├── {Feature}Repository.kt      ✅ Interface
    └── {Feature}RepositoryImpl.kt   ✅ Implementation
```

### Ονοματολογία (Naming Convention)

| Στοιχείο | Format | Παράδειγμα |
|----------|--------|-----------|
| Feature Folder | `{featureName}` (camelCase) | `customers`, `fieldreport` |
| Route File | `{FeatureName}Route.kt` (PascalCase) | `CustomersRoute.kt` |
| UseCase Class | `{Action}{Feature}UseCase.kt` | `CreateCustomerUseCase` |
| Repository Interface | `{Feature}Repository.kt` | `CustomersRepository` |
| Repository Impl | `{Feature}RepositoryImpl.kt` | `CustomerRepositoryImpl` |
| Request DTO | `Create{Feature}Request.kt` | `CreateCustomerRequest.kt` |
| Response DTO | `{Feature}Response.kt` | `CustomerResponse.kt` |

---

## Feature Route Implementation

### 1. Route File Structure (`{FeatureName}Route.kt`)

```kotlin
package com.example.feature.customers

import com.example.core.RequestContext
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.customers.usecase.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.receive

// ✅ Route function που δέχεται dependency injection
fun Route.customersRoute(
    getCustomersUseCase: GetCustomersUseCase,
    createCustomerUseCase: CreateCustomerUseCase,
    updateCustomerUseCase: UpdateCustomerUseCase,
    deleteCustomerUseCase: DeleteCustomerUseCase
) {
    authenticate("auth-jwt") {
        // GET endpoint
        get("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            
            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            
            val result = getCustomersUseCase.execute(ctx)
            call.respond(result)
        }
        
        // POST endpoint
        post("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            
            val dbName = principal.getClaim("databaseName", String::class)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            
            val ctx = RequestContext(
                dbName = dbName,
                username = principal.getClaim("username", String::class)
            )
            
            val request = call.receive<CreateCustomerRequest>()
            val result = createCustomerUseCase.execute(ctx, request)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
```

### 2. UseCase Structure

```kotlin
package com.example.feature.customers.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.customers.dto.CreateCustomerRequest
import com.example.feature.customers.repository.CustomersRepository
import com.example.models.api.Customers
import java.util.UUID

class CreateCustomerUseCase(
    private val repository: CustomersRepository
) {
    fun execute(
        ctx: RequestContext,
        input: CreateCustomerRequest
    ): Customers {
        // ✅ Validation
        require(input.name?.isNotBlank() == true) { "Name is required" }
        
        // ✅ Normalization
        val email = input.email?.trim()?.lowercase()
        
        // ✅ Use DateUtils for dates
        val storageDate = DateUtils.nowStorage()
        
        val customer = Customers(
            CustomerID = UUID.randomUUID().toString(),
            Name = input.name.trim(),
            Email = email,
            Phone = input.phone?.trim(),
            Address = input.address?.trim(),
            City = input.city?.trim(),
            ZipCode = input.zipCode?.trim(),
            Notes = input.notes?.trim(),
            Description = input.description?.trim(),
            CustomerStatus = true,
            DateCreated = storageDate,      // ✅ Storage format
            LastModified = storageDate,     // ✅ Storage format
            Version = "1.0",
            RemoteID = null
        )
        
        return repository.save(ctx, customer)
    }
}
```

### 3. Repository Structure

```kotlin
package com.example.feature.customers.repository

import com.example.core.DatabaseProvider
import com.example.core.RequestContext
import com.example.feature.customers.dto.CustomersDto
import com.example.models.api.Customers
import com.example.models.databaseModels.customerTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// ✅ Interface definition
interface CustomersRepository {
    fun save(ctx: RequestContext, customer: Customers): Customers
    fun findAll(ctx: RequestContext): List<CustomersDto>
    fun findById(ctx: RequestContext, id: String): Customers?
    fun update(ctx: RequestContext, customer: Customers): Boolean
    fun delete(ctx: RequestContext, id: String): Boolean
}

// ✅ Implementation
class CustomerRepositoryImpl(
    private val dbProvider: DatabaseProvider
) : CustomersRepository {
    
    override fun save(ctx: RequestContext, customer: Customers): Customers {
        val db = dbProvider.getDatabase(ctx.dbName)
        
        transaction(db) {
            customerTable.insert {
                it[customerId] = customer.CustomerID
                it[name] = customer.Name
                it[email] = customer.Email
                it[phone] = customer.Phone
                it[address] = customer.Address
                it[city] = customer.City
                it[zipCode] = customer.ZipCode
                it[notes] = customer.Notes
                it[description] = customer.Description
                it[customerStatus] = customer.CustomerStatus
                it[dateCreated] = customer.DateCreated         // ✅ Storage format
                it[lastModified] = customer.LastModified       // ✅ Storage format
                it[version] = customer.Version
                it[remoteId] = customer.RemoteID
            }
        }
        
        return customer
    }
    
    override fun findAll(ctx: RequestContext): List<CustomersDto> {
        val db = dbProvider.getDatabase(ctx.dbName)
        
        return transaction(db) {
            customerTable
                .select { customerTable.isDeleted eq false }
                .map { row ->
                    CustomersDto(
                        id = row[customerTable.customerId],
                        name = row[customerTable.name],
                        email = row[customerTable.email],
                        phone = row[customerTable.phone],
                        address = row[customerTable.address],
                        city = row[customerTable.city],
                        zipCode = row[customerTable.zipCode],
                        notes = row[customerTable.notes],
                        description = row[customerTable.description],
                        dateCreated = DateUtils.storageToUi(row[customerTable.dateCreated]),  // ✅ Convert to UI
                        lastModified = DateUtils.storageToUi(row[customerTable.lastModified]), // ✅ Convert to UI
                        status = row[customerTable.customerStatus]?.toString()
                    )
                }
        }
    }
}
```

---

## Core Layer Integration

### AppContainer Setup

**Αρχείο:** `src/main/kotlin/com/example/core/AppContainer.kt`

Κάθε feature πρέπει να καταχωρηθεί στο `AppContainer`:

```kotlin
package com.example.core

import com.example.feature.customers.usecase.*
import com.example.feature.customers.repository.CustomerRepositoryImpl
// ... άλλα imports

class AppContainer {
    
    // ✅ Database Provider (singleton)
    private val dbProvider = DatabaseProvider()
    
    // ✅ Repository instances (singleton per repository)
    private val customerRepository = CustomerRepositoryImpl(dbProvider)
    
    // ✅ UseCase instances (singleton per usecase)
    val createCustomerUseCase = CreateCustomerUseCase(customerRepository)
    val getCustomersUseCase = GetCustomersUseCase(customerRepository)
    val updateCustomerUseCase = UpdateCustomerUseCase(customerRepository)
    val deleteCustomerUseCase = DeleteCustomerUseCase(customerRepository)
    
    // ✅ Επαναλάβετε για όλα τα features...
}
```

### Κανόνες AppContainer

1. **Ένα instance ανά αντικείμενο** - Όλες οι εξαρτήσεις είναι singleton
2. **Order matters** - Declare repositories πριν από UseCases
3. **Lazy initialization δεν επιτρέπεται** - Use eager initialization
4. **Κανένα context dependency** - Κανένα RequestContext στο constructor

---

## Application.kt Integration

**Αρχείο:** `src/main/kotlin/com/example/Application.kt`

### 1. Imports

```kotlin
package com.example

import com.example.core.AppContainer
import com.example.feature.customers.customersRoute
import com.example.feature.equipments.equipmentsRoute
import com.example.feature.fieldreport.fieldReportRoute
// ... άλλα imports
```

### 2. Setup dalam Module

```kotlin
fun Application.module() {
    // ✅ Install plugins
    install(ContentNegotiation) { json() }
    install(CORS) { /* config */ }
    install(Authentication) { /* config */ }
    
    // ✅ Create AppContainer
    val container = AppContainer()
    
    // ✅ Configure routing
    routing {
        // ✅ Κάθε feature route δέχεται dependencies από container
        customersRoute(
            getCustomersUseCase = container.getCustomersUseCase,
            createCustomerUseCase = container.createCustomerUseCase,
            updateCustomerUseCase = container.updateCustomerUseCase,
            deleteCustomerUseCase = container.deleteCustomerUseCase
        )
        
        equipmentsRoute(
            getEquipmentUseCase = container.getEquipmentUseCase,
            createEquipmentUseCase = container.createEquipmentUseCase,
            updateEquipmentUseCase = container.updateEquipmentUseCase,
            deleteEquipmentUseCase = container.deleteEquipmentUseCase
        )
        
        fieldReportRoute(
            getFieldReportUseCase = container.getFieldReportUseCase,
            createFieldReportUseCase = container.createFieldReportUseCase,
            updateFieldReportUseCase = container.updateFieldReportUseCase,
            deleteFieldReportUseCase = container.deleteFieldReportUseCase
        )
        
        // ... Κάθε feature route στη σειρά...
    }
}
```

---

## DTO (Data Transfer Objects) Guidelines

### Request DTOs

```kotlin
package com.example.feature.customers.dto

data class CreateCustomerRequest(
    val name: String?,                  // ✅ Nullable for validation
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val zipCode: String?,
    val notes: String?,
    val description: String?,
    val dateCreated: String?            // ✅ UI format (dd/MM/yyyy)
)
```

### Response DTOs

```kotlin
package com.example.feature.customers.dto

data class CustomersDto(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val zipCode: String?,
    val notes: String?,
    val description: String?,
    val dateCreated: String?,           // ✅ UI format (dd/MM/yyyy) - converted by repository
    val lastModified: String?,          // ✅ UI format (dd/MM/yyyy) - converted by repository
    val status: String?
)
```

---

## Best Practices

### ✅ ΝΑΙ - Do

```kotlin
// ✅ UseCase validation
class CreateCustomerUseCase(repository: CustomersRepository) {
    fun execute(ctx: RequestContext, input: CreateCustomerRequest) {
        require(input.name?.isNotBlank() == true) { "Name required" }
        require(input.email?.contains("@") == true) { "Valid email required" }
        // ... logic
    }
}

// ✅ Repository uses DateUtils for UI conversion
val dateCreatedUI = DateUtils.storageToUi(row[customerTable.dateCreated])

// ✅ UseCase uses DateUtils for storage format
val storageDate = DateUtils.nowStorage()

// ✅ Route extracts RequestContext from JWT
val dbName = principal.getClaim("databaseName", String::class)
val ctx = RequestContext(dbName = dbName, username = ...)

// ✅ Single Responsibility - one UseCase per action
class CreateCustomerUseCase { fun execute(...) }
class UpdateCustomerUseCase { fun execute(...) }
class DeleteCustomerUseCase { fun execute(...) }
```

### ❌ ΟΧΙ - Avoid

```kotlin
// ❌ Business logic σε Route file
fun Route.customersRoute() {
    post("/customers") {
        val customer = Customers(...)  // ❌ WRONG PLACE
        repository.save(customer)      // ❌ WRONG PLACE
    }
}

// ❌ Direct database access σε UseCase
class CreateCustomerUseCase {
    fun execute() {
        transaction(db) {               // ❌ Should be in Repository
            customerTable.insert(...)
        }
    }
}

// ❌ Multiple responsibilities in one UseCase
class CustomerUseCase {
    fun create() { /* ... */ }
    fun update() { /* ... */ }
    fun delete() { /* ... */ }
    fun getAll() { /* ... */ }          // ❌ Split into separate classes
}

// ❌ No validation in UseCase
class CreateCustomerUseCase {
    fun execute(ctx: RequestContext, input: CreateCustomerRequest) {
        val customer = Customers(name = input.name)  // ❌ No validation!
        repository.save(customer)
    }
}

// ❌ String dates χωρίς DateUtils
val dateStr = "2026-09-17"              // ❌ No format consistency
val today = System.currentTimeMillis()  // ❌ Use DateUtils.nowStorage()
```

---

## Feature Addition Checklist

Όταν προσθέτετε ένα νέο feature:

- [ ] Δημιουργώ φάκελο στο `src/main/kotlin/com/example/feature/{featureName}`
- [ ] Δημιουργώ `{FeatureName}Route.kt` με σωστή ονοματολογία
- [ ] Δημιουργώ `usecase/` folder με Create, Get, Update, Delete UseCases
- [ ] Δημιουργώ `repository/` folder με Interface και Impl
- [ ] Δημιουργώ `dto/` folder με Request/Response DTOs
- [ ] Χρησιμοποιώ `DateUtils` για όλες τις ημερομηνίες
- [ ] Προσθέτω εξαρτήσεις στο `AppContainer.kt`
- [ ] Καταχωρώ route στο `Application.kt`
- [ ] Όλα τα DTOs χρησιμοποιούν UI format για dates
- [ ] Όλες οι repositories μετατρέπουν storage dates σε UI format
- [ ] Όλα τα UseCase δεν περιέχουν direct DB logic
- [ ] Έχω proper error handling και validation

---

**Ενημέρωση:** 2026-09-17 | **Version:** 1.1

