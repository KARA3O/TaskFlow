# TaskFlow

TaskFlow is a Spring Boot task-management application using:

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security
- BCrypt password hashing
- JWT authentication
- Jakarta Bean Validation
- A small HTML/CSS/JavaScript frontend

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
  -> Controller
```

## Requirements

- JDK 17
- PostgreSQL
- Git

## Database

Create a PostgreSQL database named `taskflow`.

Do not put your real database password in Git.

Set these environment variables before starting the application:

```text
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_base64_secret
```

`JWT_SECRET` must decode to at least 32 bytes.

You can generate a suitable Base64 secret with Python:

```bash
python -c "import secrets,base64; print(base64.b64encode(secrets.token_bytes(32)).decode())"
```

Then run:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

On Windows Git Bash, use:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Or use:

```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8080/
```

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

The response contains a JWT.

For protected endpoints send:

```text
Authorization: Bearer <token>
```

### Current user

```http
GET /api/users/me
Authorization: Bearer <token>
```

### Tasks

```text
GET    /api/tasks
GET    /api/tasks/{id}
POST   /api/tasks
PUT    /api/tasks/{id}
DELETE /api/tasks/{id}
```

## Important security note

Passwords are never stored as plain text. They are encoded using BCrypt.

JWT secrets and database passwords belong in environment variables or another secret-management system, not in source control.

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
