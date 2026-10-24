# Ενσωμάτωση του Customers flow με το `/auth/v2/login`

## Σύντομη απάντηση

Το `CustomersRoute.kt` δεν πρέπει απλώς να αλλάξει από `auth-jwt` σε
`auth-jwt-v2`. Το V2 access token δεν περιέχει `databaseName` ή `username`.
Περιέχει `sub` (user ID), `tenant_id` (company ID) και `role`.

Η σωστή ροή είναι:

```text
POST /auth/v2/login
        |
        v
accessToken (sub, tenant_id, role)
        |
        v
Authorization: Bearer <accessToken>
        |
        v
auth-jwt-v2 -> server-side context resolver
        |
        v
central DB: user + company + database_name
        |
        v
RequestContext -> customer use case -> σωστή tenant DB
```

## Τι κάνει ο client

### 1. Login

```http
POST /auth/v2/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

Κρατά το `accessToken` μόνο στη μνήμη της εφαρμογής. Το `refreshToken` τίθεται
αυτόματα από τον backend ως `HttpOnly` cookie και δεν είναι προσβάσιμο από JavaScript.

### 2. Κλήση Customers endpoint

```http
GET /customers
Authorization: Bearer <accessToken>
```

Το ίδιο header στέλνεται σε `POST`, `PUT` και `DELETE`. Ο client δεν στέλνει
`databaseName`, `tenant_id`, `companyId`, `role` ή `username` ως στοιχεία
εξουσιοδότησης. Η ταυτότητα προκύπτει αποκλειστικά από το επαληθευμένο JWT και
την κεντρική βάση.

### 3. Ανανέωση token

Σε `401` λόγω ληγμένου access token, ο client καλεί:

```http
POST /auth/v2/refresh
```

Ο browser στέλνει αυτόματα το refresh cookie. Ο client κρατά το νέο access token
στη μνήμη και επαναλαμβάνει το αρχικό customers request μία φορά. Αν αποτύχει
και το refresh, καθαρίζει τη session και επιστρέφει στο
login. Το `403` δεν ενεργοποιεί refresh: σημαίνει ότι ο χρήστης είναι
authenticated αλλά δεν έχει το απαιτούμενο permission.

## Τι πρέπει να αλλάξει στο backend

### 1. Δημιουργία authenticated context resolver

Χρειάζεται κοινό component που:

1. Διαβάζει `sub`, `tenant_id` και `role` από το `JWTPrincipal`.
2. Αναζητά στην `DatabaseFactory.centralDb` τον ενεργό χρήστη με ID `sub`.
3. Ελέγχει ότι `users.company_id == tenant_id`.
4. Αναζητά το `companies.database_name` με βάση το `tenant_id`.
5. Επιστρέφει context με τουλάχιστον `dbName`, `username`, `userId`,
   `companyId` και `role`.

Το `databaseName` πρέπει να προέρχεται μόνο από το `CompanyTable` και ποτέ από
claim ή request του client.

Ενδεικτικό contract:

```kotlin
data class AuthenticatedRequestContext(
    val dbName: String,
    val username: String,
    val userId: Int,
    val companyId: Int,
    val role: String
) {
    fun toRequestContext() = RequestContext(
        dbName = dbName,
        username = username
    )
}

interface AuthenticatedRequestContextResolver {
    fun resolve(principal: JWTPrincipal): AuthenticatedRequestContext?
}
```

Ο resolver πρέπει να απορρίπτει αν λείπει claim, αν ο user δεν είναι ενεργός,
αν δεν υπάρχει company/database ή αν user και token ανήκουν σε διαφορετική
εταιρεία.

### 2. Αλλαγή του `CustomersRoute`

Η υπογραφή αποκτά τον resolver και το authentication provider αλλάζει:

```kotlin
fun Route.customersRoute(
    getCustomersUseCase: GetCustomersUseCase,
    createCustomerUseCase: CreateCustomerUseCase,
    updateCustomerUseCase: UpdateCustomerUseCase,
    deleteCustomerUseCase: DeleteCustomerUseCase,
    contextResolver: AuthenticatedRequestContextResolver
) {
    authenticate("auth-jwt-v2") {
        // GET, POST, PUT, DELETE
    }
}
```

Σε κάθε endpoint αντικαθίσταται η ανάγνωση των legacy claims:

```kotlin
val dbName = principal.getClaim("databaseName", String::class)
val username = principal.getClaim("username", String::class)
```

με:

```kotlin
val principal = call.principal<JWTPrincipal>()
    ?: return@get call.respond(HttpStatusCode.Unauthorized)

