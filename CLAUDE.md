# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plans:

**Frontend Migration (Svelte → Astro)**:
📄 [Implementation Plan](../specs/001-migrate-svelte-astro/plan.md)
📄 [Research & Decisions](../specs/001-migrate-svelte-astro/research.md)
📄 [Data Model](../specs/001-migrate-svelte-astro/data-model.md)

**Backend E-Commerce (Spring Boot + Modulith)**:
📄 [Implementation Plan](../specs/002-backend-ecommerce/plan.md)
📄 [Research & Decisions](../specs/002-backend-ecommerce/research.md)
📄 [Data Model](../specs/002-backend-ecommerce/data-model.md)
📄 [Quickstart Guide](../specs/002-backend-ecommerce/quickstart.md)
📄 [Module Contracts](../specs/002-backend-ecommerce/contracts/)

**Templates Frontend & Admin Editor (Astro)**:
📄 [Implementation Plan](../specs/003-templates-frontend-admin/plan.md)
📄 [Research & Decisions](../specs/003-templates-frontend-admin/research.md)
📄 [Data Model](../specs/003-templates-frontend-admin/data-model.md)
📄 [Quickstart Guide](../specs/003-templates-frontend-admin/quickstart.md)
📄 [API Client Contracts](../specs/003-templates-frontend-admin/contracts/)
<!-- SPECKIT END -->

## Project Overview

Monorepo with three independent apps: `front` (public storefront, Astro), `admin` (management dashboard, Astro), `back` (Spring Boot Modulith backend). Development is spec-driven via [spec-kit](https://github.com/github/spec-kit) — feature work lives under `specs/<NNN-name>/` (`spec.md`, `plan.md`, `tasks.md`, `research.md`, `data-model.md`, `contracts/`). Slash commands for the spec-kit workflow (specify, plan, tasks, clarify, analyze, checklist, constitution, implement) are available as skills under `.claude/skills/speckit-*`.

Before starting new backend or frontend work, check the relevant `tasks.md` (`specs/002-backend-ecommerce/tasks.md` for backend `T0xx` IDs, `specs/001-migrate-svelte-astro/tasks-frontend.md` for frontend `F0xx` IDs) — these are checkbox-tracked and are the source of truth for what's done vs. pending, not the README.

## Commands

### Backend (`back/`)

```bash
./gradlew bootRun                         # run the app (localhost:8080, context-path /api/v1)
./gradlew build
./gradlew test                            # all tests
./gradlew test --tests "ClassName"        # single test class
./gradlew test --tests "ClassName.methodName"
./gradlew test --tests "*catalog*"        # tests for one module
./gradlew checkstyle spotbugs             # static analysis
./gradlew jacocoTestReport                # coverage report (build/reports/jacoco)
./gradlew qualityGate                     # check + coverage in one gate
./gradlew flywayInfo / flywayMigrate       # migration status / apply
```

Requires PostgreSQL + Redis (see `back/compose.yaml`); Testcontainers spins up its own instances for integration tests.

### Frontend (`front/`, `admin/`)

Both apps share the same script names:

```bash
npm run dev          # front: :3001, admin: :3000
npm run build
npm run preview
npm run check        # tsc --noEmit
npm test             # vitest
npx vitest run <path>              # single test file
npm run test:e2e     # playwright
```

## Architecture

### Backend: Spring Modulith

- Java 21, Spring Boot 3.4.3, group `org.sirantar`, base package `org.sirantar.recadero` (note: README/spec docs sometimes reference `org.sirantar.e_recadero` — that package does not exist; use `org.sirantar.recadero`).
- The domain is split into 6 modules per `specs/002-backend-ecommerce/spec.md`: `catalog`, `users`, `cart`, `orders`, `promotions`, `templates`, plus a `shared` package for cross-cutting infra (security, exceptions, DTOs, config). Only `catalog` and `shared` are implemented; the other 5 are fully specified in `tasks.md` but have no source yet.
- Each module follows the same internal layout: `domain/` (JPA entities), `repository/` (Spring Data repos), `service/` (business logic + DTOs), `api/` (REST controllers), `events/` (domain events + publisher), and a `package-info.java` carrying `@ApplicationModule` to declare allowed dependencies.
- Module boundaries are enforced by Spring Modulith: cross-module access must go through REST contracts or domain events published via `ApplicationEventPublisher` — never direct repository/entity access across module packages. `*ApplicationModuleTest` classes verify this per module.
- `.specify/memory/constitution.md` governs backend workflow: define/update the OpenAPI contract in `specs/002-backend-ecommerce/contracts/` first, then implement controllers/DTOs from it, then add contract + integration tests. Contracts are the source of truth, not the code.
- Schema-per-module in Postgres via Flyway migrations (`back/src/main/resources/db/migration`), one `V*__create_<module>_schema.sql` per module.

### Frontend split

- `front` is the customer-facing storefront; `admin` is the internal management UI. Both are independent Astro projects (own `package.json`, own `astro.config.mjs`) — there is no shared package between them yet, so duplicated logic (API clients, types) currently lives separately in each app's `src/lib/`.
- `front`'s catalog pages/components (`src/pages/catalog/`, `src/pages/product/`, `src/components/catalog/`) consume the backend Catalog module REST API directly; frontend phases in `tasks-frontend.md` are explicitly gated on backend task IDs being done (e.g., cart-related frontend work is blocked until the Cart backend module exists).
- `admin` currently only has a base layout/shell (`MainLayout.astro`, `Sidebar.astro`, `Header.astro`) — management screens per module (products, orders, templates, etc.) are not built yet.
