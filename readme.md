# 🚀 Ktor Backend (Codespaces Dev Setup)

This project runs inside GitHub Codespaces using a full devcontainer setup with Kotlin Ktor + PostgreSQL + Exposed ORM.

---

## 🧱 Tech Stack

- Kotlin
- Ktor
- PostgreSQL (Docker)
- Exposed ORM
- GitHub Codespaces Dev Containers

---

## 🚀 How to run the project

### 1. Open Codespace
Just open the repo in GitHub Codespaces.

---

### 2. Wait for devcontainer setup
On first startup, the container will:

- Install Java 21
- Run Gradle build
- Create `assets/env.dev` if missing

This is handled automatically by:


.devcontainer/init-env.sh


---

### 3. Start the server

```bash
./gradlew run

Server runs at:

http://0.0.0.0:8080
🌐 Port Forwarding (IMPORTANT)

In Codespaces:

Go to Ports tab
Find port 8080
Set visibility:
❌ Private = not accessible
✅ Public = required to open in browser
Click Open in Browser
🐘 Database (PostgreSQL)

The database runs via Docker inside Codespaces.

Connection config:
DB_HOST=db
DB_PORT=5432
DB_NAME=central_db
DB_USER=postgres
DB_PASSWORD=Giorgos13
Important:
Use db as hostname (NOT localhost)
The service name comes from docker-compose
📦 Devcontainer behavior

On first creation:

init-env.sh runs automatically
assets/env.dev is created if missing
Gradle project is built

Configured in:

.devcontainer/devcontainer.json
⚠️ Common issues
❌ "UnknownHostException: db"

Cause:

app not running inside docker network

Fix:

ensure docker-compose service is active
ensure DB_HOST=db
❌ "Pages isn’t loaded"

Cause:

port not public

Fix:

set port 8080 → Public in Ports tab
❌ "The Secret cannot be null"

Cause:

missing JWT_SECRET in env file

Fix:

check assets/env.dev
🛠 Useful commands

Build project:

./gradlew build

Run project:

./gradlew run

Rebuild container:

Ctrl + Shift + P → Codespaces: Rebuild Container
🧠 Architecture notes
App runs in Codespaces container
DB runs as docker service
Communication via docker network hostname (db)
Env loaded from assets/env.dev
🚀 Future improvements
Flyway migrations
JWT auth hardening
Separate dev/prod env configs
CI/CD pipeline
Production Docker setup