# RBAC Tables και επόμενα βήματα

## Τι δημιουργήθηκε

Προστέθηκαν τέσσερις πίνακες στην κεντρική authentication βάση για την
υλοποίηση Role-Based Access Control (RBAC):

```text
companies
    └── roles
          ├── role_permissions ── permissions
          └── user_roles ──────── users
```

Οι πίνακες δημιουργούνται από το `DatabaseFactory.ensureTables()` μαζί με τους
υπάρχοντες authentication πίνακες.

### `roles`

Κάθε role ανήκει υποχρεωτικά σε μία εταιρεία.

Πεδία:

- `id`: primary key.
- `company_id`: foreign key προς `companies.id`.
- `name`: όνομα role, για παράδειγμα `owner`, `admin`, `technician`.
- `description`: προαιρετική περιγραφή.
- `is_system`: δείχνει αν είναι προκαθορισμένο role της εφαρμογής.
- `created_at`, `updated_at`: timestamps.

Ο συνδυασμός `company_id` και `name` είναι unique. Επομένως μία εταιρεία δεν
μπορεί να έχει δύο roles με το ίδιο όνομα, αλλά διαφορετικές εταιρείες μπορούν
να χρησιμοποιούν τα ίδια role names.

Αν διαγραφεί μία εταιρεία, διαγράφονται με cascade και τα roles της.

### `permissions`

Αποτελεί τον κοινό κατάλογο ενεργειών που υποστηρίζει το backend.

Πεδία:

- `id`: primary key.
- `code`: μοναδικό permission code.
- `description`: προαιρετική περιγραφή.
- `created_at`: timestamp.

Προτεινόμενη μορφή permission code:

```text
{feature}.{action}
```

Παραδείγματα:

```text
customers.read
customers.create
customers.update
customers.delete
users.read
users.create
users.update
users.delete
settings.read
settings.manage
```

Τα permissions είναι global definitions. Δεν περιέχουν `company_id`, επειδή το
ίδιο permission code έχει την ίδια σημασία σε όλες τις εταιρείες.

### `role_permissions`

Many-to-many σύνδεση μεταξύ roles και permissions.

Πεδία:

- `role_id`: foreign key προς `roles.id`.
- `permission_id`: foreign key προς `permissions.id`.
- `created_at`: timestamp.

Το composite primary key `(role_id, permission_id)` αποτρέπει διπλές αναθέσεις.
Η διαγραφή role ή permission διαγράφει αυτόματα τις αντίστοιχες συνδέσεις.

### `user_roles`

Many-to-many σύνδεση μεταξύ users και roles.

Πεδία:

- `user_id`: foreign key προς `users.id`.
- `role_id`: foreign key προς `roles.id`.
- `created_at`: timestamp.

Το composite primary key `(user_id, role_id)` αποτρέπει διπλές αναθέσεις.
Η διαγραφή user ή role διαγράφει αυτόματα τις αντίστοιχες συνδέσεις.

## Απόφαση για tenant isolation

Τα roles είναι πάντα company-scoped. Δεν υπάρχουν global roles με
`company_id = null`.

Κατά τη δημιουργία μιας εταιρείας θα δημιουργούνται τα system roles της, όπως:

- `owner`
- `admin`
- `user`

Αυτό απλοποιεί το tenant isolation και επιτρέπει σε κάθε εταιρεία να αλλάζει τα
permissions των roles της χωρίς να επηρεάζει άλλες εταιρείες.

## Σημαντικός κανόνας ακεραιότητας

Η βάση εξασφαλίζει ότι το `user_roles.user_id` και το `user_roles.role_id`
υπάρχουν, αλλά δεν μπορεί με τα υπάρχοντα απλά foreign keys να εξασφαλίσει ότι ο
user και το role ανήκουν στην ίδια εταιρεία.

Το repository/service που θα κάνει role assignment πρέπει υποχρεωτικά να
ελέγχει:

```text
users.company_id == roles.company_id
```

Η ανάθεση role άλλης εταιρείας πρέπει να απορρίπτεται μέσα στο ίδιο database
transaction.

## Backward compatibility

Το υπάρχον πεδίο `UserTable.role` παραμένει προσωρινά ενεργό. Δεν πρέπει να
αφαιρεθεί ακόμη, επειδή χρησιμοποιείται από το νέο login για το `role` claim και
ενδέχεται να χρησιμοποιείται από legacy λειτουργίες.

Κατά τη μετάβαση:

1. Δημιουργούνται τα νέα roles και permissions.
2. Μεταφέρονται οι τιμές του `users.role` στα `roles` και `user_roles`.
3. Τα authorization checks αρχίζουν να χρησιμοποιούν permissions.
4. Το JWT και το login παύουν να βασίζονται στο `users.role`.
5. Το παλιό πεδίο αφαιρείται μόνο σε ξεχωριστό migration, αφού ολοκληρωθούν οι
   έλεγχοι.

## Επόμενο βήμα 1: Permission catalog

Να δημιουργηθεί κεντρική λίστα permission constants, ώστε να αποφεύγονται
τυπογραφικά λάθη:

```kotlin
object Permissions {
    const val CUSTOMERS_READ = "customers.read"
    const val CUSTOMERS_CREATE = "customers.create"
    const val CUSTOMERS_UPDATE = "customers.update"
    const val CUSTOMERS_DELETE = "customers.delete"
}
```

Η λίστα πρέπει να επεκταθεί για κάθε feature πριν μεταφερθεί στο νέο
authorization.

