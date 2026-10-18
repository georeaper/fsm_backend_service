# Authentication V2

Το συγκεκριμένο feature περιέχει τη νέα, ασφαλέστερη υλοποίηση authentication.
Λειτουργεί παράλληλα με το προηγούμενο login και δεν αλλάζει τα υπάρχοντα endpoints
ή τα features που χρησιμοποιούν το authentication provider `auth-jwt`.

## Endpoints

Η νέα ροή εκτίθεται κάτω από το `/auth/v2`.

### Login

```http
POST /auth/v2/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "password"
}
```

Επιτυχής απάντηση:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "username": "admin",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Πιθανά status codes:

- `200 OK`: επιτυχές login.
- `400 Bad Request`: λανθασμένο request body.
- `401 Unauthorized`: λανθασμένα credentials ή μη ενεργός χρήστης.
- `429 Too Many Requests`: υπέρβαση του ορίου αποτυχημένων προσπαθειών.

### Refresh

```http
POST /auth/v2/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

Με κάθε επιτυχημένο refresh:

1. Το παλιό refresh token ανακαλείται.
2. Δημιουργείται νέο access token.
3. Δημιουργείται νέο refresh token.
4. Στη βάση αποθηκεύεται μόνο το SHA-256 hash του νέου refresh token.

Αν χρησιμοποιηθεί ξανά ένα ήδη ανακλημένο refresh token, θεωρείται πιθανή κλοπή
token και ανακαλούνται όλες οι ενεργές συνεδρίες του συγκεκριμένου χρήστη.

### Logout

```http
POST /auth/v2/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

Το logout είναι idempotent και επιστρέφει:

```json
{
  "success": true
}
```

## Δομή feature

```text
feature/auth/
├── AuthRoute.kt
├── README.md
├── dto/
│   └── AuthDtos.kt
├── repository/
│   ├── AuthRepository.kt
│   └── AuthRepositoryImpl.kt
└── usecase/
    └── AuthUseCase.kt
```

### `AuthRoute.kt`

Δηλώνει τα HTTP endpoints και μετατρέπει τα αποτελέσματα του use case σε HTTP
responses. Δεν καταγράφει passwords, access tokens ή refresh tokens στα logs.

Εφαρμόζει επίσης rate limiting στο login ανά συνδυασμό IP και username:

- έως 5 αποτυχημένες προσπάθειες,
- μέσα σε χρονικό παράθυρο 15 λεπτών,
- reset μετά από επιτυχημένο login.

Το rate limiter είναι in-memory. Σε deployment με περισσότερα από ένα backend
instances πρέπει να αντικατασταθεί από κοινό storage, όπως Redis.

### `dto/AuthDtos.kt`

Περιέχει τα serializable request και response models:

- `LoginV2Request`
- `RefreshTokenRequest`
- `LogoutRequest`
- `TokenPairResponse`
- `AuthErrorResponse`
- `LogoutResponse`

### `repository/AuthRepository.kt`

Ορίζει το contract για:

- αναζήτηση ενεργού χρήστη,
- δημιουργία authentication session,
- ασφαλές refresh-token rotation,
- ανάκληση session κατά το logout.

### `repository/AuthRepositoryImpl.kt`

Χρησιμοποιεί την κεντρική βάση μέσω του `DatabaseFactory.centralDb` και τους
πίνακες:

- `UserTable`
- `CompanyTable`
- `AuthSessionTable`

Οι λειτουργίες rotation και reuse detection εκτελούνται μέσα σε database
transaction. Η εγγραφή της session κλειδώνεται με `FOR UPDATE`, ώστε δύο
ταυτόχρονα refresh requests να μην μπορούν να χρησιμοποιήσουν επιτυχώς το ίδιο
refresh token.

### `usecase/AuthUseCase.kt`

Περιέχει την επιχειρησιακή λογική για login, refresh και logout:

- κάνει normalization και validation του input,
- επιβεβαιώνει ότι ο χρήστης είναι ενεργός,
- επαληθεύει BCrypt password hashes,
- δημιουργεί access και refresh tokens,
- αποθηκεύει μόνο hashes των refresh tokens.

