# Security review και checklist για production

## Συμπέρασμα

Το project δεν είναι ακόμη έτοιμο για ασφαλές production deployment. Υπάρχουν κρίσιμα ζητήματα σχετικά με εκτεθειμένα secrets, JWT tokens, logging ευαίσθητων δεδομένων, δημόσια development endpoints, authorization και tenant isolation.

Η προτεινόμενη σειρά εργασιών είναι:

1. Rotation και αφαίρεση όλων των secrets.
2. Αφαίρεση database credentials από JWT και logs.
3. Κατάργηση των seed/init endpoints από το production.
4. Ασφαλές tenant isolation.
5. Role-based authorization.
6. Rate limiting, validation και ασφαλές error handling.
7. Dependency, container και infrastructure hardening.
8. Security tests πριν από το release.

---

## P0 — Κρίσιμα προβλήματα πριν από production

### 1. Rotation και αφαίρεση εκτεθειμένων secrets

Υπάρχουν PostgreSQL credentials και development credentials μέσα σε tracked αρχεία, μεταξύ άλλων στα:

- `src/main/resources/application.yaml`
- `src/main/resources/applicationdev.yaml`
- `src/main/kotlin/com/example/routes/SeedCompanyARoute.kt`
- `src/main/kotlin/com/example/routes/dbcreationRoute.kt`
- `src/main/kotlin/com/example/utils/fun_db_creation.kt`
- PowerShell scripts, README και devcontainer configuration.

Απαιτούμενες ενέργειες:

- [ ] Αλλαγή του PostgreSQL password.
- [ ] Δημιουργία νέου, ισχυρού JWT secret τουλάχιστον 32 τυχαίων bytes.
- [ ] Αφαίρεση πραγματικών secrets από source code, YAML, scripts και documentation.
- [ ] Αποθήκευση secrets σε runtime environment variables ή secret manager.
- [ ] Έλεγχος αν το repository έχει κοινοποιηθεί ή γίνει push σε remote.
- [ ] Καθαρισμός του Git history, εφόσον απαιτείται.
- [ ] Ενεργοποίηση secret scanning στο CI.

Η απλή αφαίρεση ενός secret από το τελευταίο commit δεν είναι αρκετή, επειδή το secret μπορεί να παραμένει στο Git history.

### 2. Αφαίρεση database credentials από τα JWT

Στο `JwtConfig.kt` το token περιλαμβάνει:

- database host,
- database port,
- database username,
- database password.

Τα JWT είναι υπογεγραμμένα, αλλά δεν είναι κρυπτογραφημένα. Ο κάτοχος ενός token μπορεί να διαβάσει όλα τα claims χωρίς να γνωρίζει το JWT secret.

Το JWT πρέπει να περιέχει μόνο μη ευαίσθητα identifiers, για παράδειγμα:

```text
sub=user-id
tenant_id=company-id
role=admin
jti=random-token-id
```

Ο server πρέπει να μετατρέπει εσωτερικά το `tenant_id` σε επιτρεπόμενη database configuration. Το database password δεν πρέπει να φεύγει ποτέ από τον server.

- [ ] Αφαίρεση `databaseHost`, `databasePort`, `databaseUser` και `databasePassword` από το JWT.
- [ ] Χρήση αμετάβλητου user ID στο `sub` αντί για username.
- [ ] Χρήση `tenant_id` αντί για database name.
- [ ] Server-side αντιστοίχιση tenant σε database.
- [ ] Ξεχωριστός, least-privilege DB user ανά tenant, όπου είναι εφικτό.

### 3. Αφαίρεση passwords και tokens από τα logs

Το login και το onboarding καταγράφουν ευαίσθητα δεδομένα, όπως:

- ολόκληρο login request,
- user/database record,
- παραγόμενο JWT,
- bearer tokens στα sync endpoints,
- onboarding response που περιέχει token.

Σχετικά αρχεία:

- `src/main/kotlin/com/example/routes/LoginRouting.kt`
- `src/main/kotlin/com/example/routes/devRoute.kt`
- `src/main/kotlin/com/example/feature/onboarding/routes/OnboardingRoutes.kt`
- `src/main/resources/logback.xml`

Απαιτούμενες ενέργειες:

- [ ] Αφαίρεση όλων των logs που περιέχουν password ή token.
- [ ] Αποφυγή logging ολόκληρων DTOs όταν μπορεί να περιέχουν προσωπικά δεδομένα.
- [ ] Structured logging μόνο με request ID, user ID, tenant ID και event type.
- [ ] Αλλαγή του production root log level από `TRACE` σε `INFO` ή `WARN`.
- [ ] Redaction των headers `Authorization`, `Cookie` και `Set-Cookie`.

### 4. Κατάργηση δημόσιων seed και database-management endpoints