## Επόμενο βήμα 2: Seed permissions και system roles

Χρειάζεται idempotent seeding που θα:

1. Δημιουργεί όσα permission codes λείπουν.
2. Δημιουργεί `owner`, `admin` και `user` για κάθε εταιρεία.
3. Αναθέτει permissions στα system roles.
4. Συνδέει τον αρχικό administrator της εταιρείας με το `owner` role.

Το seeding δεν πρέπει να διαγράφει custom roles ή assignments.

Ενδεικτική αρχική πολιτική:

| Role | Permissions |
| --- | --- |
| `owner` | Όλα τα permissions της εταιρείας |
| `admin` | Όλα εκτός από ownership/billing operations |
| `user` | Read permissions και όσα write permissions αποφασιστούν |

Οι ακριβείς κανόνες πρέπει να οριστούν πριν γραφτεί ο seeder.

## Επόμενο βήμα 3: Onboarding integration

Το onboarding μιας νέας εταιρείας πρέπει, μέσα στην κεντρική database
transaction, να:

1. Δημιουργεί την εταιρεία.
2. Δημιουργεί τον owner user.
3. Δημιουργεί τα system roles για τη νέα εταιρεία.
4. Αναθέτει τα κατάλληλα permissions.
5. Συνδέει τον owner user με το `owner` role στο `user_roles`.

Αν κάποιο βήμα αποτύχει, πρέπει να γίνεται rollback ολόκληρη η authentication
εγγραφή της εταιρείας.

## Επόμενο βήμα 4: Migration υπαρχόντων users

Χρειάζεται idempotent data migration για τα υπάρχοντα δεδομένα:

1. Εύρεση όλων των εταιρειών.
2. Δημιουργία των system roles ανά εταιρεία.
3. Αντιστοίχιση κάθε `users.role` στο αντίστοιχο νέο role.
4. Δημιουργία εγγραφής στο `user_roles`.
5. Αναφορά άγνωστων legacy role values χωρίς σιωπηρή μετατροπή.

Πριν αφαιρεθεί το legacy field πρέπει να επιβεβαιωθεί ότι κάθε ενεργός user έχει
τουλάχιστον ένα role.

## Επόμενο βήμα 5: Authorization repository/service

Να δημιουργηθεί repository που μπορεί να απαντήσει:

```kotlin
fun getPermissions(userId: Int, companyId: Int): Set<String>
fun hasPermission(userId: Int, companyId: Int, permission: String): Boolean
fun assignRole(userId: Int, roleId: Int)
fun revokeRole(userId: Int, roleId: Int)
```

Πάνω από το repository να δημιουργηθεί `AuthorizationService` ή use case που θα
εφαρμόζει tenant validation και θα επιστρέφει `403 Forbidden` όταν ο χρήστης δεν
έχει permission.

## Επόμενο βήμα 6: JWT και request context

Το access token δεν χρειάζεται να περιλαμβάνει ολόκληρη τη λίστα permissions.
Τα permissions μπορεί να αλλάξουν ενώ το token είναι ακόμη ενεργό.

Προτεινόμενα JWT claims:

```json
{
  "sub": "user-id",
  "tenant_id": 1
}
```

Ο authenticated request resolver θα φορτώνει server-side:

- τον ενεργό user,
- την εταιρεία του,
- τα roles του,
- τα permissions του,
- το tenant database name.

Μπορεί αργότερα να προστεθεί μικρής διάρκειας cache, με invalidation όταν αλλάζει
role ή permission assignment.

## Επόμενο βήμα 7: Customers migration

Το `CustomersRoute` πρέπει να ελέγχει:

| Endpoint | Permission |
| --- | --- |
| `GET /customers` | `customers.read` |
| `POST /customers` | `customers.create` |
| `PUT /customers/{id}` | `customers.update` |
| `DELETE /customers/{id}` | `customers.delete` |

Οι υπόλοιπες λεπτομέρειες της μετάβασης βρίσκονται στο:

```text
feature/customers/AUTH_V2_MIGRATION.md
```

## Tests που απαιτούνται

- Δεν επιτρέπεται duplicate role name στην ίδια εταιρεία.
- Επιτρέπεται το ίδιο role name σε διαφορετικές εταιρείες.
- Δεν επιτρέπεται duplicate permission code.
- Δεν επιτρέπεται duplicate role-permission assignment.
- Δεν επιτρέπεται duplicate user-role assignment.
- Το service απορρίπτει role assignment μεταξύ διαφορετικών εταιρειών.
- Η αφαίρεση role αφαιρεί τις συνδέσεις, όχι τον user ή το permission.
- Η αφαίρεση user αφαιρεί τα `user_roles` και τα auth sessions του.
- User χωρίς permission παίρνει `403`.
- User με permission μπορεί να εκτελέσει την αντίστοιχη ενέργεια.
- Αλλαγή permissions εφαρμόζεται χωρίς να απαιτείται νέο access token ή αφού
  λήξει το επιλεγμένο cache TTL.

## Προτεινόμενη σειρά συνέχειας

1. Ορισμός του πλήρους permission catalog.
2. Υλοποίηση idempotent seeder.
3. Ενσωμάτωση system-role creation στο onboarding.
4. Migration των υπαρχόντων users.
5. Υλοποίηση authorization repository και service.
6. Υλοποίηση authenticated request context resolver.
7. Μετάβαση του Customers feature.
8. Μετάβαση των υπόλοιπων features ένα-ένα.
9. Αφαίρεση του legacy `users.role` μόνο στο τέλος.
