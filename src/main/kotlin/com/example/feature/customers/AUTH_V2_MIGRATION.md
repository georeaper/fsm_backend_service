# Μετάβαση του Customers feature στο Authentication V2

Το παρόν αρχείο περιγράφει τις αλλαγές που χρειάζονται ώστε το
`CustomersRoute.kt` να λειτουργήσει με το νέο login και τον authentication
provider `auth-jwt-v2`.

Δεν έχουν εφαρμοστεί ακόμη οι αλλαγές που περιγράφονται εδώ. Το υπάρχον
customers feature συνεχίζει να χρησιμοποιεί το legacy `auth-jwt`.

## Τρέχουσα κατάσταση

Το `CustomersRoute.kt` χρησιμοποιεί:

```kotlin
authenticate("auth-jwt")
```

και διαβάζει απευθείας από το παλιό JWT:

```kotlin
val dbName = principal.getClaim("databaseName", String::class)
val username = principal.getClaim("username", String::class)
```

Στη συνέχεια δημιουργεί:

```kotlin
RequestContext(
    dbName = dbName,
    username = username
)
```

Η προσέγγιση αυτή δεν είναι συμβατή με το Authentication V2, επειδή το νέο JWT
δεν περιέχει `databaseName` ή `username`.

## Claims του νέου JWT

Το access token του Authentication V2 περιέχει μόνο:

- `sub`: το ID του χρήστη στον κεντρικό πίνακα `users`,
- `tenant_id`: το ID της εταιρείας στον πίνακα `companies`,
- `role`: τον ρόλο του χρήστη,
- `jti`, `iat`, `exp`, `iss` και `aud`.

Το `username` επιστρέφεται στο response του login και του refresh για χρήση από
το UI, αλλά δεν χρησιμοποιείται ως JWT claim ή ως πηγή authorization.

Το `databaseName` πρέπει να αναζητείται server-side από τον πίνακα `companies`
με βάση το `tenant_id`. Με αυτόν τον τρόπο ο client δεν μπορεί να επιλέξει ή να
τροποποιήσει αυθαίρετα το όνομα της tenant βάσης.

## Απαιτούμενες αλλαγές

### 1. Δημιουργία server-side tenant resolver

Χρειάζεται ένα component, για παράδειγμα `AuthenticatedRequestContextResolver`,
το οποίο θα:

1. Διαβάζει από το επαληθευμένο JWT τα `sub`, `tenant_id` και `role`.
2. Αναζητά τον χρήστη στην κεντρική βάση.
3. Επιβεβαιώνει ότι ο χρήστης έχει `status = active`.
4. Επιβεβαιώνει ότι το `users.company_id` συμφωνεί με το `tenant_id` του token.
5. Αναζητά το `companies.database_name` server-side.
6. Επιστρέφει ασφαλές `RequestContext`.

Η αναζήτηση πρέπει να γίνεται μέσω `DatabaseFactory.centralDb` και όχι μέσω
δεδομένων που στέλνει ο client.

Ενδεικτικό αποτέλεσμα του resolver:

```kotlin
data class AuthenticatedRequestContext(
    val userId: Int,
    val companyId: Int,
    val username: String,
    val role: String,
    val dbName: String
)
```

Ο resolver καλό είναι να βρίσκεται σε κοινό security/authentication package,
επειδή θα χρησιμοποιηθεί αργότερα και από τα υπόλοιπα features.

### 2. Επέκταση ή αντικατάσταση του `RequestContext`

Το σημερινό `RequestContext` περιέχει μόνο:

```kotlin
data class RequestContext(
    val dbName: String,
    val username: String?
)
```

Για το Authentication V2 προτείνεται να περιλαμβάνει τουλάχιστον:

```kotlin
data class RequestContext(
    val dbName: String,
    val username: String,
    val userId: Int,
    val companyId: Int,
    val role: String
)
```

Επειδή το `RequestContext` χρησιμοποιείται ήδη από πολλά features, η αλλαγή δεν
πρέπει να γίνει απότομα. Ασφαλείς επιλογές είναι:

- να προστεθούν nullable/default τιμές προσωρινά, ή
- να δημιουργηθεί νέο `AuthenticatedRequestContext` μόνο για τα features που
  μεταφέρονται στο V2.

