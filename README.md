# TaskFlow

TaskFlow is a self-contained Spring Boot task-management application. It serves its HTML, CSS, and JavaScript frontend from the same application that exposes the JSON API, and stores user and task data in PostgreSQL.

| Component | Release configuration |
|---|---|
| Application runtime | Java 17 / Spring Boot 3.3 |
| Database | PostgreSQL 16 |
| Authentication | BCrypt password hashing and HS256 JWTs |
| Default HTTP port | `8080` internally; configurable externally with `APP_PORT` |
| Health endpoint | `GET /api/health` |
| AI recommendations | Optional Gemini integration with a safe local fallback |

## Deploy today with Docker Compose

Docker Compose is the fastest supported path because it creates the PostgreSQL database and application together. Install Docker Engine with the Compose plugin on the target host, then run the following from the project directory.

```bash
cp .env.example .env
python3 -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(32)).decode())"
```

Edit `.env` with a strong database password and the generated value as `JWT_SECRET`. Add `GEMINI_API_KEY` only if the live AI recommendations feature should call Gemini; the application remains fully functional without it and returns a local prioritisation fallback.

```bash
docker compose config
docker compose up -d --build
docker compose ps
curl -fsS http://localhost:8080/api/health
```

A successful health check returns the following JSON response.

```json
{"status":"TaskFlow API is running!"}
```

If port `8080` is already used by another process, add `APP_PORT=8081` to `.env`, rerun `docker compose up -d --build`, and request `http://localhost:8081/api/health` instead. For a public deployment, place the service behind the hosting provider’s TLS-enabled domain or a reverse proxy that terminates HTTPS and forwards traffic to the configured application port.

## Environment variables

| Variable | Required | Purpose |
|---|---:|---|
| `DB_URL` | No with Compose | JDBC PostgreSQL URL. Compose supplies the internal database URL. |
| `DB_USERNAME` | No with Compose | Database username. Compose uses `postgres`. |
| `DB_PASSWORD` | Yes | Strong PostgreSQL password. |
| `JWT_SECRET` | Yes | Base64-encoded secret containing at least 32 bytes. Generate a new one for every environment. |
| `JWT_EXPIRATION` | No | Token lifetime in milliseconds; defaults to `86400000` (24 hours). |
| `GEMINI_API_KEY` | No | Enables live Gemini-powered recommendations. |
| `DDL_AUTO` | No | Schema policy; defaults to `update`. Set to `validate` after the schema is established and migrations are managed. |
| `PORT` | No | Port used inside non-Compose hosting environments; defaults to `8080`. |
| `APP_PORT` | No with Compose | Host port mapped to the container’s internal `8080` port; defaults to `8080`. |

> Keep real values only in the deployment platform’s secret manager or the untracked `.env` file. Never commit, upload, or share those values.

## Non-Docker deployment

A platform that accepts a Dockerfile can build the included `Dockerfile` directly. Configure the variables in the preceding table plus a managed PostgreSQL instance, set `DB_URL` to its JDBC connection string, and expose the platform-provided `PORT` value to the container. The image runs as a non-root `spring` user and serves on port `8080` by default.

For a manual Java launch, install a JDK 17, provide the required environment variables, and run:

```bash
./mvnw clean package
java -jar target/taskflow-0.0.1-SNAPSHOT.jar
```

## Release verification

Before publishing, use the following commands. The test suite runs against an isolated in-memory database; it covers application startup, health, registration, login, a protected task lifecycle, and the AI endpoint fallback.

```bash
./mvnw clean test
./mvnw clean package
```

After deployment, validate the public host rather than only the local machine.

```bash
curl -fsS https://YOUR-DOMAIN/api/health
```

Then open the domain in a browser, create a test account, sign in, add a task, start and complete it, and use **Analyze My Day**. If `GEMINI_API_KEY` is intentionally unset, the AI panel should still display a local priority recommendation instead of an error.

## Operations and rollback

Use the following operational commands from the project directory.

```bash
# Follow application logs
docker compose logs -f app

# Follow database logs
docker compose logs -f db

# Rebuild and restart after a code update
docker compose up -d --build

# Stop containers but preserve the database volume
docker compose down
```

Do **not** run `docker compose down -v` on the live host unless you intend to permanently delete the PostgreSQL data volume. To roll back application code, restore the previously known-good project revision or image tag, then run `docker compose up -d --build`; the named database volume remains in place.

## Main API routes

| Method | Route | Authentication |
|---|---|---|
| `POST` | `/api/users` | Public registration |
| `POST` | `/api/auth/login` | Public login |
| `GET` | `/api/users/me` | Bearer JWT |
| `GET` | `/api/tasks` | Bearer JWT |
| `GET` | `/api/tasks/{id}` | Bearer JWT |
| `POST` | `/api/tasks` | Bearer JWT |
| `PUT` | `/api/tasks/{id}` | Bearer JWT |
| `PATCH` | `/api/tasks/{id}/start` | Bearer JWT |
| `PATCH` | `/api/tasks/{id}/complete` | Bearer JWT |
| `DELETE` | `/api/tasks/{id}` | Bearer JWT |
| `GET` | `/api/ai/recommendations` | Bearer JWT |
| `GET` | `/api/health` | Public |

Every task lookup, update, and deletion is scoped to the authenticated user. The backend returns JSON authentication errors, while the frontend handles expired or invalid sessions by returning the visitor to the sign-in screen.