Τα παρακάτω endpoints είναι ενεργά χωρίς authentication:

```text
GET /seed/companyA
GET /sign-up/init-db
```

Αυτά μπορούν να δημιουργήσουν δεδομένα ή tables και δεν πρέπει να υπάρχουν στο production routing.

- [ ] Αφαίρεση των `seedCompanyARoute()` και `dbCreation()` από το production application module.
- [ ] Μεταφορά migrations και seeding σε ξεχωριστό deployment job ή CLI task.
- [ ] Χρήση migration tool, όπως Flyway ή Liquibase.
- [ ] Εκτέλεση migrations με ξεχωριστά και περιορισμένα credentials.

### 5. Περιορισμός του CORS

Στο `Application.kt` χρησιμοποιούνται μαζί:

```kotlin
anyHost()
allowCredentials = true
```

Στο production πρέπει να επιτρέπονται μόνο τα πραγματικά frontend domains:

```kotlin
install(CORS) {
    allowHost("app.example.com", schemes = listOf("https"))
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowCredentials = true
}
```

- [ ] Αφαίρεση του `anyHost()`.
- [ ] Allow-list συγκεκριμένων HTTPS hosts από configuration.
- [ ] Αφαίρεση custom headers και HTTP methods που δεν χρησιμοποιούνται.
- [ ] Ξεχωριστή CORS configuration για development και production.

---

## P1 — Υψηλής προτεραιότητας

### 6. Authorization και ρόλοι

Οι περισσότεροι routes απαιτούν έγκυρο JWT, αλλά δεν εφαρμόζουν role-based permissions. Έτσι, κάθε authenticated χρήστης μπορεί δυνητικά να εκτελέσει ενέργειες που θα έπρεπε να είναι διαθέσιμες μόνο σε admin, όπως δημιουργία χρηστών, αλλαγή settings ή διαγραφές.

Προτεινόμενο αρχικό μοντέλο:

| Ρόλος | Δικαιώματα |
|---|---|
| `owner` | Πλήρης διαχείριση tenant και χρηστών |
| `admin` | Users, settings και επιχειρησιακά δεδομένα |
| `technician` | Επιτρεπόμενες operational ενέργειες |
| `viewer` | Μόνο ανάγνωση |

- [ ] Προσθήκη `role` ή permission claims στο JWT.
- [ ] Server-side έλεγχος ότι ο ρόλος παραμένει ενεργός.
- [ ] Έλεγχος `status=active` σε κάθε authentication/refresh flow.
- [ ] Authorization policy για κάθε write/delete endpoint.
- [ ] Επιστροφή `403 Forbidden` όταν ο χρήστης είναι authenticated αλλά δεν έχει δικαίωμα.
- [ ] Audit log για role changes, user creation και destructive operations.

### 7. JWT hardening και session lifecycle

Χρειάζονται τουλάχιστον τα ακόλουθα claims και checks:

- `sub`: αμετάβλητο user ID,
- `tenant_id`,
- `role` ή permissions,
- `jti`: μοναδικό token ID,
- `iat`: ημερομηνία έκδοσης,
- `exp`: λήξη,
- συγκεκριμένα `iss` και `aud`.

Παρατηρήθηκε επίσης πιθανό typo: ο κώδικας διαβάζει `WT_ISSUER`, ενώ το env χρησιμοποιεί `JWT_ISSUER`.

- [ ] Διόρθωση `WT_ISSUER` σε `JWT_ISSUER`.
- [ ] Fail-fast κατά την εκκίνηση αν λείπει οποιοδήποτε απαραίτητο secret/configuration.
- [ ] Access tokens μικρής διάρκειας, ενδεικτικά 10–15 λεπτά.
- [ ] Refresh tokens με rotation και reuse detection.
- [ ] Δυνατότητα revoke/logout.
- [ ] Έλεγχος issuer, audience, subject και required claims.
- [ ] Αποδοχή μόνο του αναμενόμενου signing algorithm.

Η αποθήκευση ολόκληρων access tokens στο `tokens.json` δεν είναι κατάλληλη για production επειδή:

- αποθηκεύει bearer tokens σε plaintext,
- δημιουργεί race conditions,
- δεν λειτουργεί σωστά με πολλαπλά application instances,
- δεν παρέχει ασφαλές distributed revocation.

Για revocation μπορεί να αποθηκεύεται μόνο το `jti` ή hash refresh token σε Redis/database με expiration.

### 8. Rate limiting και brute-force protection

Τα `/auth/login` και `/onboarding/register` δεν έχουν rate limiting.

