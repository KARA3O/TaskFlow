# TaskFlow Release Notes

## Release readiness

This release has passed a clean Java 17 Maven build, two automated tests, a live Spring Boot startup check, a health probe, and a browser rendering check of the login and registration views. The executable release artifact is `target/taskflow-0.0.1-SNAPSHOT.jar`.

## Resolved defects

| Area | Issue found | Resolution |
|---|---|---|
| Compilation | `TaskService` called a removed three-argument `Task.updateTask` method. | Aligned task creation, updates, and responses with the `estimatedMinutes` task model. |
| Compilation | `AiService` called a nonexistent `TaskRepository.findByUser` method. | Replaced it with the existing owner-scoped active-task repository query. |
| Compilation | `AiPlanResponse.Java` had an uppercase extension, preventing normal source discovery. | Restored the DTO as `AiPlanResponse.java`. |
| Compilation | Several AI sources used the wrong `com.taskflow` package and one file declared a public class whose name did not match its filename. | Removed the malformed duplicate sources and retained one correctly packaged, authenticated `GET /api/ai/recommendations` controller. |
| Startup | Two `SecurityConfig` classes would create conflicting `securityConfig` beans. | Removed the duplicate configuration and retained the stateless JWT configuration with JSON 401/403 responses. |
| Configuration | `application.properties` held a live Gemini key and conflicted with the YAML-based environment configuration. | Removed the secrets-bearing properties file and added optional `GEMINI_API_KEY` environment binding. |
| Testing | The default context test depended on a PostgreSQL service and undeclared release secrets. | Added an isolated H2 test profile plus a release smoke test covering core public and protected API flows. |
| Containers | Compose did not pass the optional AI key or allow a configurable host port. | Added `GEMINI_API_KEY` and `APP_PORT` support; tightened the PostgreSQL health probe. |
| Release hygiene | The attached working copy included a local `.env` file and lacked a Docker build-context exclusion list. | Removed `.env` from the release copy, retained only `.env.example`, and added `.dockerignore`. |
| Source hygiene | Text files had mixed CRLF/LF line endings that caused repository whitespace diagnostics. | Normalized project text files to LF; `git diff --check` now passes. |

## Deployment constraint

The production Docker Compose launch was not executed in this workspace because Docker and a production PostgreSQL service are unavailable here. The application was instead started live against the isolated H2 test profile, where its health endpoint and browser UI were confirmed. Follow the Docker Compose launch and verification sequence in `README.md` on the target host.

> Before a public launch, create a fresh `.env` from `.env.example` and use a newly generated `JWT_SECRET`. Do not reuse or publish any credentials that may have appeared in the original attached archive.
