# TaskFlow

TaskFlow is a Spring Boot task-management application using:

- Java 17
- Spring Boot 3
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security
- BCrypt password hashing
- JWT authentication
- Jakarta Bean Validation
- A modern HTML/CSS/JavaScript frontend (no build step required)

## Architecture

```text
Frontend
   |
   | HTTP / JSON + Bearer JWT
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
PostgreSQL
```

Authentication adds:

```text
Login
  -> AuthenticationManager
  -> UserService / UserRepository
  -> BCrypt password check
  -> JwtService
  -> JWT returned to client

Protected request
  -> JwtAuthenticationFilter
  -> validate JWT
  -> SecurityContext
  -> Controller (scoped to the authenticated user for all task operations)
```

Every task belongs to exactly one user (`Task.owner`). All task queries,
updates, and deletes are scoped to the authenticated user at the repository
level, so one account can never read or modify another account's tasks.

## Requirements

- JDK 17
- PostgreSQL (or Docker, see below)
- Git

## Quick start with Docker (recommended for deploying today)

This spins up Postgres and the app together, with no local JDK/Maven needed.

```bash
cp .env.example .env
# edit .env: set DB_PASSWORD and generate JWT_SECRET (command below)

python3 -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(32)).decode())"
# paste the output into .env as JWT_SECRET

docker compose up --build
```

Then open <http://localhost:8080/>.

To deploy to a cloud host (Render, Railway, Fly.io, a VM, etc.), build and
push the image from the included `Dockerfile`, or point the platform at this
repo if it builds Dockerfiles directly. Set these environment variables on
the host:

| Variable       | Required | Example                                         |
|----------------|----------|--------------------------------------------------|
| `DB_URL`       | yes      | `jdbc:postgresql://<host>:5432/taskflow`          |
| `DB_USERNAME`  | yes      | `postgres`                                        |
| `DB_PASSWORD`  | yes      | (your database password)                          |
| `JWT_SECRET`   | yes      | Base64 string, at least 32 bytes                  |
| `JWT_EXPIRATION` | no     | milliseconds, defaults to `86400000` (24h)        |
| `PORT`         | no       | defaults to `8080`                                |
| `DDL_AUTO`     | no       | defaults to `update`; use `validate` once stable  |

## Running without Docker

Create a PostgreSQL database named `taskflow`, then set the same environment
variables as above (at minimum `DB_PASSWORD` and `JWT_SECRET`) and run:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

On Windows Git Bash, use the same two commands, or:

```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Open <http://localhost:8080/>.

## Main endpoints

### Register

```http
POST /api/users
Content-Type: application/json
```

```json
{
  "username": "karabo",
  "email": "karabo@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "karabo@example.com",
  "password": "password123"
}
```

The response contains a JWT. For protected endpoints send:

```text
Authorization: Bearer <token>
```

### Current user

```http
GET /api/users/me
Authorization: Bearer <token>
```

### Tasks (all scoped to the authenticated user)

```text
GET    /api/tasks
GET    /api/tasks/{id}
POST   /api/tasks
PUT    /api/tasks/{id}
PATCH  /api/tasks/{id}/start
PATCH  /api/tasks/{id}/complete
DELETE /api/tasks/{id}
```

### Health check

```http
GET /api/health
```

Unauthenticated, useful for uptime checks / load balancer probes.

## Security notes

- Passwords are never stored as plain text; they're hashed with BCrypt.
- JWTs are signed with HS256 using a secret that must be supplied via
  `JWT_SECRET` (the app refuses to start with a blank or invalid one).
- Every task read, update, and delete is scoped to the authenticated user
  at the query level (`findByIdAndOwner`) - one account cannot access
  another account's tasks by guessing or iterating IDs.
- The API is stateless (no server-side sessions), so CSRF protection is
  not needed for it; CSRF is only relevant when a browser can be tricked
  into sending an authenticated *cookie-based* request.
- Auth failures return generic JSON errors ("Invalid email or password",
  "Authentication required") rather than leaking whether an email exists
  or exposing internal exception details.
- Any unhandled server error returns a generic 500 message; the real
  stack trace is only written to server-side logs, never to the client.
- Secrets belong in environment variables or a secret manager - never in
  source control. `.env` and `application.properties`/`application-*.yml`
  are gitignored for this reason; the checked-in `application.yml` holds
  no secrets, only `${VAR}` placeholders.

## Study order

1. `model/User.java`
2. `repository/UserRepository.java`
3. `dto/UserRequest.java`
4. `service/UserService.java`
5. `controller/UserController.java`
6. `controller/AuthController.java`
7. `security/SecurityConfig.java`
8. `security/JwtService.java`
9. `security/JwtAuthenticationFilter.java`
10. `model/Task.java`
11. `service/TaskService.java`
12. `controller/TaskController.java`
13. Frontend `static/js/app.js`

The goal is to understand the request flow instead of memorising individual classes.
