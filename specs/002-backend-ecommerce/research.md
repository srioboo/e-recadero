# Research & Design Decisions: Backend E-Commerce with Spring Boot Modulith

**Date**: 2026-05-09 | **Feature**: 002-backend-ecommerce | **Status**: Complete

## 1. Spring Modulith Stability & Production Readiness

**Decision**: Use Spring Modulith 1.2.x+ (stable) with Spring Boot 3.3.x. Production-ready for modular monoliths.

**Rationale**: 
- Spring Modulith reached production status in 1.0 (2023); 1.2+ includes module visualization, event publishing, and documentation generation
- E-commerce requirements (isolated modules, clear boundaries, future microservice migration) align perfectly with Modulith's design goals
- Spring Boot 3.3.x provides latest security patches and performance improvements required for payment processing

**Alternatives Considered**:
- Quarkus (faster startup, less memory) → Rejected: Team expertise in Spring ecosystem; Docker deployment mitigates startup concerns
- Micronaut (lightweight modularity) → Rejected: Spring Modulith has better tooling and larger community support
- Custom module boundaries (without Modulith) → Rejected: No compile-time enforcement; teams can accidentally break module contracts

**Implementation Notes**:
- Enables `@ApplicationModuleTest` for module-level integration testing
- Module visibility rules prevent unauthorized cross-module dependencies at compile time
- Event publication via `org.springframework.context.ApplicationEventPublisher` (native Modulith support)

---

## 2. Event Processing Strategy

**Decision**: Use Spring Modulith's native `ApplicationEventPublisher` for synchronous domain events + Spring Cloud Stream with Kafka for asynchronous, persistent events.

**Rationale**:
- ApplicationEventPublisher: Synchronous, in-process events with @EventListener; perfect for catalog updates triggering inventory sync
- Kafka (via Spring Cloud Stream): Asynchronous, persistent events for order processing, payment notifications, email triggers; survives restarts
- Hybrid approach: Simple synchronous flows use ApplicationEventPublisher; critical async flows (payments, fulfillment) use Kafka for guarantees

**Alternatives Considered**:
- Pure Event Sourcing (Axon/EventStoreDB) → Rejected: Introduces significant complexity; traditional event pattern sufficient for this scope
- JMS/RabbitMQ → Rejected: Kafka chosen for durability, partitioning, and stream replay capabilities
- Direct method calls between modules → Rejected: Creates tight coupling; violates Modulith separation principle

**Implementation Notes**:
```
Domain Events (synchronous):
- OrderCreatedEvent → triggers InventoryReservation
- ProductPriceChangedEvent → updates Cart item calculations

Kafka Events (asynchronous):
- OrderConfirmedEvent → Order Fulfillment system, Email service, Analytics
- PaymentProcessedEvent → Order status update, Invoice generation
- CartAbandonedEvent → Email reminder campaign
```

---

## 3. Polyglot Persistence Trade-offs

**Decision**: PostgreSQL (primary transactional store) + Redis (cache & cart state) + Elasticsearch (optional product search at scale).

**Rationale**:
- PostgreSQL: ACID transactions mandatory for orders (no overselling), user payments, inventory locks
- Redis: Sub-100ms latency for cart operations (shopping cart hot path), session storage, product catalog cache (1-hour TTL)
- Elasticsearch: Optional; Phase 1 uses PostgreSQL full-text search; add ES at Phase 2 if product search latency exceeds 500ms at scale
- Per-module database schemas prevent accidental cross-module queries; module owns its data contract exclusively

**Alternatives Considered**:
- MongoDB only → Rejected: ACID transactions in older versions; lacks strong payment processing guarantees
- PostgreSQL + Cassandra → Rejected: Operational complexity; PostgreSQL + Redis covers all access patterns
- Complete polyglot (per-module DB selection) → Rejected: Increases DevOps overhead; unified PostgreSQL simplifies backup/replication

**Implementation Notes**:
- PostgreSQL schema per module with FK constraints ONLY within module
- Redis: Use RedisTemplate + Spring Data Redis; keys: `cart:{userId}`, `product-cache:{productId}`
- Cart expiration: Redis eviction policy TTL + scheduled task cleanup (Scheduled annotation)
- Connection pooling: HikariCP with pool size = (core_count * 2) + 1 for 4-core server = 9 connections

---

## 4. Authentication Strategy

**Decision**: JWT (short-lived) + Refresh token + Spring Security OAuth2 Resource Server configuration.

**Rationale**:
- JWT provides stateless authentication across modules (each module can validate token independently)
- Short-lived tokens (15 min): Compromised token exposure limited; refresh flow ensures continuous auth
- Spring Security OAuth2: Native Spring Boot support; minimal custom code; auditable
- No session affinity needed; enables horizontal scaling

