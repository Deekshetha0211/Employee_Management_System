# EMS (Employee Management System)

A Spring Boot Employee Management System (EMS) using Spring Data JPA, Spring Security (JWT), PostgreSQL and Redis. This README explains the repository layout, quick local setup with Docker, build options and trade-offs.

---

## Repo structure (high level)

- `pom.xml` — Maven project file and dependencies
- `Dockerfile` — builds the backend image
- `docker-compose.yml` — development compose file (Postgres + Redis + backend)
- `src/main/java/...` — application source code (controllers, services, repositories)
- `src/main/resources/` — `application.yaml`, `schema.sql`, `data.sql`
- `src/test/` — unit and integration tests

Note: `target/` contains build artifacts; do not commit them.

---

## Prerequisites

- Docker Desktop (Docker + Docker Compose) on Windows
- Java 17+ (if you run the app without Docker)
- Maven (for building and running tests)

---

## Quickstart: Run with Docker Compose (recommended for development)

The provided `docker-compose.yml` starts:
- `db` — PostgreSQL (DB: `ems`, user: `ems_user`, password: `ems_pass`) on port 5432
- `redis` — Redis on port 6379
- `backend` — the Spring Boot app on port 8080

Start (build images and run):

```powershell
# from repository root
docker-compose up --build
```

Run in background:

```powershell
docker-compose up --build -d
```

Stop and remove containers:

```powershell
docker-compose down
```

Remove containers and volumes (destructive):

```powershell
docker-compose down -v
```

Tail backend logs:

```powershell
docker-compose logs -f backend
```

Open: http://localhost:8080

---

## Build options

1) Maven (runnable JAR)

```powershell
mvn -DskipTests package
java -jar target/*.jar
```

2) Build with the project Dockerfile

```powershell
docker build -t ems_backend .
```

Note: `spring-boot:build-image` (buildpacks) is another option if you prefer buildpacks; it is optional and not required for local development.

---

## Environment & configuration

- Main config file: `src/main/resources/application.yaml`. Override values via environment variables as needed.
- Database seed and schema: `src/main/resources/schema.sql` and `data.sql` (the app may execute these depending on your Spring Boot settings).
- `docker-compose.yml` contains credentials for local development only — do not reuse them in production.

---

## Tests

Run unit tests locally:

```powershell
mvn test
```

---

## Development tips and trade-offs

- Docker Compose provides a reproducible local environment (Postgres + Redis). It’s convenient for development but should be replaced by managed services in production.
- Redis is used for caching (see `CacheConfig`) to improve read performance.
- We keep a named Postgres volume (`ems_pgdata`) so data persists between restarts. Use `docker-compose down -v` to reset.
- Secrets are stored in `docker-compose.yml` for convenience — for production, use a secrets manager or environment-specific secret store.

---

## Authentication & Password Reset Architecture

This system implements a secure, enterprise-style authentication and onboarding flow using
Spring Security (JWT) and BCrypt password hashing.

---

### Core Principles

- Raw passwords are **never stored**
- All passwords are hashed using **BCrypt**
- New employees receive a **temporary password**
- Users are **forced to reset password on first login**
- Authorization is role-based (`ADMIN`, `HR_MANAGER`, `EMPLOYEE`)

---

### User Identity Model

Authentication data is stored separately from HR data.

**`employees`**
- HR domain entity (name, department, status, hire date)

**`app_users`**
- Authentication identity linked to employee

Key fields in `app_users`:
- `email`
- `password_hash`
- `role`
- `employee_id`
- `enabled`
- `must_change_password`
- `password_changed_at`

---

### Onboarding Flow (Admin / HR)

1. Admin/HR creates an employee via:
2. System generates a **random temporary password**
3. Password is hashed using BCrypt
4. `app_users` record is created with:
- role = `EMPLOYEE`
- link to `employee_id`
- `must_change_password = true`
5. Temporary password is returned **once** in the response

> Temporary passwords are never persisted in plain text.

---

### Login Flow

1. Employee logs in using:
2. Credentials are verified using `BCryptPasswordEncoder.matches`
3. On success, a JWT token is issued
4. Login response includes:
- `accessToken`
- `role`
- `mustChangePassword` flag

Example response:
```json
{
"accessToken": "<jwt-token>",
"tokenType": "Bearer",
"role": "EMPLOYEE",
"mustChangePassword": true
}
```

### Password Reset Flow
1. Authenticated user calls:
POST /auth/change-password
2. Request body: {
   "currentPassword": "TemporaryPassword",
   "newPassword": "NewStrongPassword@123"
   }
3. Backend: 
- validates current password 
- enforces password policy 
- hashes new password with BCrypt 
- updates user record:
  - password_hash 
  - must_change_password = false 
  - password_changed_at = now()
4. User can now access the system normally

### Password Security
BCrypt provides:
- per-password random salt 
- configurable work factor 
- Same password always produces a different hash 
- Password verification is done using PasswordEncoder.matches 
- Password hashes are never reversible

### Authorization Rules

- ADMIN full access 
- HR, MANAGER manage employees 
- EMPLOYEE read-only access 
- Role enforcement is done at API layer via Spring Security

### Design Decisions & Trade-offs
- Temporary passwords improve onboarding security without email dependencies 
- First-login reset avoids long-lived weak credentials 
- JWT provides stateless authentication 
- No password reset tokens required for first-login flow 
- Identity (app_users) is decoupled from HR data (employees)