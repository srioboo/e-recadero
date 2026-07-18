# Quality Review (T189–T191)

Performed at the end of Phase 9, after the E2E tests (T180–T182) and security
audit (T187–T188).

## Critical finding, fixed: every endpoint was unreachable at its documented URL

While writing load-test scripts (T183/184) I needed the real, external request
paths and discovered that `server.servlet.context-path` is `/api/v1`
(`application.yml`, and every profile), while **every single
`@RequestMapping`/`@GetMapping`/etc. across all 16 controllers, and every
`SecurityConfig.requestMatchers` pattern, also independently hardcoded a
redundant `/api/v1` prefix**. Since Spring strips the context path before
matching both controller mappings and Security's request matchers, the actual
effective routes were doubled up (e.g. `/api/v1/api/v1/auth/login`) and every
declared `SecurityConfig` rule (including all the `permitAll()` rules for
login, register, tracking, public templates, coupon validation) silently
never matched anything — falling through to the terminal
`.anyRequest().authenticated()` rule. In a real deployment this would have
meant **the entire API was unreachable at its documented URLs, and login
itself would have required authentication** — a full lockout, invisible until
now purely because no test in this project has ever gone through the real
`DispatcherServlet`/`SecurityFilterChain` (0% controller coverage, see T189).

Fixed by stripping the redundant `/api/v1`/`/api` prefix from all 16
controllers' mappings, `SecurityConfig`'s `requestMatchers`, and
`OpenApiConfig`'s `GroupedOpenApi.pathsToMatch()` patterns (which match
against the same controller-relative values). Added
`back/src/test/java/org/sirantar/recadero/shared/config/RoutingAndSecurityWebMvcTest.java`
— the first test in this project to exercise a real `DispatcherServlet` +
`SecurityFilterChain` (`@WebMvcTest` + `@Import(SecurityConfig.class)`,
configured with the real `/api/v1` context path) — to empirically prove the
fix rather than relying on static reasoning alone: confirms the public login
endpoint is reachable unauthenticated, a protected endpoint rejects anonymous
callers, and accepts authenticated ones. All 174 tests pass (173 + this new
one; only the pre-existing, unrelated `RecaderoApplicationTests` H2/Flyway
migration-dialect failure remains — confirmed unrelated, it fails on the very
first migration, before any routing code runs).

## T189 — Code coverage

`./gradlew jacocoTestReport` (report: `back/build/reports/jacoco/test/jacocoTestReport.xml`).

Overall: **45% instructions, 31% branches** — well under the constitution's
80% target.

| Package type | Coverage |
|---|---|
| `*.domain` (entities) | ~100% |
| `*.service` | 49–78% |
| `*.api` (controllers) | **0% — every single controller, all 6 modules** |

