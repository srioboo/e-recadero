# Quickstart Guide: Backend E-Commerce Spring Boot Development

**Date**: 2026-05-09 | **Target Environment**: macOS/Linux | **Prerequisites**: Java 21+, Docker, Maven/Gradle

## Overview

This guide sets up a local development environment for the E-Recadero backend e-commerce application built with Spring Boot 3.x and Modulith architecture.

## Prerequisites

### Required
- **Java 21+**: [Download JDK 21](https://adoptium.net/)
- **Docker & Docker Compose**: [Install Docker Desktop](https://www.docker.com/products/docker-desktop)
- **Git**: Latest version
- **IDE**: IntelliJ IDEA Community or VS Code with Extension Pack for Java

### Verify Installation
```bash
java --version        # Should output Java 21 or higher
docker --version      # Should output Docker version
docker-compose --version
```

## Project Setup

### 1. Clone Repository
```bash
cd ~/Work/e-recadero
git clone <repository-url> .
cd back/
```

### 2. Install Dependencies
Using Gradle (recommended for Modulith):
```bash
./gradlew clean build --offline  # First time, may download large artifacts (5-10 min)
```

Or with Maven:
```bash
mvn clean install -DskipTests
```

### 3. Start Infrastructure (PostgreSQL, Redis, Kafka)
```bash
# From repository root (back/)
docker-compose up -d

# Verify services are running
docker ps

# Check logs if issues
docker-compose logs postgres redis kafka
```

**Services Started**:
- PostgreSQL: `localhost:5432` (user: `erecadero`, password: `erecadero_dev`)
- Redis: `localhost:6379`
- Kafka: `localhost:9092` (broker)
- Zookeeper: `localhost:2181` (Kafka cluster manager)

### 4. Create Database & Migrations
```bash
# Connect to PostgreSQL container
docker exec -it <postgres-container-id> psql -U erecadero -d erecadero

# Or using psql locally (if installed)
psql -h localhost -U erecadero -d erecadero

# Migrations run automatically on application startup (Flyway integration)
```

### 5. Start Spring Boot Application
```bash
# From back/ directory
./gradlew bootRun

# Or with IntelliJ IDEA:
# Right-click RecaderoApplication.java → Run
```

**Expected Output**:
```
Started RecaderoApplication in XX seconds
Tomcat started on port(s): 8080
Modulith modules: catalog, users, cart, orders, promotions, templates
```

Access API: `http://localhost:8080/api/v1/`

## Project Structure

```
back/
├── src/main/java/org/sirantar/recadero/
│   ├── catalog/                 # Catalog Module
│   │   ├── api/
│   │   │   └── CategoryController.java
│   │   │   └── ProductController.java
│   │   ├── domain/
│   │   │   └── Category.java
│   │   │   └── Product.java
│   │   ├── persistence/
│   │   │   └── CategoryRepository.java
│   │   │   └── ProductRepository.java
│   │   └── service/
│   │       └── CatalogService.java
│   │       └── ProductService.java
│   ├── users/                   # Users Module
│   ├── cart/                    # Shopping Cart Module
│   ├── orders/                  # Orders Module
│   ├── promotions/              # Promotions Module
│   ├── templates/               # Templates Module
│   ├── shared/                  # Shared infrastructure (no business logic)
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   │   └── PersistenceConfig.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   └── dto/
│   │       └── ErrorResponse.java
│   └── RecaderoApplication.java # Main Spring Boot app
├── src/test/java/org/sirantar/recadero/
│   ├── catalog/                 # Unit & integration tests
│   ├── integration/             # Cross-module tests
│   └── contract/                # Contract tests
├── build.gradle.kts             # Gradle build config
├── compose.yaml                 # Docker Compose config
└── README.md
```

## Key Gradle Tasks

```bash
# Build & Test
./gradlew clean build              # Full build with tests
./gradlew test                     # Unit tests only
./gradlew test --info             # Verbose test output

# Run Application
./gradlew bootRun                  # Start Spring Boot server
./gradlew bootRun --args="--help"  # View boot arguments

# Database Migrations
./gradlew flywayMigrate            # Run pending migrations
./gradlew flywayClean              # Drop all tables (dev only!)

# Quality & Analysis
./gradlew check                    # Run spotbugs, pmd, checkstyle
./gradlew codeCoverageReport       # Generate test coverage report

# Dependencies
./gradlew dependencies             # List project dependencies
./gradlew dependencyInsight        # Analyze specific dependency

# Module Analysis (Modulith-specific)
./gradlew projectDocumentation    # Generate Modulith module docs
```

## Running Module Tests

```bash
# Test specific module
./gradlew test --tests "*CatalogServiceTest"
./gradlew test --tests "*CartModuleTest"

# Contract tests (verify module boundaries)
./gradlew test --tests "*ApplicationModuleTest"

# Integration tests (cross-module)
./gradlew test --tests "*integration*"
```

## API Testing

### Using curl
```bash
# Get all categories
curl -X GET http://localhost:8080/api/v1/categories

# Create category (requires ADMIN role)
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "slug": "electronics",
    "description": "Electronic devices"
  }'

# Get product details
curl -X GET http://localhost:8080/api/v1/products/product-uuid
```

### Using Postman
1. Open Postman
2. Import collection: `specs/002-backend-ecommerce/postman-collection.json`
3. Set environment variable: `BASE_URL=http://localhost:8080`
4. Use pre-configured requests for all endpoints

### Using OpenAPI/Swagger UI
Navigate to: `http://localhost:8080/swagger-ui.html`

## Common Development Tasks

### Add New Database Column to Existing Table
1. Create migration file: `src/main/resources/db/migration/V{VERSION}__Add_new_column.sql`
2. Restart application (migration runs automatically)

Example:
```sql
-- V2__Add_featured_flag_to_products.sql
ALTER TABLE catalog.product ADD COLUMN featured BOOLEAN DEFAULT FALSE;
CREATE INDEX idx_product_featured ON catalog.product(featured);
```

### Add New REST Endpoint
1. Create DTOs in `shared/dto/`:
   ```java
   public record ProductCreateRequest(String sku, String name, BigDecimal price) {}
   public record ProductResponse(UUID id, String sku, String name, BigDecimal price) {}
   ```

2. Create controller method in module (e.g., `catalog/api/ProductController.java`):
   ```java
   @PostMapping
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
       // Call service
       return ResponseEntity.status(201).body(response);
   }
   ```

3. Test with curl or Postman

### Handle Cross-Module Communication
```java
// Synchronous: REST call
@Transactional
public void addItemToCart(CartItem item) {
    // Call Catalog module API
    AvailabilityResponse availability = restTemplate.getForObject(
        "http://localhost:8080/api/v1/products/{id}/availability",
        AvailabilityResponse.class,
        item.getProductVariantId()
    );
    // Continue with cart logic
}

// Asynchronous: Publish event
@Transactional
public void orderConfirmed(Order order) {
    // Publish event; listeners (Email, Fulfillment) react independently
    applicationEventPublisher.publishEvent(new OrderConfirmedEvent(this, order));
}

// Listen to event
@Component
@EventListener(OrderConfirmedEvent.class)
public void handleOrderConfirmed(OrderConfirmedEvent event) {
    // Send confirmation email, trigger fulfillment, etc.
}
```

### Debugging

**Enable Debug Logging**:
```yaml
# application.yml
logging:
  level:
    org.sirantar.recadero: DEBUG
    org.springframework.web: DEBUG
    org.hibernate: DEBUG
```

**Debug in IDE**:
1. IntelliJ: Click Run menu → Debug RecaderoApplication
2. Set breakpoints by clicking line number gutter
3. Inspect variables in Debug panel

**Check Module Boundaries**:
```bash
./gradlew test --tests "*ApplicationModuleTest"
# Failure indicates module violated Modulith constraints
```

## Performance Testing

### Load Testing with k6
```bash
# Install k6: https://k6.io/docs/getting-started/installation/

# Run basic load test (100 concurrent users, 30 second ramp-up)
k6 run load-tests/checkout-flow.js

# Expected output shows request metrics, error rate
```

### Database Query Optimization
```sql
-- Identify slow queries (log_min_duration_ms = 1000 in PostgreSQL)
EXPLAIN ANALYZE SELECT * FROM catalog.product WHERE category_id = 'uuid' AND status = 'PUBLISHED';

-- Add index if full scan detected
CREATE INDEX CONCURRENTLY idx_product_category_status ON catalog.product(category_id, status);
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port 8080 already in use** | `lsof -i :8080` to find process, then kill it or change `server.port` in `application.yml` |
| **PostgreSQL connection refused** | Verify Docker container running: `docker ps`. Restart: `docker-compose up -d postgres` |
| **Out of memory during build** | Increase Gradle heap: `export JAVA_OPTS="-Xmx2g"` |
| **Module visibility error** | Module trying to access private component. Check `package-info.java` module definition |
| **Flyway migration fails** | Check `src/main/resources/db/migration/` for SQL errors. Rollback: `docker-compose down -v` (clears DB) |
| **Tests fail locally but pass in CI** | Check timezone differences; use `@MockTime` for deterministic dates |

## Next Steps

1. **Read Implementation Plan**: [plan.md](plan.md)
2. **Study Module Contracts**: [catalog-contract.md](contracts/catalog-contract.md), etc.
3. **Review Data Model**: [data-model.md](data-model.md)
4. **Run Tests**: `./gradlew test` to verify setup
5. **Create First Feature**: Follow [tasks.md](tasks.md) for actionable implementation tasks

## References

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring Modulith Docs**: https://spring.io/projects/spring-modulith
- **Spring Security**: https://spring.io/projects/spring-security
- **JPA/Hibernate**: https://hibernate.org/orm/documentation/
- **PostgreSQL**: https://www.postgresql.org/docs/current/
- **Postman**: https://www.postman.com/product/api-client/

## Support

- **Slack**: #backend-development
- **GitHub Issues**: [E-Recadero Issues](https://github.com/sirantar/e-recadero/issues)
- **Local Docs**: See `README.md` in each module for module-specific guidance