**Alternatives Considered**:
- Session-based (traditional Spring Security) → Rejected: Requires sticky sessions; limits scalability
- mTLS → Rejected: Operational complexity; overkill for REST APIs (internal traffic can use VPC security)
- Custom JWT implementation → Rejected: Spring Security handles validation, caching, token refresh; reinventing wheel introduces vulnerabilities

**Implementation Notes**:
```
Token Claims:
- sub (subject): user_id
- email: user_email
- roles: [CUSTOMER, ADMIN, VENDOR]
- iat, exp: standard JWT times
- aud: recadero-api

Token Lifecycle:
- Access Token: 15 minutes
- Refresh Token: 7 days (stored in Redis for revocation)
- Logout: Mark refresh token revoked in Redis
```

---

## 5. Payment Gateway Integration

**Decision**: Event-driven pattern with webhook callbacks. Asynchronous order confirmation after payment success via Kafka event.

**Rationale**:
- Webhooks (e.g., Stripe, PayPal): PCI-DSS compliant; we never touch card data; payment processor initiates confirmation
- Kafka event (`PaymentProcessedEvent`) ensures Order module receives notification even if service temporarily down (retry semantics)
- Order state transitions: pending → confirmed (webhook) → shipped (fulfillment) → delivered (carrier update)
- Timeout policy: Orders stuck in "pending" >10 min auto-expire; cart re-opened for customer

**Alternatives Considered**:
- Polling payment status → Rejected: Unnecessary latency; webhooks are real-time
- Direct payment processor SDK calls → Rejected: Introduces dependency; if processor down, checkout blocked
- Synchronous REST call after payment → Rejected: No guarantee of delivery; lost if Order service temporarily unavailable

**Implementation Notes**:
```
Webhook Handler (SecurityController):
POST /api/webhooks/payment/stripe
- Verify signature with Stripe public key
- Emit PaymentProcessedEvent
- Return 200 OK immediately (max 30s)

Event Listener (Orders Module):
@EventListener
public void handlePaymentProcessed(PaymentProcessedEvent event) {
  Order order = orderRepository.findById(event.getOrderId());
  order.setStatus(OrderStatus.CONFIRMED);
  orderRepository.save(order);
  publishOrderConfirmedEvent(order);
}
```

---

## 6. Inventory Management: Preventing Overselling

**Decision**: Optimistic locking on Product.version + Reservations table + Order confirmation workflow.

**Rationale**:
- Optimistic locking: Fast reads; conflicts rare (only on hot products); automatic retry at service layer
- Reservations table: Explicit `cart_item.reserved_quantity` prevents double-booking; cleared on cart expiration
- Two-phase commit (implicit):
  1. Add to cart: Reserve inventory (ProductVariant.stock_quantity -= 1; Reservation inserted)
  2. Checkout: Reduce actual stock on order confirmation (ProductVariant.stock_quantity finalized)
- Saga pattern: Not needed for happy path; reservations handle the majority of cases

**Alternatives Considered**:
- Pessimistic locking (SELECT FOR UPDATE) → Rejected: Creates lock contention under load; slower
- Saga pattern (compensating transactions) → Rejected: Overkill complexity; optimistic locking + reservations sufficient for scale
- No reservation (allocate on order confirm only) → Rejected: Introduces race conditions; user sees item in cart but stock disappears

**Implementation Notes**:
```
Entity: ProductVariant
- stock_quantity: int (actual available stock)
- version: long (optimistic lock field @Version)

Entity: Reservation
- id, cart_item_id, product_variant_id
- reserved_quantity, created_at, expires_at

Add to Cart Flow:
1. SELECT ProductVariant WHERE id = ? AND stock >= qty (check availability)
2. INSERT INTO Reservation (cart_item_id, product_variant_id, qty, expires_at)
3. ProductVariant.stock_quantity -= qty (may trigger OptimisticLockingException if concurrent adds)
4. On exception: retry step 3 (exponential backoff)

Order Confirmation Flow:
1. BEGIN TRANSACTION
2. For each OrderItem: DELETE Reservation WHERE cart_item_id = ?
3. Finalize inventory (stock already reserved; just confirm)
4. COMMIT

Cart Expiration Job:
DELETE Reservation WHERE expires_at < NOW()
UPDATE ProductVariant SET stock_quantity += reserved_qty (restore from expired reservations)
```

---

## 7. Module Communication Contracts

**Decision**: RESTful API for synchronous operations; Kafka events for asynchronous notifications. Contract tests enforce boundaries.

