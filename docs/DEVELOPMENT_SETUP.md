# FSM Backend — Development Setup

> **Project:** Field Service Management (FSM)  
> **Backend:** Kotlin / Ktor / Exposed ORM  
> **Database:** PostgreSQL (central database and separate tenant databases)  
> **Primary development environment:** Windows + IntelliJ IDEA  
> **Document status:** Development guide — update as the project evolves

## 1. Purpose

This document explains how to prepare a new development PC, configure the FSM backend, and verify that it starts successfully. **Never commit actual passwords, JWT secrets, or production credentials.**

## 2. Prerequisites

Install:

| Software | Version / notes |
|---|---|
| Git | Current supported version |
| IntelliJ IDEA | Version compatible with the project |
| Java Development Kit (JDK) | **Java 17** — currently tested with **Microsoft OpenJDK 17.0.17** |
| PostgreSQL | Version compatible with the existing databases; exact version to be documented |
| Docker Desktop | Optional, for container-based development |
| Postman | Optional, for testing REST endpoints |

The existing IntelliJ configuration uses module **`ktor-sample.main`** and **JRE: Default (17 — SDK of module)**. Java 17 is the *observed working development runtime*; verify the Gradle toolchain and JVM target before treating it as an enforced project requirement.

## 3. Install and configure Java 17

1. Install **Microsoft OpenJDK 17** or another compatible JDK 17 distribution (e.g., Eclipse Temurin 17).
2. Open PowerShell and check:

```powershell
java -version
javac -version
```

3. In IntelliJ, open **File → Project Structure → Project** and select **Project SDK: JDK 17**.
4. Go to **Settings → Build, Execution, Deployment → Build Tools → Gradle** and check the **Gradle JVM**.
5. Open **Run → Edit Configurations → ApplicationKt**:
   - **Use classpath of module:** `ktor-sample.main`
   - **JRE:** `Default (17 — SDK of 'ktor-sample.main' module)`
6. Check the Gradle runtime:

```powershell
.\gradlew.bat --version
```

**Optional after verifying `build.gradle.kts`:** explicitly configure a JVM toolchain so that a new PC does not depend on IDE defaults:

```kotlin
kotlin {
    jvmToolchain(17)
}
```

Do not add this blindly if the current project uses a different target or incompatible plugin configuration.

## 4. Clone and open the backend

```powershell
git clone <YOUR_REPOSITORY_URL>
cd fsm_backend_service
```

Open the project folder in IntelliJ and wait for Gradle sync. Replace `<YOUR_REPOSITORY_URL>` with the actual Git repository URL.

## 5. PostgreSQL and central database

The backend connects to a PostgreSQL central database named **`central_db`**.

**When Ktor runs locally on Windows** and PostgreSQL is reachable on the host:

```text
jdbc:postgresql://localhost:5432/central_db
```

**When Ktor runs inside a Docker Compose network** with a PostgreSQL service named `db`:

```text
jdbc:postgresql://db:5432/central_db
```

These hostnames are not interchangeable: `db` resolves only in the relevant container network. Ensure the database exists and the configured user has appropriate permissions.

> **To document:** PostgreSQL version, database schema initialization/migrations, creation of the central admin account, tenant database provisioning, and demo data setup. Do not assume a fresh empty `central_db` is sufficient to run the application.

## 6. Environment variables

Create a committed **`.env.example`** containing placeholders, **not secrets**:

```dotenv
# Central database — local development
DB_URL=jdbc:postgresql://localhost:5432/central_db
DB_USER=postgres
DB_PASSWORD=CHANGE_ME

# JWT
JWT_SECRET=GENERATE_A_RANDOM_SECRET
JWT_ISSUER=fsm-backend
JWT_AUDIENCE=fsm-users
```

Set real values through **IntelliJ → Run → Edit Configurations → ApplicationKt → Environment variables**. The current application uses environment variables; creating a `.env` file by itself **does not automatically load it** unless a dotenv loader or another explicit mechanism is configured.

### Generate a JWT secret on Windows PowerShell

