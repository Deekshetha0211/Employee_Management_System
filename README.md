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