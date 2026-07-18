# E-Recadero Backend 🔌

<div align="center">

[![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.1.5-brightgreen?logo=spring)](https://spring.io/projects/spring-modulith)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](../LICENSE)

**Enterprise-grade modular backend for e-commerce with Spring Boot 3.x & Modulith.**

Strict module boundaries • Event-driven architecture • <200ms API response times • 80%+ test coverage

[Quick Start](#quick-start) • [Architecture](#architecture) • [Modules](#modules) • [Development](#development) • [Testing](#testing)

</div>

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Modules](#modules)
- [API Reference](#api-reference)
- [Database](#database)
- [Caching & Performance](#caching--performance)
- [Development](#development)
- [Testing](#testing)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## Overview

The **E-Recadero Backend** is a modular Spring Boot 3.x application built with **Spring Modulith**, ensuring strict module boundaries and clean architecture principles.

### Design Goals

| Goal | Implementation |
|------|------------------|
| **Modularity** | 6 independent modules (Catalog, Users, Cart, Orders, Promotions, Templates) |
| **Performance** | Redis caching, async events, optimized queries (<200ms p95) |
| **Reliability** | 80%+ test coverage, contract tests, integration tests |
| **Maintainability** | No circular dependencies, clear module contracts, comprehensive documentation |
| **Security** | Spring Security, OAuth2, JWT tokens, PCI DSS compliance ready |
| **Scalability** | Event streaming (Kafka/RabbitMQ), async processing, connection pooling |

### Key Features

- ✅ **REST APIs** — 50+ endpoints for catalog, users, orders, promotions
- ✅ **Modular Monolith** — 6 independent modules communicating via events & REST
- ✅ **Event-Driven** — Domain events for async inter-module communication
- ✅ **Caching Layer** — Redis for catalog, cart, session data
- ✅ **Database Migrations** — Flyway for version-controlled schema changes
- ✅ **Authentication** — Spring Security + OAuth2 + JWT
- ✅ **Testing** — JUnit 5, Mockito, Testcontainers, contract tests
- ✅ **Monitoring** — Spring Actuator, health checks, metrics
- ✅ **Documentation** — Swagger/OpenAPI, structured logs

---

## Tech Stack

### Core

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21+ | Language |
| **Spring Boot** | 3.4.3 | Web framework |
| **Spring Modulith** | 1.1.5 | Module isolation & contracts |
| **Spring Data JPA** | 3.x | ORM & database abstraction |
| **Spring Security** | 3.x | Authentication & authorization |
| **Spring Actuator** | 3.x | Monitoring & health checks |

### Data & Cache

| Component | Version | Purpose |
|-----------|---------|---------|
| **PostgreSQL** | 15+ | Primary relational database |
| **Redis** | 6.x+ | Caching & session store |
| **Flyway** | 9.x | Database migrations |

### Messaging & Events

| Component | Version | Purpose |
|-----------|---------|---------|
| **Kafka** or **RabbitMQ** | Latest | Event streaming & async processing |
| **Spring Cloud Stream** | 4.x | Abstraction for messaging |

### Testing

| Framework | Version | Purpose |
|-----------|---------|---------|
| **JUnit 5** | 5.x | Unit testing |
| **Mockito** | 5.x | Mocking & spying |
| **AssertJ** | 3.x | Fluent assertions |
| **Spring Boot Test** | 3.x | Integration testing |
| **Testcontainers** | 1.x | Docker-based test infrastructure |
| **Spring Cloud Contract** | 4.x | Contract testing |

### Code Quality

| Tool | Purpose |
|------|---------|
| **Checkstyle** | Code style enforcement |
| **SpotBugs** | Bug detection |
| **JaCoCo** | Code coverage measurement |
| **Gradle** | Build automation & dependency management |

---

## Quick Start

### Prerequisites

```bash
# Check required versions
java -version      # Should be 21 or higher
gradle -v          # Should be 8.x+
docker -v          # For PostgreSQL & Redis
```

### Setup & Run

#### Option 1: With Docker (Recommended)

```bash
# Start database & cache services
cd .. && docker-compose up -d postgres redis && cd back

# Run backend
./gradlew bootRun

# Backend will be available at: http://localhost:8000
```

#### Option 2: Local with Manual Services

```bash
# Start PostgreSQL (ensure it's running on localhost:5432)
# Start Redis (ensure it's running on localhost:6379)

# Create database
psql -U postgres -c "CREATE DATABASE e_recadero;"

# Run backend
./gradlew bootRun
```

### Verify Setup

```bash
# Health check
curl http://localhost:8000/actuator/health

# API docs (Swagger UI)
open http://localhost:8000/swagger-ui.html

# Application info
curl http://localhost:8000/actuator/info
```

---

## Project Structure

```
back/
├── src/
│   ├── main/
│   │   ├── java/org/sirantar/e_recadero/
│   │   │   ├── catalog/
│   │   │   │   ├── api/                 # REST controllers
│   │   │   │   ├── domain/              # Domain entities & value objects
│   │   │   │   ├── application/         # Application services & DTOs
│   │   │   │   ├── infrastructure/      # Repositories, external services
│   │   │   │   └── events/              # Domain events
│   │   │   │
│   │   │   ├── users/
│   │   │   │   ├── api/
│   │   │   │   ├── domain/
│   │   │   │   ├── application/
│   │   │   │   ├── infrastructure/
│   │   │   │   └── events/
│   │   │   │
│   │   │   ├── cart/
│   │   │   │   └── [similar structure]
│   │   │   │
│   │   │   ├── orders/
│   │   │   │   └── [similar structure]
│   │   │   │
│   │   │   ├── promotions/
│   │   │   │   └── [similar structure]
│   │   │   │
│   │   │   ├── templates/
│   │   │   │   └── [similar structure]
│   │   │   │
│   │   │   └── shared/
│   │   │       ├── config/              # Global configuration
│   │   │       ├── exception/           # Exception handling
│   │   │       ├── event/               # Shared event infrastructure
│   │   │       ├── persistence/         # JPA configuration
│   │   │       └── util/                # Shared utilities
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/           # SQL migration scripts (Flyway)
│   │       │       ├── V1__init.sql
│   │       │       ├── V2__add_promotions.sql
│   │       │       └── ...
│   │       ├── application.yml          # Base configuration
│   │       ├── application-local.yml    # Local development
│   │       ├── application-test.yml     # Test profile
│   │       └── application-prod.yml     # Production profile
│   │
│   └── test/
│       ├── java/org/sirantar/e_recadero/
│       │   ├── catalog/
│       │   │   ├── CatalogServiceTest.java      # Unit tests
│       │   │   └── CatalogControllerIT.java     # Integration tests
│       │   ├── orders/
│       │   │   └── [similar]
│       │   └── ...
│       └── resources/
│           ├── application-test.yml
│           └── test-data.sql
│
├── build.gradle.kts                     # Build configuration
├── settings.gradle.kts
├── gradle/
├── Dockerfile                           # Container image
├── compose.yaml                         # Docker Compose services
├── DOCKER_COMPOSE.md                    # Deployment instructions
└── README.md                            # This file
```

---

## Architecture

### Modulith Pattern

E-Recadero Backend uses **Spring Modulith** to enforce module isolation:

```
┌─────────────────────────────────────────────────┐
│         Spring Modulith Application             │
│                                                 │
│  ┌──────────────┬───────────────┬──────────────┐│
│  │  Catalog     │  Users        │  Cart        ││
│  │  Module      │  Module       │  Module      ││
│  │              │               │              ││
│  │  - Products  │  - Auth       │  - Add item  ││
│  │  - Inventory │  - Profiles   │  - Remove    ││
│  │  - Search    │  - JWT tokens │  - Update    ││
│  └──────────────┴───────────────┴──────────────┘│
│  ┌──────────────┬───────────────┬──────────────┐│
│  │  Orders      │  Promotions   │  Templates   ││
│  │  Module      │  Module       │  Module      ││
│  │              │               │              ││
│  │  - Checkout  │  - Discounts  │  - Pages     ││
│  │  - History   │  - Pricing    │  - Content   ││
│  │  - Tracking  │  - Campaigns  │  - Rendering││
│  └──────────────┴───────────────┴──────────────┘│
│                                                 │
│  ┌─────────────────────────────────────────────┐│
│  │     Shared Event Bus (ApplicationEvents)    │ │
│  └─────────────────────────────────────────────┘│
│                                                 │
│  ┌─────────────────────────────────────────────┐│
│  │    Spring Actuator (Monitoring & Health)    │ │
│  └─────────────────────────────────────────────┘│
└─────────────────────────────────────────────────┘
```

### Module Interaction

Modules communicate via:

1. **REST APIs** — Public HTTP contracts (GET /products, POST /orders, etc.)
2. **Domain Events** — Async notifications (ProductCreatedEvent, OrderConfirmedEvent)
3. **Shared DTOs** — Contracts defined in module interfaces
4. **NO Direct Dependencies** — No module directly imports another's internal classes

**Example: Adding Item to Cart**

```
User Request
    ↓
Cart API (POST /cart/items)
    ↓
Cart Service receives item ID
    ↓
Cart Service publishes: ItemAddedToCartEvent
    ↓
Promotions Module listens
    ↓
Promotions Service calculates discounts
    ↓
Promotion Applied Event
    ↓
Cart updates with discounted price
    ↓
Response sent to user
```

---

## Modules

### 1. Catalog Module

**Responsibility**: Manage products, categories, inventory, and search.

**Endpoints**:
```
GET  /catalog/products              # List all products
GET  /catalog/products/{id}         # Get product details
GET  /catalog/products/search       # Full-text search
GET  /catalog/categories            # List categories
GET  /catalog/categories/{slug}     # Get category products
GET  /catalog/availability/{id}     # Check stock
```

**Key Entities**:
- `Product` — Product details, pricing, metadata
- `Category` — Product categorization
- `Inventory` — Stock levels, availability
- `SearchIndex` — Full-text search data

**Events Published**:
- `ProductCreatedEvent` — Product added
- `InventoryUpdatedEvent` — Stock level changed
- `ProductSearchedEvent` — Search performed

**Database Tables**:
- `products`, `categories`, `inventory`, `search_index`

---

### 2. Users Module

**Responsibility**: User authentication, profiles, and authorization.

**Endpoints**:
```
POST   /users/register              # Create account
POST   /users/login                 # Authenticate user
POST   /users/refresh-token         # Refresh JWT
GET    /users/profile               # Get user profile
PUT    /users/profile               # Update profile
POST   /users/logout                # Invalidate token
```

**Key Entities**:
- `User` — User account & profile
- `Role` — User roles (CUSTOMER, ADMIN)
- `Session` — Active user sessions

**Events Published**:
- `UserRegisteredEvent` — New user created
- `UserLoggedInEvent` — Login successful
- `UserProfileUpdatedEvent` — Profile changed

**Security**:
- Spring Security + JWT tokens
- Password hashing via bcrypt
- Role-based access control (RBAC)

**Database Tables**:
- `users`, `roles`, `sessions`

---

### 3. Shopping Cart Module

**Responsibility**: Manage shopping carts, item storage, and cart operations.

**Endpoints**:
```
GET    /cart                        # Get current cart
POST   /cart/items                  # Add item to cart
PUT    /cart/items/{itemId}         # Update item quantity
DELETE /cart/items/{itemId}         # Remove item
POST   /cart/checkout               # Initiate checkout
```

**Key Entities**:
- `Cart` — Shopping cart container
- `CartItem` — Item in cart
- `CartSession` — Cart state per user

**Events Published**:
- `ItemAddedToCartEvent` — Item added
- `ItemRemovedFromCartEvent` — Item removed
- `CartClearedEvent` — Cart emptied
- `CheckoutInitiatedEvent` — Checkout started

**Caching**:
- Carts stored in Redis for fast access
- Cached cart state per user session

**Database Tables**:
- `carts`, `cart_items`

---

### 4. Orders Module

**Responsibility**: Order creation, payment processing, and order history.

**Endpoints**:
```
POST   /orders                      # Create order
GET    /orders/{orderId}            # Get order details
GET    /orders                      # List user orders
PUT    /orders/{orderId}/cancel     # Cancel order
PUT    /orders/{orderId}/status     # Update status
```

**Key Entities**:
- `Order` — Order header
- `OrderItem` — Line items
- `OrderStatus` — Status tracking
- `Payment` — Payment information

**Events Published**:
- `OrderCreatedEvent` — Order placed
- `OrderConfirmedEvent` — Payment confirmed
- `OrderShippedEvent` — Order shipped
- `OrderCancelledEvent` — Order cancelled

**Workflow**:
1. Cart triggers `CheckoutInitiatedEvent`
2. Orders module creates order in `PENDING` status
3. Waits for payment confirmation
4. Updates status to `CONFIRMED`
5. Publishes `OrderConfirmedEvent` → Inventory decrements

**Database Tables**:
- `orders`, `order_items`, `payments`, `order_status_history`

---

### 5. Promotions Module

**Responsibility**: Manage discounts, pricing rules, and promotional campaigns.

**Endpoints**:
```
GET    /promotions                  # List active promotions
GET    /promotions/{code}           # Validate promo code
POST   /promotions/apply            # Apply promotion to cart
GET    /promotions/pricing/{id}     # Get dynamic pricing
```

**Key Entities**:
- `Promotion` — Discount rules
- `PromoCode` — Promo code for campaigns
- `PricingRule` — Dynamic pricing logic
- `PromotionHistory` — Applied promotions

**Events Listened To**:
- `ItemAddedToCartEvent` → Recalculate discounts
- `CartUpdatedEvent` → Update pricing

**Events Published**:
- `PromotionAppliedEvent` — Discount applied
- `PriceRecalculatedEvent` — Price changed

**Caching**:
- Promotion rules cached in Redis
- Pricing engine uses cached rules

**Database Tables**:
- `promotions`, `promo_codes`, `pricing_rules`

---

### 6. Templates Module

**Responsibility**: Dynamic page templating and content rendering.

**Endpoints**:
```
GET    /templates/{pageId}          # Get page template
GET    /templates/pages             # List available pages
POST   /templates/{pageId}          # Update page content
```

**Key Entities**:
- `Template` — Page template definition
- `TemplateBlock` — Reusable content blocks
- `PageConfiguration` — Page-specific settings

**Events Listened To**:
- `ProductCreatedEvent` → Update catalog page
- `PromotionAppliedEvent` → Update pricing display

**Events Published**:
- `TemplateUpdatedEvent` — Template changed

**Caching**:
- Rendered pages cached in Redis
- TTL: 1 hour for static content

**Database Tables**:
- `templates`, `template_blocks`, `page_configurations`

---

## API Reference

### Base URL

```
http://localhost:8000
```

### Authentication

All endpoints (except `/users/register` and `/users/login`) require JWT token:

```http
Authorization: Bearer <jwt_token>
```

### Common Response Format

**Success (200)**:
```json
{
  "status": "success",
  "data": {
    "id": "123",
    "name": "Product Name",
    ...
  }
}
```

**Error (4xx, 5xx)**:
```json
{
  "status": "error",
  "error": "NOT_FOUND",
  "message": "Product with id 123 not found",
  "timestamp": "2026-05-31T10:30:00Z"
}
```

### Swagger Documentation

**Interactive API Docs**: http://localhost:8000/swagger-ui.html

**OpenAPI JSON**: http://localhost:8000/v3/api-docs

---

## Database

### Schema Overview

```sql
-- Catalog
products (id, name, description, price, category_id, created_at, updated_at)
categories (id, slug, name, description)
inventory (id, product_id, quantity, reserved, available)

-- Users
users (id, email, password_hash, first_name, last_name, created_at)
roles (id, name)
user_roles (user_id, role_id)

-- Cart
carts (id, user_id, created_at, updated_at)
cart_items (id, cart_id, product_id, quantity)

-- Orders
orders (id, user_id, total_amount, status, created_at)
order_items (id, order_id, product_id, quantity, price)
order_status_history (id, order_id, status, changed_at)
payments (id, order_id, method, status, amount)

-- Promotions
promotions (id, name, discount_percent, start_date, end_date)
promo_codes (id, code, promotion_id, max_uses, used_count)

-- Templates
templates (id, name, content, page_id)
template_blocks (id, name, content, reusable)
```

### Migrations

Flyway manages database schema changes. Migrations are versioned:

```bash
# View migrations
ls src/main/resources/db/migration/

# Run migrations
./gradlew bootRun  # Automatically runs on startup

# Check status
./gradlew flywayInfo
```

### Connection

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/e_recadero
    username: postgres
    password: password
  jpa:
    show-sql: false  # Set to true for debugging
```

---

## Caching & Performance

### Redis Setup

```bash
# Start Redis (via Docker Compose)
docker-compose up -d redis

# Or install locally and start manually
redis-server
```

### Cache Configuration

```yaml
# application.yml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
```

### Cached Data

| Data | Key Pattern | TTL |
|------|------------|-----|
| Products | `catalog:products:{id}` | 1 hour |
| Categories | `catalog:categories:{id}` | 1 hour |
| Cart | `cart:user:{userId}` | 30 days |
| Promotions | `promotions:active` | 15 minutes |
| Templates | `template:page:{pageId}` | 1 hour |
| Sessions | `session:{sessionId}` | 24 hours |

### Performance Targets

| Operation | Target | Notes |
|-----------|--------|-------|
| GET /products | <100ms | Cached, lightweight |
| GET /products/{id} | <150ms | Cached |
| POST /cart/items | <200ms | Redis write |
| POST /orders | <300ms | Database transaction |
| Full-text search | <500ms | Indexed, paginated |
| API p95 latency | <200ms | All endpoints |

---

## Development

### Local Setup

```bash
# Clone & setup
git clone https://github.com/srioboo/e-recadero.git
cd e-recadero/back

# Start services
docker-compose up -d postgres redis

# Build & run
./gradlew bootRun
```

### IDE Setup (IntelliJ IDEA)

1. Open project: `File → Open → e-recadero/back/build.gradle.kts`
2. Mark `src/main/java` as Sources Root
3. Install Spring Boot plugin: `Preferences → Plugins → Search "Spring Boot"`
4. Create run config: `Run → Edit Configurations → + Spring Boot`

### IDE Setup (VS Code)

1. Install extensions:
   - Spring Boot Extension Pack
   - Gradle for Java
   - Java Extension Pack

2. Open folder: `File → Open Folder → e-recadero/back`

### Common Tasks

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run with debugging
./gradlew bootRun --debug

# Run specific profile
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# Code formatting
./gradlew spotlessApply

# Code quality checks
./gradlew checkstyle spotbugs

# View dependency tree
./gradlew dependencies
```

### Adding a New Endpoint

1. Create controller in module: `src/main/java/org/sirantar/e_recadero/{module}/api/{ModuleController}.java`

```java
@RestController
@RequestMapping("/api/{module}")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService service;
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(service.getProduct(id));
    }
}
```

2. Create service: `src/main/java/org/sirantar/e_recadero/{module}/application/{ModuleService}.java`

```java
@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository repository;
    
    public ProductDTO getProduct(String id) {
        return repository.findById(id)
            .map(ProductDTO::from)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
```

3. Create repository: `src/main/java/org/sirantar/e_recadero/{module}/infrastructure/{ModuleRepository}.java`

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategory(String category);
}
```

4. Create test: `src/test/java/org/sirantar/e_recadero/{module}/{ModuleControllerIT}.java`

---

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests CatalogServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

**Example Unit Test**:

```java
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock
    private ProductRepository repository;
    
    @InjectMocks
    private CatalogService service;
    
    @Test
    void shouldReturnProduct() {
        // Arrange
        var product = new Product("id", "name", 10.0);
        when(repository.findById("id")).thenReturn(Optional.of(product));
        
        // Act
        var result = service.getProduct("id");
        
        // Assert
        assertThat(result.getName()).isEqualTo("name");
    }
}
```

### Integration Tests

```bash
# Run integration tests (marked with IT suffix)
./gradlew test --tests "*IT"

# Uses Testcontainers for PostgreSQL
```

**Example Integration Test**:

```java
@SpringBootTest
@Testcontainers
class CatalogControllerIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15")
    );
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldGetProduct() throws Exception {
        mockMvc.perform(get("/catalog/products/123"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.data.name").exists()
            );
    }
}
```

### Contract Tests

```bash
# Run contract tests
./gradlew test --tests "*Contract"
```

These verify module boundaries and API contracts.

### Test Coverage

```bash
# Generate coverage report
./gradlew jacocoTestReport

# Open report
open build/reports/jacoco/test/html/index.html
```

**Target**: 80%+ coverage

---

## Configuration

### Profiles

| Profile | Use Case | Key Settings |
|---------|----------|--------------|
| `dev` | Local development | PostgreSQL (Docker Compose), debug logging |
| `test` | Automated tests | PostgreSQL via Testcontainers, reset DB per test |
| `prod` | Production | PostgreSQL, Redis, optimized settings |

### Environment Variables

```bash
# Spring profiles
SPRING_PROFILES_ACTIVE=prod

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/e_recadero
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>

# Redis
SPRING_REDIS_HOST=<host>
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=<password>

# Application
SERVER_PORT=8000
SERVER_SERVLET_CONTEXT_PATH=/

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_ORG_SIRANTAR=DEBUG

# Security
SECURITY_JWT_SECRET=<32-char-secret-key>
SECURITY_JWT_EXPIRATION=86400000  # 24 hours in ms
```

### application.yml (Base)

```yaml
spring:
  application:
    name: e-recadero
    version: 0.0.1-SNAPSHOT
  
  datasource:
    url: jdbc:postgresql://localhost:5432/e_recadero
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for migrations
    show-sql: false
  
  cache:
    type: redis
  
  redis:
    host: localhost
    port: 6379

server:
  port: 8000
  servlet:
    context-path: /

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check Redis is running
docker ps | grep redis

# View logs
./gradlew bootRun --args='--debug'
```

### Database Migration Fails

```bash
# View Flyway status
./gradlew flywayInfo

# Reset database (DEV ONLY)
./gradlew flywayClean flywayMigrate
```

### Tests Fail Locally

```bash
# Ensure Docker is running (for Testcontainers)
docker ps

# Clear Gradle cache
./gradlew clean

# Re-run tests
./gradlew test --rerun-tasks
```

### Cache Issues

```bash
# Clear Redis cache
redis-cli FLUSHDB

# Check Redis connection
redis-cli ping  # Should return PONG
```

### Port Already in Use

```bash
# If port 8000 is in use, find process
lsof -i :8000

# Kill process
kill -9 <PID>

# Or use different port
./gradlew bootRun --args='--server.port=8001'
```

### JWT Token Errors

```bash
# Generate new JWT secret (32 characters)
openssl rand -base64 24

# Update SECURITY_JWT_SECRET environment variable
export SECURITY_JWT_SECRET="<new-secret>"
./gradlew bootRun
```

---

## Next Steps

- 📖 Read [`../specs/002-backend-ecommerce/`](../specs/002-backend-ecommerce/) for detailed architecture
- 📋 See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for development guidelines
- 🧪 Explore test examples in `src/test/`
- 📊 Check module contracts in `../specs/002-backend-ecommerce/contracts/`

---

## Support

- 📚 [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- 🏗️ [Spring Modulith Docs](https://spring.io/projects/spring-modulith)
- 🔌 [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- 📋 [Gradle Build Tool](https://gradle.org)

---

<div align="center">

**Questions?** → [Open an Issue](https://github.com/srioboo/e-recadero/issues)

**Want to contribute?** → [Contributing Guide](../CONTRIBUTING.md)

Made with ❤️ by the E-Recadero team

</div>