- [ ] Rate limit ανά IP και username.
- [ ] Προοδευτικό backoff ή προσωρινό account lockout.
- [ ] Ίδιο γενικό μήνυμα για λάθος username και password.
- [ ] Email verification για δημόσιο registration.
- [ ] CAPTCHA μετά από επαναλαμβανόμενες αποτυχημένες προσπάθειες, αν χρειάζεται.
- [ ] Audit events και alerts για ύποπτη δραστηριότητα.

### 9. Tenant isolation

Το `databaseName` διαβάζεται από το JWT και χρησιμοποιείται για δημιουργία JDBC URL. Η ασφαλέστερη ροή είναι:

```text
JWT tenant_id
      ↓
server-side tenant registry
      ↓
validated database configuration
      ↓
connection pool του συγκεκριμένου tenant
```

- [ ] Να μην γίνεται αποδεκτό arbitrary database name/host/user από claims ή request input.
- [ ] Allow-list των tenant databases.
- [ ] Επιβεβαίωση ότι ο χρήστης ανήκει στο `tenant_id`.
- [ ] Database permissions που αποτρέπουν πρόσβαση σε άλλους tenants.
- [ ] Connection pool αντί για νέο `Database.connect()` ανά request.
- [ ] Tests που επιχειρούν cross-tenant πρόσβαση.

### 10. Ασφαλές password handling

Το κύριο login χρησιμοποιεί BCrypt, που είναι σωστή βάση, αλλά υπάρχει παλαιό `AuthenticationService` με απλή σύγκριση plaintext password.

- [ ] Αφαίρεση ή διόρθωση του παλιού plaintext authentication code.
- [ ] BCrypt cost τουλάχιστον 12, με επανεκτίμηση βάσει production hardware.
- [ ] Password policy με έλεγχο μήκους και breached-password screening.
- [ ] Μέγιστο μήκος request password για προστασία από resource exhaustion.
- [ ] Password reset tokens μίας χρήσης, μικρής διάρκειας και αποθηκευμένα hashed.

---

## P2 — Production hardening

### 11. Input validation και ασφαλές error handling

- [ ] Εγκατάσταση Ktor `RequestValidation`.
- [ ] Όρια μήκους για strings και IDs.
- [ ] Έλεγχος email, enums, ημερομηνιών και αριθμητικών ορίων.
- [ ] Μέγιστο request body size, ιδιαίτερα στα bulk/sync endpoints.
- [ ] Απόρριψη άγνωστων ή μη υποστηριζόμενων entity names.
- [ ] Εγκατάσταση `StatusPages` για ελεγχόμενα error responses.
- [ ] Να μην επιστρέφονται stack traces, SQL messages ή database connection details.
- [ ] `400` για invalid input, `401` για missing/invalid authentication και `403` για insufficient permissions.

Η δημιουργία database στο `fun_db_creation.kt` περιλαμβάνει SQL string interpolation. Τα database identifiers δεν μπορούν να περάσουν σαν απλές prepared-statement parameters, οπότε χρειάζεται αυστηρή allow-list, για παράδειγμα μόνο lowercase γράμματα, αριθμοί και underscore.

### 12. Dependency security

Το project χρησιμοποιεί Ktor `3.0.1`, αλλά δηλώνει χειροκίνητα `ktor-server-auth-jwt:2.0.0`. Όλα τα Ktor modules πρέπει να είναι στην ίδια, υποστηριζόμενη έκδοση.

- [ ] Ευθυγράμμιση όλων των Ktor dependencies στην ίδια έκδοση.
- [ ] Αναβάθμιση παλιών Gson, Jackson, Exposed και λοιπών dependencies μετά από tests.
- [ ] Αφαίρεση dependencies που δεν χρησιμοποιούνται.
- [ ] Gradle dependency locking.
- [ ] Dependabot ή Renovate.
- [ ] OWASP Dependency-Check, Snyk ή αντίστοιχο dependency scanning.
- [ ] CodeQL ή άλλο SAST στο CI.

### 13. CI/CD secrets

Το production env file δημιουργείται μέσα στο Docker build context και το Dockerfile εκτελεί `COPY . .`. Production secrets δεν πρέπει να περνούν ως Docker build context, build arguments ή image layers.

- [ ] Τα secrets να εισάγονται μόνο κατά το runtime.
- [ ] Χρήση GitHub Environments και protected production approvals.
- [ ] Ελάχιστα permissions στο `GITHUB_TOKEN`.
- [ ] Pinning των GitHub Actions σε commit SHA για υψηλότερη ασφάλεια supply chain.
- [ ] Image scanning πριν από push/deploy.
- [ ] Μοναδικά, immutable image tags αντί για αποκλειστική χρήση του `latest`.

### 14. Docker hardening