This works with Windows PowerShell 5.1:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
[Convert]::ToBase64String($bytes)
```

Copy the generated value into the **`JWT_SECRET`** environment variable. Keep it private. Use distinct JWT secrets in development and production.

### Protect local configuration

Ensure `.gitignore` excludes actual secrets, for example:

```gitignore
.env
.env.*
!.env.example
```

Do not enable **Store as project file** for an IntelliJ Run Configuration containing real secrets if that file could be committed. Check tracked files and Git history separately; `.gitignore` does not remove previously committed secrets.

## 7. Ktor `application.yaml`

The working Ktor YAML syntax uses **`$VARIABLE`**, not `${VARIABLE}`:

```yaml
ktor:
  deployment:
    port: 8080
  application:
    modules:
      - com.example.ApplicationKt.module
  database:
    centralDatabase:
      driver: org.postgresql.Driver
      url: "$DB_URL"
      user: "$DB_USER"
      password: "$DB_PASSWORD"
```

In the existing development environment, the `${DB_URL}` form triggered a YAML configuration exception. The `$DB_URL` form worked.

## 8. Start and test the backend

1. Start PostgreSQL.
2. Confirm the `central_db` database and required schema are available.
3. In IntelliJ, select **ApplicationKt**.
4. Confirm JRE 17 and all required environment variables.
5. Click **Run**.
6. Verify the backend starts on **`http://localhost:8080`**.
7. Test authentication using Postman or the React frontend:

```http
POST http://localhost:8080/auth/login
```

Use the login request body required by the current backend DTO. A successful startup alone does not prove that the database connection and authentication work; test an endpoint that queries the central database.

## 9. Docker development notes

If the backend and PostgreSQL run as containers in the same Compose network, the backend JDBC URL should use the database **service name** rather than `localhost`:

```text
DB_URL=jdbc:postgresql://db:5432/central_db
```

Pass secrets **at container runtime**, not during the Docker image build. Do not copy a real `.env` file into the image or include it in the build context. Add it to `.dockerignore` as appropriate.

The exact Compose services, ports, volumes, and commands still need to be verified against the repository before documenting a fully automated Docker setup.

## 10. Troubleshooting

| Symptom | Check |
|---|---|
| `NoSuchElementException` in `YamlConfig.resolveValue` | Use Ktor YAML syntax `"$DB_URL"`, `"$DB_USER"`, `"$DB_PASSWORD"` rather than `${...}`. |
| Missing JWT or DB environment variable | IntelliJ Run Configuration → Environment variables; verify spelling and nonempty values. |
| PostgreSQL connection refused | PostgreSQL running, port 5432, correct host/network and published port. |
| PostgreSQL authentication failed | DB username/password and permissions. |
| Database not found | Ensure `central_db` exists and migrations/initialization have run. |
| Java / Gradle mismatch | Project SDK, Gradle JVM, `java -version`, `gradlew --version`, and Gradle toolchain configuration. |
| JWT verification fails | Matching secret, issuer, audience and token validity; note that changing a secret invalidates older JWTs. |

## 11. Security notes for development

- Do not put PostgreSQL passwords or connection credentials inside JWT claims.
- Do not log passwords, Authorization headers, JWTs or full sensitive DTOs.
- Do not commit secrets or real `.env` files.
- Do not expose development PostgreSQL or seed/init endpoints to the public internet.
- **Before any public deployment**, rotate credentials that have appeared in source code, Git history, screenshots, or other shared material.
- Complete JWT tenant resolution, authorization, rate limiting, safe error handling and security tests before production release.

## 12. Documentation still to complete

- [ ] Verify `build.gradle.kts`: Java toolchain, Kotlin JVM target, Gradle wrapper and Ktor dependencies.
- [ ] Record the tested PostgreSQL version.
- [ ] Document central database schema and initialization.
- [ ] Document tenant database creation and migrations.
- [ ] Document test user / admin onboarding without publishing credentials.
- [ ] Verify Docker Compose development startup.
- [ ] Document the React frontend setup.
- [ ] Document production deployment separately.
- [ ] Keep this guide updated whenever configuration changes.