Η δεύτερη επιλογή προσφέρει καθαρότερο migration boundary.

### 3. Αλλαγή provider στο `CustomersRoute`

Μετά την προσθήκη του resolver, το route πρέπει να αλλάξει από:

```kotlin
authenticate("auth-jwt")
```

σε:

```kotlin
authenticate("auth-jwt-v2")
```

Δεν αρκεί μόνο αυτή η αλλαγή. Αν αλλάξει ο provider χωρίς resolver, όλα τα
customers endpoints θα επιστρέφουν `401`, επειδή το νέο JWT δεν έχει το legacy
claim `databaseName`.

### 4. Αντικατάσταση της ανάγνωσης legacy claims

Κάθε endpoint σήμερα επαναλαμβάνει την ανάγνωση του principal και του
`databaseName`. Η λογική πρέπει να συγκεντρωθεί στον resolver.

Ενδεικτικά:

```kotlin
get("/customers") {
    val principal = call.principal<JWTPrincipal>()
        ?: return@get call.respond(HttpStatusCode.Unauthorized)

    val ctx = contextResolver.resolve(principal)
        ?: return@get call.respond(HttpStatusCode.Unauthorized)

    val result = getCustomersUseCase.execute(ctx.toRequestContext())
    call.respond(HttpStatusCode.OK, result)
}
```

Ο ίδιος resolver πρέπει να χρησιμοποιηθεί στα `POST`, `PUT` και `DELETE`.

### 5. Dependency injection στο route

Η υπογραφή του route θα χρειαστεί μία επιπλέον dependency:

```kotlin
fun Route.customersRoute(
    getCustomersUseCase: GetCustomersUseCase,
    createCustomerUseCase: CreateCustomerUseCase,
    updateCustomerUseCase: UpdateCustomerUseCase,
    deleteCustomerUseCase: DeleteCustomerUseCase,
    contextResolver: AuthenticatedRequestContextResolver
)
```

Το `AppContainer` πρέπει να δημιουργεί τον resolver και το `Application.kt` να
τον περνά στο `customersRoute`.

### 6. Role-based authorization

Το νέο JWT περιλαμβάνει `role`, αλλά το `CustomersRoute` δεν εφαρμόζει ακόμη
permissions.

Πρέπει να αποφασιστεί policy ανά operation. Ενδεικτικά:

| Endpoint | Ενδεικτικοί ρόλοι |
| --- | --- |
| `GET /customers` | `owner`, `admin`, `user` |
| `POST /customers` | `owner`, `admin` |
| `PUT /customers/{id}` | `owner`, `admin` |
| `DELETE /customers/{id}` | `owner`, `admin` |

Η τελική λίστα ρόλων πρέπει να συμφωνεί με τους πραγματικούς επιχειρησιακούς
κανόνες του project.

Αν ο χρήστης είναι authenticated αλλά δεν έχει τον απαιτούμενο ρόλο, πρέπει να
επιστρέφεται `403 Forbidden` και όχι `401 Unauthorized`.

Ενδεικτικός έλεγχος:

```kotlin
if (ctx.role !in setOf("owner", "admin")) {
    return@post call.respond(HttpStatusCode.Forbidden)
}
```

Ο έλεγχος καλό είναι να συγκεντρωθεί σε reusable authorization helper αντί να
αντιγράφεται σε κάθε route.

### 7. Ασφαλής σύνδεση στην tenant database

Το `CustomerRepositoryImpl` χρησιμοποιεί:

```kotlin
dbProvider.getDatabase(ctx.dbName)
```

Αυτό μπορεί να παραμείνει προσωρινά, εφόσον το `ctx.dbName` προέρχεται
αποκλειστικά από τον server-side resolver και όχι από JWT claim, request body,
query parameter ή route parameter.

Σε επόμενο hardening προτείνεται ο `DatabaseProvider` να δέχεται `companyId` ή
ένα ήδη validated tenant descriptor και να κάνει ο ίδιος το lookup/cache της
σύνδεσης. Έτσι το repository δεν θα χειρίζεται καθόλου database names.

### 8. Username και audit fields

