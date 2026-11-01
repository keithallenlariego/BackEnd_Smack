# Secure Web Chatroom - Spring Boot backend

Spring Boot 4 + Spring Data JPA + Spring Security (JWT) + WebSocket (STOMP), connected to the Supabase Postgres database.

## 1. Put the code in your project
Copy `src/main/java/com/example/securewebchatroom/*` into your project's main package folder.
If your package is not `com.example.securewebchatroom`, rename it (in VS Code: right-click the package > Rename Symbol)
and keep the main class (`@SpringBootApplication`) one level above these folders.

Add the dependencies from `pom.xml` to yours:
webmvc, data-jpa, security, validation, websocket, postgresql, and the three `jjwt` libraries.
(On Spring Boot 3.x the web starter is called `spring-boot-starter-web` instead of `spring-boot-starter-webmvc`.)

## 2. Settings
Copy `src/main/resources/application.properties` over yours, then copy
`application-local.properties.example` to `application-local.properties` and fill in:
- the Supabase connection (Dashboard > your project > Connect > **Session pooler**) and your database password
- `JWT_SECRET`: any long random text (32+ characters)

`application-local.properties` is in `.gitignore`. Never commit it.

## 3. Run
Run the main class (or `./mvnw spring-boot:run`). The API is at http://localhost:8080.
The tables already exist in Supabase; Hibernate only validates them (`ddl-auto=validate`).

## Endpoints (all under /api, except /ws)
| Method | Path | What it does |
|---|---|---|
| POST | /auth/register, /auth/login | Create account / log in (returns a token) |
| GET, PUT, DELETE | /users/me | Profile, update profile, delete account |
| GET | /users/me/stats | Dashboard numbers |
| POST, GET | /rooms | Create a room / rooms I belong to |
| GET | /rooms/public | Public rooms I can join |
| GET, PUT, DELETE | /rooms/{id} | One room (403 if private and you are not a member) |
| POST | /rooms/{id}/join, /rooms/{id}/leave | Join a public room / leave |
| GET, POST | /rooms/{id}/members | Member list / owner adds someone by username |
| PUT, DELETE | /rooms/{id}/members/{userId}[/role] | Owner changes role / removes member |
| GET, POST | /rooms/{id}/messages | Chat history / send a message |
| GET, PUT | /rooms/{id}/moderation | AI moderator settings (owner) |
| GET | /rooms/{id}/moderation/flags | Flagged messages and log (owner) |
| PUT | /moderation/flags/{flagId} | Approve, restore, or delete (owner) |
| WS | /ws | STOMP. Subscribe to /topic/rooms/{id} (members) and /topic/rooms/{id}/moderation (owner) |

Errors come back as JSON: `{"message": "..."}` with 400, 401, 403, 404 or 409.
