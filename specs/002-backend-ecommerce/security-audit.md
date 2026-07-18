# Security Audit (T187–T188)

Static code review performed against the codebase at the end of Phase 9.
No live penetration testing was performed — there is no running server or
staging environment available in this environment (no Docker). Items
marked ⚠️ are real gaps found during this review; items marked ✅ were
verified directly against the code.

## Checklist (T187)

| Item | Status | Notes |
|---|---|---|
| SQL injection prevention | ✅ | All queries go through Spring Data JPA / parameterized JPQL (`:param` binding). No raw string-concatenated SQL anywhere in the codebase. |
| CSRF protection | ✅ (by design) | Disabled in `SecurityConfig`, but the API is stateless JWT-bearer-token auth (no cookies), which is CSRF-safe by construction — this is the standard, correct posture for a token API, not an oversight. |
| Rate limiting | ⚠️ **Not implemented** | No rate limiting exists in the application itself (`/api/v1/coupons/validate` and the auth endpoints are the ones the contracts call out as needing it). The `CouponController` comment currently punts this to "the gateway", but no gateway exists yet either. **Needs a real decision**: add `bucket4j`/similar in-app, or stand up a gateway (nginx/API Gateway) before production. |
| JWT token expiry | ✅ | 15 min access / 7 day refresh, configured in `SecurityProperties` / `application.yml`. |
| Password hashing bcrypt cost ≥ 12 | ✅ | `BCryptPasswordEncoder(12)` in `SecurityConfig`. |
| No secrets in code/logs | ⚠️ **Fixed during this audit** | `application-prod.yml` never overrode `app.security.jwt.secret`, so a production deployment would have silently signed tokens with the placeholder `"changeme-this-is-default-secret-key-use-env-var-in-prod"` value checked into `application.yml` — a publicly-readable secret in this repo. Fixed by requiring `${JWT_SECRET}` (no default) and `${ALLOWED_ORIGINS}` (no `localhost` fallback) in `application-prod.yml`; Spring will now fail to start in the `prod` profile rather than silently using an insecure default. No other hardcoded secrets found; no password/token values appear in any `log.*` call. |
| PCI DSS compliance (no card storage) | ✅ | `OrderPayment` stores only `payment_method` (a label) and `transaction_id` (the processor's opaque token). No card number, CVV, or other cardholder data is ever persisted. |
| CORS: only known frontend origins | ✅ | `SecurityConfig.corsConfigurationSource()` uses the configured `allowed-origins` list (defaults to `admin`/`front` dev ports); not a wildcard. Combined with `allowCredentials(true)`, a wildcard origin would in fact be rejected by Spring, so this is correctly restrictive by construction. |

## Authorization boundary review (T188, static)

Reviewed every controller for whether a caller can act on another user's
resources by guessing/incrementing a path-variable ID:

- **Users** (`AddressController`, `UserProfileController`): all operations resolve the target user from `@AuthenticationPrincipal`, never from a client-supplied ID — a user cannot address another user's profile/addresses.
- **Cart** (`CartController`): every operation resolves `userId` from the JWT principal; cart/item IDs are looked up scoped to that cart.
- **Orders** (`OrderController`): `getOwnedOrder(orderId, userId)` is called before every read/mutation (`getOrder`, `getPayment`, `getShipment`, `initiateReturn`, `listReturns`, `cancelOrder`, `refundOrder`) and throws `ResourceNotFoundException` (404, not 403 — deliberately avoids confirming the order exists for another user) if the order belongs to someone else.
- **Admin controllers** (`AdminOrderController`, `AdminUserController`, `PromotionAdminController`, `TemplateAdminController`): gated by `@AdminOnly` (method-level `@PreAuthorize`) in addition to `SecurityConfig`'s path-level `/api/v1/admin/**` → `hasRole("ADMIN")` rule — defense in depth, matches `RBAC_GUIDE.md`.
- ⚠️ **Known, documented gaps** (not fixed — no real integration exists to fix them against):
  - `POST /api/v1/orders/{id}/shipment/webhook` — no carrier authentication; any caller can push a status update for any tracking number. Documented in code as needing real carrier API-key/HMAC verification before production use.
  - `POST /api/v1/coupons/{code}/apply` — reachable directly over HTTP without the same ownership check the internal same-JVM path (`OrderConfirmedEventListener`) gets "for free" by only running off a trusted internal event. Low risk (idempotency limited by usage caps, no financial mutation), but should get proper service-to-service auth if ever called externally.

## Not performed (needs a running environment)

- **T188 live penetration testing** (XSS/injection probing against a running server, automated authz fuzzing) — no server is running in this environment.
- **Dependency vulnerability scan** (e.g., `./gradlew dependencyCheckAnalyze` or Snyk/Dependabot) — not run; recommend wiring into CI (see T193's pipeline).