Το `username` δεν χρειάζεται να διαβαστεί από το JWT. Ο resolver μπορεί να το
φορτώνει από το `UserTable` μαζί με τον έλεγχο του user/company association.

Για νέα audit πεδία είναι προτιμότερο να χρησιμοποιείται το αμετάβλητο `userId`
και όχι μόνο το username, επειδή το username μπορεί μελλοντικά να αλλάξει.

## Προτεινόμενη τελική μορφή route

Το παρακάτω είναι ενδεικτικό και όχι έτοιμο copy/paste implementation:

```kotlin
fun Route.customersRoute(
    getCustomersUseCase: GetCustomersUseCase,
    createCustomerUseCase: CreateCustomerUseCase,
    updateCustomerUseCase: UpdateCustomerUseCase,
    deleteCustomerUseCase: DeleteCustomerUseCase,
    contextResolver: AuthenticatedRequestContextResolver
) {
    authenticate("auth-jwt-v2") {
        get("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val ctx = contextResolver.resolve(principal)
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            call.respond(getCustomersUseCase.execute(ctx.toRequestContext()))
        }

        post("/customers") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val ctx = contextResolver.resolve(principal)
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            if (ctx.role !in setOf("owner", "admin")) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }

            val request = call.receive<CreateCustomerRequest>()
            val result = createCustomerUseCase.execute(
                ctx.toRequestContext(),
                request
            )
            call.respond(HttpStatusCode.Created, result)
        }

        // PUT και DELETE με το ίδιο context resolution και authorization policy.
    }
}
```

## Client-side αλλαγές

Μετά από επιτυχημένο `/auth/v2/login`, ο client πρέπει να στέλνει το access token
σε κάθε customers request:

```http
Authorization: Bearer <accessToken>
```

Το `username` του login response χρησιμοποιείται μόνο για παρουσίαση στο UI.
Δεν πρέπει να στέλνεται ως απόδειξη ταυτότητας σε custom header ή request body.

Όταν το API επιστρέψει `401` επειδή έληξε το access token, ο client πρέπει:

1. Να καλέσει το `/auth/v2/refresh` με το τρέχον refresh token.
2. Να αποθηκεύσει και τα δύο νέα tokens.
3. Να επαναλάβει το αρχικό customers request μία φορά.
4. Αν αποτύχει και το refresh, να διαγράψει τα tokens και να επιστρέψει στο
   login.

## Tests που πρέπει να προστεθούν

Πριν αντικατασταθεί το legacy authentication, χρειάζονται τουλάχιστον οι
παρακάτω έλεγχοι:

- Έγκυρο V2 access token επιστρέφει customers μόνο από τη σωστή tenant βάση.
- Token εταιρείας Α δεν αποκτά πρόσβαση σε customers εταιρείας Β.
- Αλλαγμένο ή ανύπαρκτο `tenant_id` απορρίπτεται.
- User που δεν ανήκει στο `tenant_id` απορρίπτεται.
- User με `status != active` απορρίπτεται, ακόμη και αν το JWT δεν έχει λήξει.
- Expired, malformed ή invalid-signature token επιστρέφει `401`.
- User χωρίς τον απαιτούμενο ρόλο παίρνει `403` σε create/update/delete.
- Επιτρεπόμενος ρόλος μπορεί να εκτελέσει create/update/delete.
- Τα GET/POST/PUT/DELETE δεν δέχονται legacy token μετά την ολοκλήρωση της
  μετάβασης.

## Προτεινόμενη σειρά υλοποίησης

1. Δημιουργία `AuthenticatedRequestContextResolver`.
2. Δημιουργία ή επέκταση του ασφαλούς request context.
3. Προσθήκη resolver στο `AppContainer`.
4. Προσθήκη resolver ως dependency στο `customersRoute`.
5. Αλλαγή του provider σε `auth-jwt-v2`.
6. Αντικατάσταση όλων των legacy claim lookups.
7. Προσθήκη role-based authorization.
8. Προσθήκη integration και cross-tenant tests.
9. Χειροκίνητος έλεγχος login, refresh και όλων των customers endpoints.

Μόνο αφού ολοκληρωθούν οι παραπάνω έλεγχοι πρέπει να θεωρηθεί ότι το customers
feature έχει μεταφερθεί πλήρως στο Authentication V2.