Η απάντηση του login και του refresh περιλαμβάνει επίσης το `username`, ώστε ο
client να μπορεί να εμφανίζει τον συνδεδεμένο χρήστη χωρίς πρόσθετο request.

## Security infrastructure

Τα κοινά security utilities βρίσκονται στο
`com.example.infrastructure.security`:

- `PasswordVerifier`: BCrypt verification και dummy BCrypt check για να
  περιορίζονται timing differences μεταξύ λάθος password και ανύπαρκτου χρήστη.
- `RefreshTokenHasher`: SHA-256 hashing πριν από την αποθήκευση refresh token.
- `LoginAttemptLimiter`: προστασία από brute-force login attempts.

## Access token

Το access token δημιουργείται από το `JwtConfigV2` και λήγει σε 15 λεπτά.
Περιλαμβάνει μόνο τα παρακάτω claims:

- `sub`: user ID,
- `tenant_id`: company ID,
- `role`: ρόλος χρήστη,
- `jti`: μοναδικό token ID,
- `iat`: χρόνος έκδοσης,
- `exp`: χρόνος λήξης,
- `iss` και `aud`.

Δεν περιλαμβάνει database hostname, username, password ή άλλα tenant database
credentials.

Για την υπογραφή απαιτούνται:

- `JWT_SECRET`, τουλάχιστον 32 bytes,
- `JWT_ISSUER`,
- `JWT_AUDIENCE`.

Οι τιμές διαβάζονται πρώτα από environment variables και, για local development,
από το αντίστοιχο dotenv αρχείο του project.

## Refresh sessions

Τα refresh sessions αποθηκεύονται στον πίνακα `auth_sessions` και λήγουν μετά
από 30 ημέρες. Η βάση αποθηκεύει:

- session UUID,
- user ID,
- SHA-256 refresh-token hash,
- ημερομηνία δημιουργίας και λήξης,
- ημερομηνία ανάκλησης,
- ημερομηνία τελευταίας χρήσης.

Το πραγματικό refresh token επιστρέφεται μόνο στον client και δεν αποθηκεύεται
σε plaintext στη βάση ή στα logs.

## Authentication provider

Στο Ktor έχει προστεθεί ξεχωριστός provider με όνομα `auth-jwt-v2`.
Τα υπάρχοντα features συνεχίζουν να χρησιμοποιούν το `auth-jwt` μέχρι να γίνει
η ελεγχόμενη μετάβασή τους.

Ένα feature μπορεί αργότερα να μεταφερθεί στη νέα επαλήθευση ως εξής:

```kotlin
authenticate("auth-jwt-v2") {
    // Protected routes
}
```

Η αλλαγή του provider από μόνη της δεν ολοκληρώνει το tenant isolation. Κατά τη
μελλοντική μετάβαση, το feature πρέπει να χρησιμοποιεί το `tenant_id` για να
αναζητά server-side τα στοιχεία της tenant βάσης και όχι να περιμένει database
credentials μέσα στο JWT.

## Έλεγχοι

Προστέθηκαν tests για:

- deterministic SHA-256 refresh-token hashing,
- σωστή και λανθασμένη BCrypt επαλήθευση,
- ασφαλή απόρριψη malformed BCrypt hash,
- dummy verification όταν ο χρήστης δεν υπάρχει,
- login attempt blocking,
- reset του rate limiter,
- λήξη του rate-limit window.

Ο έλεγχος εκτελείται με:

```powershell
.\gradlew.bat test
```

## Συμβατότητα με το παλιό login

Παραμένουν διαθέσιμα χωρίς αλλαγή:

- το παλιό `/auth/login`,
- το παλιό `JwtConfig`,
- ο provider `auth-jwt`,
- τα features που βασίζονται στα παλιά JWT claims.

Η αφαίρεση του legacy authentication πρέπει να γίνει μόνο αφού ελεγχθεί η νέα
ροή και μεταφερθούν σταδιακά όλα τα προστατευμένα features.
