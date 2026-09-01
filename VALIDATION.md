# Release Validation Record

**Date:** 2026-09-01

| Check | Result | Evidence |
|---|---|---|
| Clean Maven build | Passed | `./mvnw -B clean package` completed successfully with Java 17. |
| Automated test suite | Passed | 2 tests passed: application context and API release smoke test. |
| Authenticated task workflow | Passed | Automated smoke test covered registration, login, create task, start task, AI recommendation fallback, and complete task. |
| Live application startup | Passed | Spring Boot started on port `8090` using the isolated `test` profile. |
| Health probe | Passed | `GET /api/health` returned `{"status":"TaskFlow API is running!"}`. |
| Browser interface | Passed | Login and registration views rendered with expected fields and primary controls. |
| Release jar | Passed | `target/taskflow-0.0.1-SNAPSHOT.jar` was produced successfully. |
| Patch whitespace check | Passed | `git diff --check` produced no output after line-ending normalization. |

> The live runtime validation used the in-memory test profile because a container runtime and production PostgreSQL service are not available in this workspace. The included Docker Compose configuration is prepared for a production PostgreSQL deployment but was not executed here.