**Root cause**: no `MockMvc`/`WebTestClient`/`@SpringBootTest` controller
tests exist anywhere in the project. Every test written across Phases 4–9
(including this phase's E2E tests) instantiates services directly with
mocked repositories — it never goes through a controller, so request
mapping, validation annotations (`@Valid`), `@PreAuthorize`/`@AdminOnly`
enforcement, and exception-handler wiring are exercised by nothing.

**Not fixed in this pass** — closing this gap means writing HTTP-layer
tests for ~16 controllers across 6 modules, which is a substantial, separate
body of work (likely comparable in size to one of the earlier module
phases). Flagging for the user to decide whether/when to schedule it rather
than doing it unprompted.

## T190 — Static analysis

### Checkstyle — was completely broken, now fixed

`./gradlew checkstyleMain` failed outright with *"Unable to create Root
Module"* — the config (`back/config/checkstyle.xml`) had never successfully
run once, on any past phase. Three separate Checkstyle 10.x incompatibilities
in the config, fixed in place:

1. `LineLength` was nested inside `TreeWalker`; in this Checkstyle version
   it must be a direct child of `Checker` (alongside `FileLength`,
   `FileTabCharacter`).
2. `AvoidNestedBlocks`'s `allowInnerClasses` property was renamed/removed;
   replaced with the current `allowInSwitchCase`.
3. `DoubleCheckedLocking` was removed from Checkstyle entirely (double-checked
   locking has been safe under the Java Memory Model since Java 5); the
   module was deleted from the config.

With the config now actually running, it reports **628 warnings / 2209 info**
findings across 251 files — mostly `MagicNumber` (JPA column-length/precision
literals like `255`, `12`, `100`) and `FinalParameters`. This is the
project's first-ever clean checkstyle run, so this backlog has simply never
been visible before. `maxWarnings = 0` in `build.gradle.kts` means
`checkstyleMain` will keep failing the build until either the backlog is
triaged or the threshold is relaxed — left as-is rather than silently
raising the threshold, so the gap stays visible.

### SpotBugs — was already working; findings triaged

`spotbugsMain` runs fine (this was last confirmed broken only through Phase
4; not an issue now). Findings:

- **Bulk of findings (~90%) are `EI`/`EI2`** ("may expose internal
  representation") — record accessors returning their own fields, and
  constructor-injected Spring beans storing their dependencies. This is the
  standard, intentional style throughout this codebase (Java records, Spring
  DI) — not fixed, and not worth defensive-copying everywhere.
- **Three genuine findings, fixed in this pass**:
  - `GlobalExceptionHandler.handleMethodArgumentTypeMismatch` called
    `ex.getRequiredType().getSimpleName()` without a null check;
    `getRequiredType()` is nullable per Spring's `TypeMismatchException`
    contract. Fixed with an explicit null check.
  - `TemplateBlockService.parseBlockType` and `TemplateService.parseType`
    both caught `NullPointerException` alongside `IllegalArgumentException`
    around `Enum.valueOf(...)`, which is fragile (can mask an unrelated NPE
    inside the same try block). Replaced with an explicit `value == null`
    check before the `valueOf` call.
- **One low-risk finding, left as-is**: `CT` on `JwtTokenProvider`'s
  constructor (throws on invalid config, which is intentional fail-fast
  behavior, not a defect).

## T191 — Modulith module-boundary review

Added `back/src/test/java/org/sirantar/recadero/ModularityTests.java`,
running `ApplicationModules.of(RecaderoApplication.class).verify()`. This is
a genuinely new check — the existing `*ApplicationModuleTest` classes (one
per module) only assert that expected classes exist by classpath resource
lookup; none of them actually verified module boundaries. This is the first
time `ApplicationModules.verify()` has ever been run against this codebase.

First run failed with ~100 violations. Root cause: by default, Spring
Modulith only treats a module's **root package** as its public API; every
module here puts all real code in subpackages (`domain/`, `repository/`,
`service/`, `api/`, `events/`, per the layout documented in `CLAUDE.md`), so
none of that code was ever actually exposed to other modules under strict
verification — every declared `allowedDependencies` entry was accidentally a
no-op the whole time (it only granted access to each module's root package,
which is empty everywhere).

Fixed by declaring `@NamedInterface` on the specific subpackages that are
genuinely used cross-module (`shared.dto`, `shared.exception`,
`shared.security`, `catalog.repository`, `catalog.domain`,
`catalog.service`, `orders.events`, `orders.repository`, `cart.events`,
`cart.service`, `users.repository`, `users.domain`), then updating each
module's `allowedDependencies` to reference them explicitly (e.g.
`"catalog::repository"` instead of `"catalog"`). No previously-undeclared
module coupling or dependency cycle was found once this was fixed — the
architecture described in `CLAUDE.md` (Orders→Cart/Catalog/Users,
Promotions→Orders/Cart/Catalog via dependency inversion for the coupon
validator, Cart with zero awareness of Promotions) holds up exactly as
designed. `ModularityTests` now passes and will catch any future violation
of these boundaries automatically.

## T185 — Index review (static)

No running database is available, so `EXPLAIN ANALYZE` couldn't be run
(that part of T185/T186 needs a real Postgres instance). Did the static half:
cross-checked all 86 `CREATE INDEX`/`CREATE UNIQUE INDEX` statements across
`back/src/main/resources/db/migration/*.sql` against every index documented
in `data-model.md`. Found a real, sizeable gap — documented indexes that were
never actually created:

- **Catalog**: `product.status` (+ composite `(category_id, status)`);
  `inventory.warehouse_id` (+ composite unique `(product_variant_id, warehouse_id)`).
- **Users**: `user (status, created_at DESC)` composite; `address (user_id, address_type)`
  composite; `user_role_assignment.role_name` standalone.
- **Cart**: `shopping_cart.expires_at`; `shopping_cart.session_id`;
  `cart_item (cart_id, variant_id)` unique; `cart_promotion.promotion_id`,
  `.coupon_code`, and composite unique `(cart_id, promotion_id)`.
- **Orders**: `order.confirmed_date`; composites `(user_id, status)` and
  `(status, created_at DESC)`.
- **Promotions**: `promotion.priority`; `promotion_rule (promotion_id, rule_type)`
  composite; `promotion_usage.order_id`/`.coupon_code_id` and composites
  `(promotion_id, used_at DESC)`, `(user_id, promotion_id)`.
- **Templates**: `template.created_by`; composite `(status, type)`.

Not fixed in this pass — these are additive `ALTER TABLE`/`CREATE INDEX`
migrations, low-risk to add, but it's a non-trivial batch (~20 statements)
across 6 modules and I didn't want to add speculative migrations without the
user weighing in on priority. Flagging for a follow-up migration.

## T183/T184 — Load test scripts

No running server/staging environment exists in this session, so these
couldn't actually be executed — written as ready-to-run k6 scripts instead:
`back/load-tests/checkout-flow.js` (T183: mixed browse/add-to-cart/coupon/
checkout traffic, 100 VUs) and `back/load-tests/product-search.js` (T184: 500
VUs hitting product search/listing). Both encode the p95 latency targets from
`tasks.md` as k6 `thresholds` so they fail loudly if run against a real
deployment and the target isn't met.

## Summary

| Task | Status |
|---|---|
| T189 coverage | Measured and documented; 0%-controller gap **not closed** (needs explicit scoping) |
| T190 static analysis | Checkstyle config fixed (was totally broken); SpotBugs findings triaged, 3 real bugs fixed |
| T191 code review | Real Modulith boundary verification added and passing (previously never actually ran) |