- [ ] Εκτέλεση της εφαρμογής ως non-root user.
- [ ] Read-only root filesystem όπου είναι δυνατό.
- [ ] Writable προσωρινός φάκελος μόνο όπου χρειάζεται.
- [ ] Αφαίρεση compilers και build tools από το runtime image.
- [ ] Pinned base image version ή digest.
- [ ] Health/readiness endpoints χωρίς αποκάλυψη internals.
- [ ] CPU και memory limits.
- [ ] Αφαίρεση του `tokens.json` από το runtime design.

### 15. TLS και HTTP security headers

- [ ] HTTPS μόνο, μέσω load balancer ή reverse proxy.
- [ ] Redirect HTTP σε HTTPS.
- [ ] HSTS μετά την επιβεβαίωση ότι όλα λειτουργούν αποκλειστικά μέσω HTTPS.
- [ ] `Content-Security-Policy` για το frontend.
- [ ] `X-Content-Type-Options: nosniff`.
- [ ] `Referrer-Policy`.
- [ ] `frame-ancestors` μέσω CSP για clickjacking protection.
- [ ] Ασφαλής διαχείριση forwarded headers μόνο από trusted proxies.

### 16. Database και infrastructure

- [ ] Η PostgreSQL να μην είναι δημόσια προσβάσιμη από το Internet.
- [ ] TLS για εφαρμογή προς PostgreSQL.
- [ ] Least-privilege DB users.
- [ ] Άλλος DB user για migrations και άλλος για την εφαρμογή.
- [ ] Automated encrypted backups.
- [ ] Τακτικά restore tests.
- [ ] Rotation database credentials.
- [ ] Monitoring για αποτυχημένα logins, αυξημένα 5xx και ύποπτες database ενέργειες.

---

## Security tests πριν από το release

### Authentication

- [ ] Missing JWT επιστρέφει `401`.
- [ ] Invalid signature επιστρέφει `401`.
- [ ] Expired token επιστρέφει `401`.
- [ ] Λάθος issuer ή audience επιστρέφει `401`.
- [ ] Disabled user δεν μπορεί να χρησιμοποιήσει παλιό token.
- [ ] Logout/revocation λειτουργεί όπως έχει σχεδιαστεί.

### Authorization

- [ ] Viewer δεν μπορεί να εκτελέσει create/update/delete.
- [ ] Technician δεν μπορεί να διαχειριστεί users/settings χωρίς permission.
- [ ] Admin-only endpoints επιστρέφουν `403` σε απλό χρήστη.
- [ ] Κάθε destructive operation έχει audit event.

### Tenant isolation

- [ ] User του tenant A δεν μπορεί να διαβάσει δεδομένα tenant B.
- [ ] User του tenant A δεν μπορεί να γράψει ή να διαγράψει δεδομένα tenant B.
- [ ] Τροποποιημένο `tenant_id` ή database name απορρίπτεται.
- [ ] IDs άλλου tenant δεν προκαλούν data leakage.

### Abuse και validation

- [ ] Brute-force login ενεργοποιεί rate limit/backoff.
- [ ] Oversized JSON απορρίπτεται.
- [ ] Πολύ μεγάλα strings απορρίπτονται.
- [ ] Invalid IDs και dates δεν προκαλούν `500`.
- [ ] Unsupported sync entity απορρίπτεται με ασφαλές error.
- [ ] Error responses δεν περιέχουν stack trace, SQL ή secrets.

### Deployment

- [ ] Το image δεν περιέχει `.env`, passwords ή tokens.
- [ ] Το container τρέχει ως non-root.
- [ ] Η production database δεν είναι public.
- [ ] Τα backups έχουν δοκιμαστεί με πραγματικό restore.
- [ ] Dependency, secret και container scans ολοκληρώνονται επιτυχώς.

---

## Ελάχιστο production release gate

Δεν πρέπει να γίνει production release μέχρι να ισχύουν όλα τα παρακάτω:

- [ ] Όλα τα εκτεθειμένα secrets έχουν γίνει rotate.
- [ ] Κανένα DB credential δεν βρίσκεται σε JWT, logs, image ή source code.
- [ ] Τα seed/init endpoints δεν υπάρχουν στο production.
- [ ] Το CORS επιτρέπει μόνο τα production domains.
- [ ] Υπάρχει RBAC για write/delete/admin operations.
- [ ] Υπάρχει ασφαλές tenant isolation με tests.
- [ ] Login και onboarding έχουν rate limiting.
- [ ] Τα errors δεν διαρρέουν internals.
- [ ] Όλες οι dependencies έχουν ελεγχθεί και οι Ktor εκδόσεις είναι ευθυγραμμισμένες.
- [ ] Η εφαρμογή και η PostgreSQL λειτουργούν μέσω ασφαλούς production infrastructure.
- [ ] Έχουν περάσει authentication, authorization και cross-tenant security tests.

