# Implementation Plan: Backend E-Commerce Spring Boot Application with Modulith

**Branch**: `002-backend-ecommerce` | **Date**: 2026-05-09 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-backend-ecommerce/spec.md`

## Summary

Build a modular backend e-commerce platform using Spring Boot 3.x with Modulith architecture pattern. The system manages product catalogs, user accounts, shopping carts, orders, promotions, and dynamic page templating. Six independent modules (Catalog, Users, Shopping Cart, Orders, Promotions, Templates) communicate via application events and REST APIs with strict module boundaries. Target: <200ms API response times with 80%+ test coverage.

## Technical Context

**Language/Version**: Java 21+ (Spring Boot 3.x)  
**Primary Dependencies**: Spring Data JPA, Spring Security, Spring Modulith, Spring Web, PostgreSQL, Redis, Kafka/RabbitMQ  
**Storage**: PostgreSQL (primary), Redis (caching & sessions), Kafka/RabbitMQ (event streams)  
**Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers  
**Target Platform**: Linux server / Cloud deployment (AWS/GCP/Azure)  
**Project Type**: Microservices architecture (modular monolith) with REST APIs  
**Performance Goals**: API response <200ms (p95), catalog search <500ms, support 1000 concurrent checkout users  
**Constraints**: <200ms p95, PCI DSS compliance for payments, 80%+ test coverage, zero circular dependencies between modules  
**Scale/Scope**: 6 independent modules, 10+ entities, 50+ REST endpoints, 100k+ products support

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principles Evaluation:
- **Code Quality Standard**: ✅ Modulith enforces clean architecture; strict module boundaries ensure maintainability
- **Test-First Development**: ✅ Contract tests required for module boundaries; integration tests for inter-module communication; unit tests target 80%+
- **User Experience Consistency**: ✅ Template module provides consistent UI across pages; API contracts ensure consistent responses
- **Performance Requirements**: ✅ < 200ms p95 target met via database indexing, caching (Redis), and async event processing

**Status**: ✅ PASS - All principles satisfied. No violations requiring justification.

## Project Structure

### Documentation (this feature)

```text
specs/002-backend-ecommerce/
├── plan.md              # This file
├── research.md          # Phase 0 (PENDING: Technology decisions, integration patterns)
├── data-model.md        # Phase 1 (PENDING: Entity relationships, schema design)
├── quickstart.md        # Phase 1 (PENDING: Development setup guide)
├── contracts/           # Phase 1 (PENDING: Module interfaces, API contracts)
│   ├── catalog-contract.md
│   ├── users-contract.md
│   ├── cart-contract.md
│   ├── orders-contract.md
│   ├── promotions-contract.md
│   └── templates-contract.md
└── tasks.md             # Phase 2 (TODO: Actionable implementation tasks)
```

### Source Code Repository Structure

```text
back/
├── src/main/java/org/sirantar/recadero/
│   ├── catalog/                    # Catalog Module (Products & Categories)
│   │   ├── api/                    # REST Controllers
│   │   ├── domain/                 # Domain entities & business logic
│   │   ├── persistence/            # Repositories
│   │   ├── service/                # Business services
│   │   └── events/                 # Domain events
│   │
│   ├── users/                      # Users Module (Accounts & Profiles)
│   │   ├── api/
│   │   ├── domain/
│   │   ├── persistence/
│   │   ├── service/
│   │   └── events/
│   │
│   ├── cart/                       # Shopping Cart Module
│   │   ├── api/
│   │   ├── domain/
│   │   ├── persistence/
│   │   ├── service/
│   │   └── events/
│   │
│   ├── orders/                     # Orders Module
│   │   ├── api/
│   │   ├── domain/
│   │   ├── persistence/
│   │   ├── service/
│   │   └── events/
│   │
│   ├── promotions/                 # Promotions Module
│   │   ├── api/
│   │   ├── domain/
│   │   ├── persistence/
│   │   ├── service/
│   │   ├── engine/                 # Promotion rules engine
│   │   └── events/
│   │
│   ├── templates/                  # Template Management Module
│   │   ├── api/
│   │   ├── domain/
│   │   ├── persistence/
│   │   ├── service/
│   │   └── events/
│   │
│   ├── shared/                     # Shared utilities & config (NO BUSINESS LOGIC)
│   │   ├── config/                 # Spring configuration classes
│   │   ├── dto/                    # Common DTOs
│   │   ├── events/                 # Base event classes
│   │   ├── exception/              # Global exception handlers
│   │   └── util/                   # Helper utilities
│   │
│   └── RecaderoApplication.java    # Main Spring Boot application
│
├── src/test/java/org/sirantar/recadero/
│   ├── catalog/                    # Module-level unit & integration tests
│   ├── users/
│   ├── cart/
│   ├── orders/
│   ├── promotions/
│   ├── templates/
│   ├── integration/                # Cross-module integration tests
│   └── contract/                   # Contract tests (module boundaries)
│
├── build.gradle.kts                # Gradle build with module definitions
├── settings.gradle.kts
├── compose.yaml                    # Docker Compose for local dev (DB, Redis, Kafka)
├── README.md                        # Development setup guide
└── DEPLOYMENT.md                   # Deployment & ops guide
```

**Structure Decision**: 
Modular monolith with Spring Modulith. Each domain module (`catalog`, `users`, `cart`, `orders`, `promotions`, `templates`) is independently compilable and testable. A `shared` package contains ONLY cross-cutting concerns (config, utilities, exceptions, base DTOs). Module-to-module communication is ONLY via:
1. REST API calls (for synchronous operations)
2. Domain events via Spring's ApplicationEventPublisher (for asynchronous notifications)

No direct cross-module entity access.Dependencies managed in `build.gradle.kts` to prevent circular imports. This structure supports future migration to microservices if needed.

## Complexity Tracking

| Code | Aspect | Justification |
|------|--------|---------------|
| Modulith Pattern | Architectural Complexity | Provides module isolation + supports independent scaling; alternative flat structure would violate "Code Quality Standard" principle |
| Rules Engine (Promotions) | Logic Complexity | Flexible promotion rules require engine; hardcoded logic insufficient for business requirements |
| Event-Driven Communication | Integration Complexity | Necessary for async order processing, inventory updates, and cart expiration; direct method calls would create tight coupling |
| PostgreSQL + Redis | Storage Complexity | PostgreSQL for transactional consistency (orders, inventory); Redis for high-speed cache (products, sessions) + cart state |
| Testcontainers | Testing Infrastructure | Ensures real DB/cache behavior in integration tests; mocks insufficient for Modulith contract validation |

## Dependencies & Technology Decisions

**OPEN QUESTIONS FOR PHASE 0 RESEARCH**:
- [ ] Spring Modulith stability for production (version compatibility matrix)
- [ ] Event sourcing vs. traditional event publishing (CQRS vs. standard events)
- [ ] Polyglot persistence trade-offs (PostgreSQL-only vs. specialized stores per module)
- [ ] Authentication strategy: JWT, OAuth2, or Spring Security defaults?
- [ ] Payment gateway integration pattern (webhook vs. polling vs. event-driven)
- [ ] Inventory reservation strategy (pessimistic locking vs. optimistic locking vs. saga pattern)

## Rollout Strategy

### Phase 0: Research & Decision Making (PENDING)
- Technology validation: Spring Modulith maturity, event framework selection
- Integration patterns: Module communication, event serialization
- Database schema design: Entity relationships, migration strategy
- Testing harness: Contract test setup, integration test infrastructure

### Phase 1: Design & Contracts (BLOCKING: Phase 0 completion)
- Data model: Entity relationships, validation rules, state machines
- Module contracts: REST API definitions, domain event schemas
- Quickstart guide: Local development setup (Docker Compose)
- Agent context update in `.github/copilot-instructions.md`

### Phase 2: Implementation Tasks (BLOCKING: Phase 1 completion)
- Module-by-module implementation with tests
- Integration tests for module boundaries
- Performance testing & optimization
- Deployment & operational tooling

---

**Next Step**: Run `/speckit.plan --phase 0` to generate `research.md` with all technology decisions and integration patterns documented.