val authenticated = contextResolver.resolve(principal)
    ?: return@get call.respond(HttpStatusCode.Unauthorized)

val ctx = authenticated.toRequestContext()
val result = getCustomersUseCase.execute(ctx)
call.respond(HttpStatusCode.OK, result)
```

Η ίδια επίλυση context εφαρμόζεται σε `POST`, `PUT` και `DELETE` πριν από την
κλήση του αντίστοιχου use case.

### 3. Authorization

Το authentication απαντά στο «ποιος είναι ο χρήστης». Για το «τι επιτρέπεται
να κάνει» χρειάζεται authorization ανά operation:

| Endpoint | Permission |
| --- | --- |
| `GET /customers` | `customers.read` |
| `POST /customers` | `customers.create` |
| `PUT /customers/{id}` | `customers.update` |
| `DELETE /customers/{id}` | `customers.delete` |

Μέχρι να ολοκληρωθεί το permission repository/RBAC migration, μπορεί να
χρησιμοποιηθεί προσωρινός έλεγχος του validated `role`, αλλά όχι role που ήρθε
από request body/header. Missing/invalid authentication επιστρέφει `401`, ενώ
έγκυρος χρήστης χωρίς permission επιστρέφει `403`.

### 4. Dependency injection

- Το `AppContainer` δημιουργεί τον resolver (και αργότερα το permission
  service).
- Το `Application.kt` τον περνά στο `customersRoute`.
- Το `CustomerRepositoryImpl` μπορεί να συνεχίσει να καλεί
  `dbProvider.getDatabase(ctx.dbName)`, αρκεί το `dbName` να έχει παραχθεί μόνο
  από τον server-side resolver.

## Σημερινή κατάσταση του project

Το `/auth/v2/login`, το `/auth/v2/refresh`, ο provider `auth-jwt-v2` και ο
server-side context resolver υπάρχουν. Το `CustomersRoute.kt` χρησιμοποιεί τον
V2 provider και επιλύει το `databaseName` μέσω της central DB.

Μέχρι να ολοκληρωθεί το permission repository/RBAC migration, όλα τα Customers
endpoints επιτρέπονται προσωρινά μόνο όταν το validated role είναι `owner`.

## Προτεινόμενη σειρά υλοποίησης

1. Προσθήκη `AuthenticatedRequestContext` και resolver.
2. Tests του resolver για user/company/status/tenant mismatch.
3. Προσθήκη resolver στο `AppContainer` και στο `customersRoute`.
4. Αλλαγή του route σε `authenticate("auth-jwt-v2")`.
5. Αντικατάσταση των legacy claims και στα τέσσερα endpoints.
6. Προσθήκη permission checks.
7. Tests tenant isolation και `401`/`403`.
8. Χειροκίνητος έλεγχος login, customers requests, refresh και retry.

## Ελάχιστα acceptance tests

- V2 token της εταιρείας Α διαβάζει μόνο customers της εταιρείας Α.
- V2 token της εταιρείας Α δεν μπορεί να οδηγηθεί στη βάση της εταιρείας Β.
- Αλλοιωμένο ή ανύπαρκτο `tenant_id` απορρίπτεται.
- Ανενεργός user απορρίπτεται ακόμη και με μη ληγμένο token.
- Ληγμένο/άκυρο token επιστρέφει `401`.
- Έγκυρο token χωρίς permission επιστρέφει `403`.
- Επιτρεπόμενος χρήστης εκτελεί επιτυχώς GET/POST/PUT/DELETE.
- Μετά τη μετάβαση, legacy token δεν γίνεται δεκτό στα customers endpoints.
