# Backend E-Commerce Constitution

## Core Principles

### I. API-First Contracts
Every REST endpoint starts as an OpenAPI contract in `specs/002-backend-ecommerce/contracts/`. Contracts are the source of truth for paths, request and response shapes, pagination, and error formats. Controller, DTO, and client work follows approved contracts, not the reverse.

### II. Contract-Driven Testing
Contract tests must verify that module implementations match their OpenAPI contracts and respect Modulith boundaries. A feature is not complete until its contract tests and relevant integration tests pass.

### III. Modulith Boundaries
Modules must stay isolated by schema and package. Cross-module communication is only allowed through approved REST contracts or domain events. No direct cross-module entity access.

### IV. Versioned and Observable APIs
All public APIs use versioned paths and consistent error responses. Breaking changes require a version bump, contract update, and validation of downstream consumers.

### V. Simplicity and Security
Prefer the smallest implementation that satisfies the contract and business rules. Validate inputs at the boundary, protect sensitive data, and keep behavior explicit and testable.

## Technical Standards

- Java 21+ and Spring Boot 3.x are the baseline runtime stack.
- Spring Modulith governs module visibility and dependency rules.
- SpringDoc OpenAPI is required for documenting and validating REST contracts.
- PostgreSQL is the primary datastore, with Redis and Kafka for caching and asynchronous events where needed.

## Development Workflow

1. Define or update the OpenAPI contract first.
2. Review the contract against the spec and acceptance criteria.
3. Implement controllers, DTOs, and mappers from the approved contract.
4. Add or update contract tests and integration tests.
5. Verify module boundaries, performance targets, and API documentation.

## Governance

This constitution supersedes local preferences and implementation habits. Any API-breaking change must update the contract, the versioned route, and the affected tests before merge. Complexity must be justified by a documented requirement or a measurable quality constraint.

**Version**: 1.0.0 | **Ratified**: 2026-05-14 | **Last Amended**: 2026-05-14