**Rationale**:
- REST: Direct API calls within same JVM context (same transaction boundary); simplest for Modulith
- Kafka: Async events enable decoupled notification (Orders doesn't wait for Email service)
- Contract Tests: Spring Modulith's `@ApplicationModuleTest` ensures modules don't directly reference each other's entities
- API Versioning: `/api/v1/products` enables future breaking changes without duplicate code

**Implementation Notes**:
```
Module Interaction Patterns:

SYNCHRONOUS (REST):
Cart Module → Catalog Module: GET /api/v1/products/{id}/price (fetch current price)
Cart Module → Catalog Module: GET /api/v1/products/{id}/availability (check stock)

ASYNCHRONOUS (Kafka):
Orders Module → (broadcast) OrderConfirmedEvent
  - Email Service (sends confirmation email)
  - Analytics Service (logs sale)
  - Fulfillment Service (initiates shipping)

Promotions Module → (broadcast) PromotionExpiredEvent
  - Email Service (sends "promotion ended" notification)
```

---

## 8. Database Schema Distribution

**Decision**: Shared PostgreSQL instance; schemas isolated per module. No cross-module foreign keys.

**Rationale**:
- Single PostgreSQL instance: Simplifies backup, replication, monitoring (vs. per-module DBs)
- Schema isolation: `CREATE SCHEMA catalog; CREATE SCHEMA users; ...` keeps data physically separated
- No foreign keys across schemas: Enforces module independence; schema can be extracted to separate DB later
- Migration strategy: Flyway (versioned migrations per schema)

**Implementation Notes**:
```
Schema Layout:
- public: Shared config, sequences
- catalog: Product, Category, ProductAttribute, ProductVariant, Inventory
- users: User, UserProfile, UserRole, Address
- orders: Order, OrderItem, OrderShipment, OrderPayment
- cart: Cart, CartItem, CartPromotion, Reservation
- promotions: Promotion, PromotionRule, CouponCode, PromotionUsage
- templates: Template, TemplateBlock, TemplateMeta, TemplateVersion, PageContent

JPA Entity Configuration:
@Entity
@Table(name = "product", schema = "catalog")
public class Product { ... }
```

---

## 9. Testing Strategy

**Decision**: Three-tier testing: Unit tests (services) → Contract tests (module boundaries) → Integration tests (end-to-end flows).

**Rationale**:
- Unit tests: JUnit 5 + Mockito; 80%+ coverage target for business logic
- Contract tests: Spring Modulith's `@ApplicationModuleTest` ensures modules don't break each other's APIs
- Integration tests: Testcontainers (PostgreSQL, Redis, Kafka); full flows (Add to Cart → Checkout → Order Created)
- Performance tests: K6 / JMeter for concurrent checkout scenarios (1000 users)

**Implementation Notes**:
```
Test Coverage:
- cat-service-unit-tests: 85% (ProductService, CatalogService logic)
- cat-contract-tests: 100% (Module API contracts)
- integration-tests: E2E flows (2-3 key scenarios: happy path, error recovery, concurrency)

Test Execution:
mvn clean test # Unit tests only (fast; <2 min)
mvn clean verify # + contract tests + integration tests (slower; ~10 min with Testcontainers)
```

---

## 10. Performance Optimization Checkpoints

**Decision**: Database indexing, Redis caching, query optimization (N+1 prevention), and continuous monitoring.

**Rationale**:
- Database indexes: ON (user_id, status), (product_id, stock_quantity), (category_id); auto-explain slow queries
- Redis cache: Product catalog (1hr TTL), User profiles (6hr TTL), Session data
- N+1 prevention: JPA `@EntityGraph` for batch loading; repository methods must specify joins upfront
- Monitoring: Micrometer metrics (response times, DB connection pool utilization); alerting on p95 >200ms

**Implementation Notes**:
```
Critical Indexes:
- catalog.product: (category_id, status)
- catalog.product_variant: (product_id, stock_quantity)
- orders.order: (user_id, status, created_at DESC)
- cart.cart: (user_id, status, expires_at)
- cart.reservation: (expires_at, created_at DESC)

Cache Invalidation:
- ProductPriceChangedEvent → Clear Redis cache for affected product
- InventoryReservedEvent → Update Redis inventory count

Performance SLAs:
- GET /api/v1/products?category=electronics: <500ms
- POST /api/v1/cart/add-item: <100ms
- POST /api/v1/orders/checkout: <1s (includes payment processing)
- GET /api/v1/orders/{user_id}: <200ms
```

---

## Implementation Readiness Checklist

- [x] Spring Modulith version and compatibility confirmed
- [x] Event processing architecture (ApplicationEventPublisher + Kafka) defined
- [x] Persistence strategy (PostgreSQL + Redis) selected with justification
- [x] Authentication (JWT + OAuth2) finalized
- [x] Payment integration pattern (webhooks + event-driven) documented
- [x] Inventory management strategy (optimistic locking) chosen with concurrency handling
- [x] Module communication contracts established
- [x] Testing strategy (unit → contract → integration) defined
- [x] Performance targets and monitoring approach confirmed

**Next Step**: Proceed to Phase 1 (`/speckit.plan --phase 1`) to generate `data-model.md` with entity relationships and `contracts/` with module API definitions.
