# API Documentation (T192)

## OpenAPI / Swagger UI

The app already ships `springdoc-openapi-starter-webmvc-ui` and a fully
configured `OpenApiConfig` (grouped by module: catalog, users, cart, orders,
promotions, templates, admin). No extra build step or plugin is needed — the
docs are generated at runtime, directly from the annotated controllers, the
moment the app starts:

```bash
./gradlew bootRun
```

- Interactive UI: `http://localhost:8080/api/v1/swagger-ui.html`
- Raw OpenAPI JSON: `http://localhost:8080/api/v1/v3/api-docs`
- Per-module JSON: `http://localhost:8080/api/v1/v3/api-docs/{group}` where
  `{group}` is one of `catalog`, `users`, `cart`, `orders`, `promotions`,
  `templates`, `admin`.

This couldn't be exercised in this session (no running Postgres/Docker
available), but the wiring itself is code-complete — this was verified
statically by reading `OpenApiConfig` and confirming its `pathsToMatch()`
patterns now match the corrected (post-routing-fix) controller mappings (see
`quality-review.md`).

## Postman collection

Don't hand-maintain a static Postman collection file — it goes stale the
moment a controller changes, and springdoc already produces the source of
truth. Instead, once the app is running:

1. In Postman: **Import → Link** → paste `http://localhost:8080/api/v1/v3/api-docs`.
2. Postman converts the OpenAPI spec into a full collection automatically,
   including all 95+ endpoints, grouped by module, with request/response
   schemas.
3. Re-import any time the API changes — it's always in sync with the code,
   unlike a committed collection file would be.

## Authentication flow

All endpoints under `/api/v1/` require a JWT bearer token except the public
ones (`POST /auth/register`, `/auth/login`, `/auth/refresh-token`,
`/auth/verify-email`, `/auth/forgot-password`, `/auth/reset-password`, plus
`GET /tracking/**`, `GET /templates/*`, `POST /coupons/validate` — see
`SecurityConfig`).

### 1. Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "username": "jane",
    "password": "SecureP@ss123",
    "first_name": "Jane",
    "last_name": "Doe"
  }'
```

Returns `201 Created` and sends a verification email (token consumed via
`POST /auth/verify-email`).

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "jane@example.com", "password": "SecureP@ss123"}'
```

Response:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 900,
  "user": { "id": 1, "email": "jane@example.com", "username": "jane" }
}
```

`access_token` expires in 15 minutes (900s); `refresh_token` in 7 days.

### 3. Call a protected endpoint

```bash
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Refresh an expired access token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
```

Returns a new `access_token` (and typically a rotated `refresh_token`).

### 5. Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "<refresh_token>"}'
```

Invalidates the refresh token server-side (access tokens are stateless JWTs
and simply expire naturally within 15 minutes).

### Admin endpoints

Everything under `/api/v1/admin/**` additionally requires the caller's JWT
to carry the `ADMIN` role (`SecurityConfig`: `.requestMatchers("/admin/**").hasRole("ADMIN")`,
enforced again per-method via `@AdminOnly` — see `RBAC_GUIDE.md`). There's no
separate admin login; admin status is a role on the same user account.
