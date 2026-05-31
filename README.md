# E-Recadero 🛍️

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=java)](https://www.java.com)
[![Node.js](https://img.shields.io/badge/Node.js-18%2B-green?logo=node.js)](https://nodejs.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Astro](https://img.shields.io/badge/Astro-4.0-purple?logo=astro)](https://astro.build)

**A modern, modular e-commerce platform built for performance and scalability.**

[Features](#features) • [Quick Start](#quick-start) • [Architecture](#architecture) • [Development](#development) • [Contributing](#contributing)

</div>

---

## Table of Contents

- [About E-Recadero](#about-e-recadero)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Development](#development)
- [Architecture](#architecture)
- [Deployment](#deployment)
- [Testing](#testing)
- [Contributing](#contributing)
- [Documentation](#documentation)
- [License & Support](#license--support)

---

## About E-Recadero

**E-Recadero** is a full-stack e-commerce platform designed with modern development practices in mind. It showcases a **monorepo architecture** combining a high-performance frontend with a modular, enterprise-grade backend.

### What is E-Recadero?

E-Recadero is a shopping store application that enables businesses to:

- 📦 **Manage Product Catalogs** — Organize products with categories, inventory tracking, and real-time availability
- 🛒 **Shopping Cart & Checkout** — Seamless user experience with order management
- 👥 **User Management** — Secure authentication and user profiles
- 📊 **Admin Dashboard** — Monitor orders, products, and promotions
- 🎯 **Promotions & Discounts** — Dynamic pricing and campaign management
- 📱 **Responsive Design** — Works on desktop, tablet, and mobile devices

### Project Highlights

| Aspect | Details |
|--------|---------|
| **Frontend** | Ultra-fast Astro static generation + partial hydration, mobile-first responsive design |
| **Backend** | Modular Spring Boot 3.x with strict module boundaries (Modulith architecture) |
| **Database** | PostgreSQL for data, Redis for caching & sessions |
| **Performance** | Target <200ms API response times (p95), search results <500ms |
| **Quality** | 80%+ test coverage, zero circular dependencies, PCI DSS compliance ready |

---

## Features

- ⚡ **Ultra-Fast Frontend** — Astro static generation + partial hydration for optimal performance
- 📱 **Mobile-First Design** — Fully responsive across all devices
- 🌍 **Internationalization** — Support for English & Spanish
- 🔒 **Secure Backend** — Spring Security, OAuth2 resource server, JWT tokens
- 🏗️ **Modular Architecture** — 6 independent modules with clean boundaries (Modulith)
- 📦 **Real-Time Inventory** — Track product availability and stock levels
- 🎯 **Advanced Search** — Full-text catalog search with filtering
- 💾 **Caching Layer** — Redis integration for performance
- 📊 **Analytics Ready** — SEO-optimized, performance-tracked
- ♿ **Accessibility-First** — WCAG compliance for inclusive design
- 🧪 **Comprehensive Testing** — Unit, integration, contract, and E2E tests
- 🐳 **Docker Support** — Easy deployment with Docker Compose

---

## Tech Stack

### Frontend

| Layer | Technology |
|-------|-----------|
| **Framework** | [Astro 4.x](https://astro.build) |
| **Language** | TypeScript 5.x |
| **Testing** | Vitest (unit), Playwright (E2E) |
| **Package Manager** | npm 9.x+ |

**Applications**:
- **Public Website** (`/front`) — Customer-facing store
- **Admin Dashboard** (`/admin`) — Management interface

### Backend

| Layer | Technology |
|-------|-----------|
| **Framework** | [Spring Boot 3.4.3](https://spring.io/projects/spring-boot) |
| **Language** | Java 21+ |
| **Architecture** | [Spring Modulith](https://spring.io/projects/spring-modulith) |
| **Database** | PostgreSQL 15+ (ORM: Spring Data JPA) |
| **Caching** | Redis |
| **Message Broker** | Kafka / RabbitMQ (event streams) |
| **Testing** | JUnit 5, Mockito, Testcontainers |

**Modules** (6 independent, event-driven):
- **Catalog** — Products, categories, inventory
- **Users** — Authentication, profiles
- **Shopping Cart** — Cart management
- **Orders** — Order processing
- **Promotions** — Pricing, discounts
- **Templates** — Page templating & dynamic rendering

### DevOps

- **Container Runtime** — Docker
- **Orchestration** — Docker Compose
- **Build Tools** — Gradle (backend), npm (frontend)
- **CI/CD Ready** — GitHub Actions configuration included

---

## Prerequisites

Before getting started, ensure you have the following installed:

### Required

- **Git** 2.37+ — [Download](https://git-scm.com)
- **Node.js** 18.x+ — [Download](https://nodejs.org) _(required for frontend)_
- **Java** 21+ — [Download](https://www.oracle.com/java/technologies/downloads/)  _(required for backend)_
- **Docker** & **Docker Compose** — [Download](https://www.docker.com/products/docker-desktop)

### Optional

- **VS Code** with extensions: Astro, Spring Boot Extension Pack, Thunder Client
- **Postman** or **Insomnia** for API testing
- **DBeaver** or **pgAdmin** for database inspection

### Verify Installation

```bash
# Check versions
node --version      # Should be 18.x or higher
npm --version       # Should be 9.x or higher
java --version      # Should be 21 or higher
git --version       # Should be 2.37+
docker --version    # Should be 20.10+
```

---

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/srioboo/e-recadero.git
cd e-recadero
```

### 2. Install Dependencies

```bash
# Frontend (public website)
cd front && npm install && cd ..

# Frontend (admin dashboard)
cd admin && npm install && cd ..

# Backend
cd back && ./gradlew build && cd ..
```

### 3. Configure Environment

Create `.env.local` files in each directory:

**`front/.env.local`**:
```env
VITE_API_URL=http://localhost:8000
```

**`admin/.env.local`**:
```env
VITE_API_URL=http://localhost:8000
```

**`back/application-local.yml`** _(already configured for local development)_:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/e_recadero
    username: postgres
    password: password
  redis:
    host: localhost
    port: 6379
```

### 4. Start Services

#### Option A: Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, Redis, backend, frontend)
docker-compose up -d

# Wait for services to be healthy (~30 seconds)
docker-compose ps
```

**Access points**:
- 🌐 **Public Website**: http://localhost:3001
- 📊 **Admin Dashboard**: http://localhost:3000
- 🔌 **Backend API**: http://localhost:8000
- 📚 **API Docs (Swagger)**: http://localhost:8000/swagger-ui.html

#### Option B: Running Locally (Advanced)

**Terminal 1 — Database & Cache**:
```bash
# Start PostgreSQL and Redis (requires Docker)
docker-compose up -d postgres redis
```

**Terminal 2 — Backend**:
```bash
cd back
./gradlew bootRun
# Listens on http://localhost:8000
```

**Terminal 3 — Frontend**:
```bash
cd front
npm run dev
# Listens on http://localhost:3001
```

**Terminal 4 — Admin**:
```bash
cd admin
npm run dev
# Listens on http://localhost:3000
```

### 5. Verify Everything Works

```bash
# Test frontend
curl http://localhost:3001

# Test backend health
curl http://localhost:8000/actuator/health

# Test database connectivity
# Visit admin panel and check DB status
```

✅ You're ready! Start building.

---

## Project Structure

```
e-recadero/
├── front/                    # Public-facing e-commerce website (Astro)
│   ├── src/
│   │   ├── pages/           # File-based routes
│   │   ├── components/      # Reusable Astro components
│   │   ├── layouts/         # Page layouts
│   │   └── lib/             # Utilities, API clients, types
│   ├── public/              # Static assets
│   ├── messages/            # i18n translations (en.json, es.json)
│   ├── package.json
│   ├── astro.config.mjs
│   └── README.md
│
├── admin/                    # Admin dashboard for management (Astro)
│   ├── src/
│   │   ├── pages/           # Admin pages
│   │   ├── components/      # Admin-specific components
│   │   ├── layouts/         # Admin layouts
│   │   └── lib/             # Admin utilities
│   ├── public/
│   ├── messages/            # i18n translations
│   ├── package.json
│   ├── astro.config.mjs
│   └── README.md
│
├── back/                     # Backend API (Spring Boot 3.x + Modulith)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/sirantar/e_recadero/
│   │   │   │   ├── catalog/         # Catalog module
│   │   │   │   ├── users/           # Users module
│   │   │   │   ├── cart/            # Shopping cart module
│   │   │   │   ├── orders/          # Orders module
│   │   │   │   ├── promotions/      # Promotions module
│   │   │   │   ├── templates/       # Templates module
│   │   │   │   └── shared/          # Shared utilities
│   │   │   └── resources/           # Configuration, SQL migrations
│   │   └── test/                    # Integration & unit tests
│   ├── build.gradle.kts
│   ├── gradle/
│   ├── Dockerfile
│   ├── compose.yaml                # Docker Compose services
│   └── README.md                    # Backend-specific docs
│
├── specs/                    # Feature specifications & architecture
│   ├── 001-migrate-svelte-astro/    # Frontend migration spec
│   └── 002-backend-ecommerce/       # Backend architecture spec
│
├── scripts/                  # Helper scripts (dev, build, deploy)
├── admin/                    # Configuration & constants
├── CONTRIBUTING.md           # Contribution guidelines
├── DELIVERY_TIMELINE.md      # Project roadmap
├── docker-compose.yml
├── compose.yaml
└── README.md                 # This file

```

---

## Development

### Setup Local Development

Follow the **Quick Start** guide above. For detailed development workflows:

- 📖 **Frontend Development** → See [`front/README.md`](front/README.md)
- 📖 **Admin Development** → See [`admin/README.md`](admin/README.md)
- 📖 **Backend Development** → See [`back/README.md`](back/README.md)
- 📖 **Contributing** → See [`CONTRIBUTING.md`](CONTRIBUTING.md)

### Available Scripts

#### Frontend (Public Website)

```bash
cd front

npm run dev          # Start dev server (http://localhost:3001)
npm run build        # Production build to dist/
npm run preview      # Preview production build
npm run check        # TypeScript type checking
npm test             # Unit tests (Vitest)
npm run test:e2e     # E2E tests (Playwright)
```

#### Admin Dashboard

```bash
cd admin

npm run dev          # Start dev server (http://localhost:3000)
npm run build        # Production build
npm run preview      # Preview production build
npm run check        # TypeScript type checking
npm test             # Unit tests
npm run test:e2e     # E2E tests
```

#### Backend

```bash
cd back

./gradlew bootRun            # Start Spring Boot app
./gradlew build              # Build project
./gradlew test               # Run all tests
./gradlew test --tests "*IT" # Run integration tests only
./gradlew checkstyle         # Code quality checks
./gradlew jacoco             # Code coverage report
```

### Environment Configuration

#### Backend (Spring Boot)

Create `back/application-local.yml`:

```yaml
spring:
  application:
    name: e-recadero
  datasource:
    url: jdbc:postgresql://localhost:5432/e_recadero
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: validate  # Use migrations (Flyway) instead
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
```

Activate with: `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`

#### Frontend

Create `front/.env.local`:

```env
VITE_API_URL=http://localhost:8000
VITE_API_TIMEOUT=5000
```

### Database Migrations

Backend uses **Flyway** for schema management:

```bash
cd back

# Migrations are in src/main/resources/db/migration/
# They run automatically on startup

# View migration status
./gradlew flywayInfo

# Reset database (dev only!)
./gradlew flywayClean flywayMigrate
```

### Debugging Tips

**Backend (VS Code)**:
1. Install "Spring Boot Extension Pack"
2. Set breakpoints in Java files
3. Run: `./gradlew bootRun --debug`
4. Attach debugger via "Run and Debug" (Ctrl+Shift+D)

**Frontend**:
```bash
# Chrome DevTools debugging
npm run dev

# In browser console
console.log(import.meta.env)
```

**Database**:
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres

# Inspect tables
\dt
SELECT * FROM products LIMIT 10;
```

---

## Architecture

### Monorepo Structure

E-Recadero follows a **monorepo pattern** where all services are versioned together:

```
┌─────────────────────────────────────────────────┐
│                  E-Recadero Monorepo            │
│                                                 │
│  ┌──────────────┐  ┌──────────────┐ ┌────────┐ │
│  │   Frontend   │  │  Admin       │ │ Backend│ │
│  │   (Astro)    │  │  (Astro)     │ │(Spring)│ │
│  │              │  │              │ │        │ │
│  │ - Products   │  │ - Orders     │ │ 6 Mods │ │
│  │ - Cart       │  │ - Analytics  │ │ + REST │ │
│  │ - Auth       │  │ - Settings   │ │ + Events
│  └──────┬───────┘  └──────┬───────┘ └────┬───┘ │
│         │                 │               │    │
│         └─────────────────┴───────────────┘    │
│                    ↓                           │
│         Shared API contracts & types          │
│         (in front/lib, admin/lib)             │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Module Boundaries (Backend Modulith)

Each backend module is **independently testable** and communicates via:

- **REST APIs** — Public HTTP endpoints
- **Domain Events** — Async module-to-module communication
- **Shared Types** — Contracts defined in module interfaces

Example: Adding item to cart triggers `ItemAddedToCartEvent` → Promotions module listens and calculates discounts.

### Data Flow

```
User                Frontend              Backend              Database
 │                    │                     │                    │
 ├─ Browse products ──│                     │                    │
 │                    ├─ GET /products ────→│                    │
 │                    │                     ├─ Query catalog ───→│
 │                    │                     │ (Redis cache)      │
 │                    │←─ Product list ─────│←─ Cached results ──│
 │←─ Display products─│                     │                    │
 │                    │                     │                    │
 ├─ Add to cart ──────│                     │                    │
 │                    ├─ POST /cart ───────→│                    │
 │                    │                     ├─ Update cart state │
 │                    │                     ├─ Emit event ──→ [Promotions]
 │                    │                     │                    │
 │                    │←─ Cart updated ─────│←─ Apply promotions │
 │←─ Show discount ───│                     │                    │
```

---

## Deployment

### Docker Compose (Recommended for Development)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down

# Remove volumes (WARNING: deletes data)
docker-compose down -v
```

### Production Deployment

For production deployment instructions, see:

- **Backend**: [`back/DOCKER_COMPOSE.md`](back/DOCKER_COMPOSE.md)
- **Frontend**: [`front/DEPLOYMENT.md`](front/DEPLOYMENT.md)
- **Admin**: `admin/DEPLOYMENT.md` (TBD)

### Environment Variables (Production)

**Backend**:
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-prod:5432/e_recadero
SPRING_DATASOURCE_USERNAME=<SECRET>
SPRING_DATASOURCE_PASSWORD=<SECRET>
SPRING_REDIS_HOST=redis-prod
SPRING_REDIS_PORT=6379
```

**Frontend**:
```bash
VITE_API_URL=https://api.e-recadero.com
VITE_ANALYTICS_ID=GA-XXXXX
```

---

## Testing

### Unit Tests

```bash
# Frontend
cd front && npm test

# Backend
cd back && ./gradlew test
```

### Integration Tests

```bash
# Backend (uses Testcontainers for PostgreSQL + Redis)
cd back && ./gradlew test --tests "*IT"
```

### E2E Tests

```bash
# Frontend
cd front && npm run test:e2e

# Admin
cd admin && npm run test:e2e
```

### Code Coverage

```bash
# Backend (generates report in build/reports/jacoco)
cd back && ./gradlew jacocoTestReport

# Frontend
cd front && npm test -- --coverage
```

---

## Contributing

We welcome contributions! 🎉

### Before You Start

1. **Read** [`CONTRIBUTING.md`](CONTRIBUTING.md) for detailed guidelines
2. **Check** existing [GitHub Issues](https://github.com/srioboo/e-recadero/issues)
3. **Discuss** large features via Issues first

### Quick Contribution Workflow

```bash
# 1. Create feature branch
git checkout -b feature/your-feature-name

# 2. Make changes and commit
git add .
git commit -m "feat(frontend): add feature description"

# 3. Push branch
git push origin feature/your-feature-name

# 4. Open Pull Request on GitHub
```

### Code Standards

- ✅ TypeScript for frontend (strict mode)
- ✅ Java 21+ with Spring conventions
- ✅ Unit tests for new features
- ✅ ESLint & Prettier (frontend)
- ✅ Checkstyle & SpotBugs (backend)

### Development Commands

```bash
# Type checking (frontend)
cd front && npm run check

# Linting (if configured)
npm run lint

# Code quality (backend)
cd back && ./gradlew checkstyle spotbugs
```

---

## Documentation

### Project Specifications

- 📄 **Frontend Migration** — [`specs/001-migrate-svelte-astro/`](specs/001-migrate-svelte-astro/)
  - Implementation plan, research, data model, contracts
- 📄 **Backend Architecture** — [`specs/002-backend-ecommerce/`](specs/002-backend-ecommerce/)
  - Modulith architecture, module contracts, technical decisions

### Module Documentation

- 📖 **Frontend** — [`front/README.md`](front/README.md)
- 📖 **Admin** — [`admin/README.md`](admin/README.md)
- 📖 **Backend** — [`back/README.md`](back/README.md)

### Additional Guides

- 📋 **Contributing** — [`CONTRIBUTING.md`](CONTRIBUTING.md)
- 📅 **Delivery Timeline** — [`DELIVERY_TIMELINE.md`](DELIVERY_TIMELINE.md)
- 🔧 **Backend Deployment** — [`back/DOCKER_COMPOSE.md`](back/DOCKER_COMPOSE.md)
- 🚀 **Frontend Deployment** — [`front/DEPLOYMENT.md`](front/DEPLOYMENT.md)

### API Documentation

Once backend is running:

- 🔌 **Swagger UI** — http://localhost:8000/swagger-ui.html
- 📚 **OpenAPI JSON** — http://localhost:8000/v3/api-docs

---

## License & Support

### License

E-Recadero is licensed under the **MIT License**. See [`LICENSE`](LICENSE) for details.

### Getting Help

| Channel | Purpose |
|---------|---------|
| 📋 **GitHub Issues** | Bug reports, feature requests |
| 💬 **GitHub Discussions** | Questions, ideas, community |
| 📧 **Email** | Critical issues (maintainers) |
| 📚 **Documentation** | Setup, development, deployment |

### Security

If you discover a security vulnerability, please email the maintainers instead of using GitHub Issues.

---

## Acknowledgments

**Built with** ❤️ using:
- [Astro](https://astro.build) for ultra-fast frontends
- [Spring Boot](https://spring.io/projects/spring-boot) for robust backends
- [Spring Modulith](https://spring.io/projects/spring-modulith) for modular architecture
- [PostgreSQL](https://www.postgresql.org) for reliable data
- [Redis](https://redis.io) for high-speed caching

**Contributors**: See [`CONTRIBUTING.md`](CONTRIBUTING.md#recognition) for recognition

---

<div align="center">

**Ready to get started?** → [Quick Start](#quick-start)

**Want to contribute?** → [Contributing Guide](CONTRIBUTING.md)

**Questions?** → [Open an Issue](https://github.com/srioboo/e-recadero/issues)

Made with ❤️ by the E-Recadero team

</div>
