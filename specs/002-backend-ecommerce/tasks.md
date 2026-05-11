# Tasks: Backend E-Commerce Spring Boot Application with Modulith

**Input**: Design documents from `/specs/002-backend-ecommerce/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅  
**Organization**: Tasks grouped by module (independent implementation paths)  
**Format**: `[TaskID] [Optional P flag] [Optional Story label] Description with file paths`

---

## Priority & Sequencing

**Critical Path** (must complete in order):
1. Phase 1: Setup
2. Phase 2: Foundational
3. Phases 3-8: Modules (can execute in parallel after Phase 2)
4. Phase 9: Integration & Polish

**Estimated Timeline**: 8 weeks (5 developers, 40 hrs/week)  
**MVP Scope**: Phases 1-2 + Catalog + Users + Cart + Orders (Weeks 1-6)  
**Extended Scope**: Add Promotions + Templates (Weeks 7-8)

---

## Phase 1: Setup & Project Initialization

**Purpose**: Establish Java 21 + Spring Boot 3.3.x + Modulith project structure

- [x] T001 Create Spring Boot 3.3.x project with Java 21 and Gradle 8.x build file
- [x] T002 [P] Configure build.gradle.kts with dependencies: Spring Modulith, Spring Data JPA, Spring Security, Spring Web
- [x] T003 [P] Add Gradle plugins: Modulith module documentation, code coverage (JaCoCo), spotbugs, checkstyle
- [x] T004 [P] Create Docker Compose (compose.yaml) with PostgreSQL, Redis, Kafka, Zookeeper containers
- [x] T005 [P] Setup Flyway database migration framework in build.gradle.kts
- [x] T006 [P] Configure application.yml with Spring profiles (dev, test, prod)
- [x] T007 Create RecaderoApplication.java main Spring Boot entry point with @SpringBootApplication

**Checkpoint**: Project builds without errors; `./gradlew bootRun` starts successfully on localhost:8080

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that enables all module implementations

**⚠️ CRITICAL**: Phases 3-8 cannot begin until ALL Phase 2 tasks complete

### Database & Migrations

- [x] T008 Create database schemas in Flyway:
  - src/main/resources/db/migration/V1__create_schemas.sql
  - V2__create_catalog_schema.sql
  - V3__create_users_schema.sql
  - V4__create_cart_schema.sql
  - V5__create_orders_schema.sql
  - V6__create_promotions_schema.sql
  - V7__create_templates_schema.sql
- [x] T009 [P] Create shared sequence generators and default functions in V1 migration
- [x] T010 Setup Flyway config in application.yml (locations, encoding, validation)
- [x] T011 Run migrations locally: `./gradlew flywayMigrate` (verify all 7 schemas created)

### Security & Authentication

- [x] T012 Create SecurityConfig.java in src/main/java/.../recadero/shared/config/ with:
  - Security filter chain (CORS, CSRF, session config)
  - JWT authentication filter
  - OAuth2 resource server configuration
- [x] T013 [P] Create JwtTokenProvider.java utility class for token generation/validation (JWT 15-min access + 7-day refresh)
- [x] T014 [P] Create GlobalExceptionHandler.java in shared/exception/ with error response formatting
- [x] T015 [P] Create ErrorResponse.java DTO in shared/dto/
- [x] T016 Create SecurityUser.java in shared/security/ for Spring Security principal
- [x] T017 [P] Add role-based access control annotations (@PreAuthorize, @PostAuthorize)

### API Infrastructure

- [ ] T018 Create ApiController base class in shared/api/ with pagination, error handling boilerplate
- [ ] T019 [P] Create PaginationRequest.java and PaginationResponse.java in shared/dto/
- [ ] T020 [P] Configure Jackson ObjectMapper for JSON serialization (LocalDateTime format, null handling)
- [ ] T021 [P] Setup Spring MVC configuration (message converters, interceptors)
- [ ] T022 Create OpenAPI/Swagger configuration (SpringDoc OpenAPI with @OpenAPIDefinition)

### Shared Infrastructure

- [ ] T023 [P] Configure HikariCP connection pooling in application.yml (pool size = 9 for 4-core)
- [ ] T024 [P] Setup Redis client and RedisTemplate in PersistenceConfig.java
- [ ] T025 [P] Configure Kafka (Spring Cloud Stream) for event publishing/listening
- [ ] T026 [P] Create LoggingConfig.java with SLF4J + Logback configuration (debug dev, warn prod)
- [ ] T027 [P] Setup Actuator endpoints (/actuator/health, /actuator/metrics) for monitoring

### Testing Framework

- [ ] T028 [P] Configure JUnit 5, Mockito, AssertJ, Testcontainers in build.gradle.kts
- [ ] T029 [P] Create TestcontainersConfiguration.java for PostgreSQL + Redis test containers
- [ ] T030 [P] Create @SpringBootTest base test class with common setup
- [ ] T031 [P] Create JwtTokenTestUtils.java for generating test JWT tokens
- [ ] T032 Create test profile configuration (application-test.yml)

**Checkpoint**: `./gradlew test` runs successfully; Testcontainers spin up/down cleanly; 0 test failures on empty classes

---

## Phase 3: Catalog Module

**Goal**: Product and category management with full CRUD, search, and inventory tracking

**Independent Test**: `./gradlew test --tests "*catalog*"` passes all unit, integration, and contract tests

### Data Models & Repositories

- [ ] T033 [P] Create Category.java entity in back/src/main/java/.../catalog/domain/
  - Includes: id, name, slug, description, parent_category_id, image_url, status, created_at, updated_at
  - JPA annotations: @Entity, @Table(schema="catalog"), @Getter, @Setter
- [ ] T034 [P] Create Product.java entity with: id, sku, name, base_price, cost_price, category_id, status, version (optimistic lock)
- [ ] T035 [P] Create ProductAttribute.java entity with: id, product_id, name, type (TEXT/SELECT/NUMBER/DATE_RANGE), values (JSON)
- [ ] T036 [P] Create ProductVariant.java entity with: id, product_id, sku (unique), variant_attributes (JSON), price, weight
- [ ] T037 [P] Create Inventory.java entity with: id, product_variant_id, warehouse_id, quantity_on_hand, reserved_quantity, reorder_level
- [ ] T038 [P] Create @ApplicationModule(displayName = "Catalog", allowedDependencies = "shared") in catalog/package-info.java
- [ ] T039 [P] Create CategoryRepository.java extending JpaRepository with custom methods:
  - findByParentIdOrderBySortOrder(UUID parentId)
  - findBySlug(String slug)
  - findAllActive()
- [ ] T040 [P] Create ProductRepository.java with:
  - findByCategoryIdAndStatus(UUID categoryId, ProductStatus status)
  - findBySku(String sku)
  - findByStatusOrderByCreatedAtDesc(ProductStatus status)
  - search(String query, UUID categoryId, Pageable pageable)
- [ ] T041 [P] Create ProductVariantRepository.java with:
  - findByProductId(UUID productId)
  - findBySkuOrderByPrice(String sku)
- [ ] T042 [P] Create InventoryRepository.java with:
  - findByProductVariantIdAndWarehouseId(UUID variantId, UUID warehouseId)
  - findByProductVariant_Id(UUID variantId)

### Services & Business Logic

- [ ] T043 Create CatalogService.java in catalog/service/ implementing:
  - createCategory(CategoryCreateRequest): CategoryResponse
  - updateCategory(UUID, CategoryUpdateRequest): CategoryResponse
  - getCategoryWithChildren(UUID): CategoryResponse with nested children
  - deleteCategoryLogical(UUID): cascades to products (mark archived, don't delete)
- [ ] T044 [P] Create ProductService.java implementing:
  - createProduct(ProductCreateRequest): ProductResponse
  - publishProduct(UUID productId): checks required fields, emits ProductPublishedEvent
  - searchProducts(query, categoryId, minPrice, maxPrice, pageable): uses full-text search
  - getProductWithVariants(UUID): returns product + all variants + images
  - updateProductPrice(UUID, newPrice): emits ProductPriceChangedEvent, clears cache
  - archiveProduct(UUID): removes from active promotions, carts, templates
- [ ] T045 [P] Create ProductVariantService.java for variant CRUD and SKU generation
- [ ] T046 [P] Create InventoryService.java implementing:
  - checkAvailability(UUID variantId, int quantity): returns available_quantity
  - reserveInventory(UUID variantId, int quantity): decrease available; throws OutOfStockException
  - releaseReservation(UUID variantId, int quantity): restore available (cart expiration, order cancel)
  - adjustInventory(UUID variantId, int change, reason): admin adjustments
- [ ] T047 [P] Create CategoryValidationService with constraints:
  - Slug must be unique, URL-safe format
  - No circular parent references (check depth ≤ 5 levels)
  - Parent must exist if parent_id provided

### Controllers & API Endpoints

- [ ] T048 Create CategoryController.java in catalog/api/ with endpoints (8 total):
  - GET /api/v1/categories (list with pagination)
  - GET /api/v1/categories/{id}
  - POST /api/v1/categories (admin only)
  - PUT /api/v1/categories/{id} (admin only)
  - DELETE /api/v1/categories/{id} (admin only, soft delete)
  - GET /api/v1/categories/{id}/children (get subcategories)
  - POST /api/v1/categories/{id}/move (change parent)
- [ ] T049 Create ProductController.java with endpoints (10 total):
  - GET /api/v1/products (search with filters)
  - GET /api/v1/products/{id}
  - GET /api/v1/products/{id}/availability (for Cart module)
  - POST /api/v1/products (admin only)
  - PUT /api/v1/products/{id} (admin only)
  - PATCH /api/v1/products/{id}/status (admin only: DRAFT → PUBLISHED → ARCHIVED)
  - DELETE /api/v1/products/{id} (admin only)
  - POST /api/v1/products/{id}/variants
  - GET /api/v1/products/{id}/variants
  - PATCH /api/v1/products/{id}/price (admin only)
- [ ] T050 [P] Create InventoryController.java with endpoints (3 total):
  - GET /api/v1/inventory/{variant_id}
  - POST /api/v1/inventory/{variant_id}/adjust (admin only)
  - GET /api/v1/inventory/low-stock (admin only, list items below reorder_level)

### Domain Events

- [ ] T051 [P] Create ProductPublishedEvent.java in catalog/events/
- [ ] T052 [P] Create ProductPriceChangedEvent.java
- [ ] T053 [P] Create ProductArchivedEvent.java
- [ ] T054 [P] Create InventoryUpdatedEvent.java
- [ ] T055 Create CatalogEventPublisher.java to emit events via ApplicationEventPublisher

### Unit Tests (80%+ coverage target)

- [ ] T056 [P] Create CategoryServiceTest.java with tests for:
  - createCategory with valid/invalid input
  - getCategory hierarchy traversal
  - deleteCategory cascade behavior
- [ ] T057 [P] Create ProductServiceTest.java with tests for:
  - publishProduct (requires category, price)
  - searchProducts filtering
  - updateProductPrice event emission
  - archiveProduct cascade
- [ ] T058 [P] Create InventoryServiceTest.java with tests for:
  - checkAvailability with in-stock/out-of-stock scenarios
  - reserveInventory (success + OutOfStockException)
  - releaseReservation (cart expiration case)

### Contract Tests (Module Boundaries)

- [ ] T059 Create CatalogApplicationModuleTest.java using @ApplicationModuleTest:
  - Verify Catalog module doesn't import from other domain modules (users, cart, orders, etc.)
  - Verify only `catalog`, `shared` imports allowed
  - Verify REST API contract matches OpenAPI spec

### Integration Tests (E2E within module)

- [ ] T060 Create CatalogIntegrationTest.java testing:
  - Create category → Create product → Publish product → Search products flow
  - Inventory reservation → release flow
  - Variant price override affecting cart calculations

**Checkpoint**: All Catalog tests pass; Coverage ≥80%; No contract violations

---

## Phase 4: Users Module

**Goal**: Authentication, user profiles, address management, role-based access control

**Independent Test**: `./gradlew test --tests "*users*"` passes all tests

### Data Models & Repositories

- [ ] T061 [P] Create User.java entity in users/domain/ with:
  - id, email (unique, indexed), password_hash, username (unique), status, email_verified, last_login_at
  - @Version field for optimistic locking on concurrent updates
- [ ] T062 [P] Create UserProfile.java entity with: id, user_id, first_name, last_name, phone, avatar_url, preferred_locale, newsletter_subscribed
- [ ] T063 [P] Create Address.java entity with: id, user_id, type (BILLING/SHIPPING/OTHER), street_address, city, state_province, postal_code, country_code, is_primary
- [ ] T064 [P] Create UserRole.java entity with: id, user_id, role_name (CUSTOMER/ADMIN/VENDOR/SUPPORT/ANALYST), granted_at, granted_by
- [ ] T065 [P] Create @ApplicationModule(displayName = "Users") in users/package-info.java
- [ ] T066 [P] Create UserRepository.java with:
  - findByEmail(String email)
  - findByUsernamne(String username)
  - findByStatusOrderByCreatedAtDesc(UserStatus status)
- [ ] T067 [P] Create AddressRepository.java with:
  - findByUserIdAndType(UUID userId, AddressType type)
  - findByUserIdAndIsPrimaryAndType(UUID userId, boolean isPrimary, AddressType type)
- [ ] T068 [P] Create UserRoleRepository.java with:
  - findByUserId(UUID userId)
  - findByUserIdAndRoleName(UUID userId, String roleName)

### Services & Business Logic

- [ ] T069 Create UserAuthenticationService.java implementing:
  - registerUser(RegisterRequest): UserResponse (generate email verification token)
  - verifyEmail(token): activate account
  - login(email, password): generate JWT access + refresh tokens (validates bcrypt password)
  - refreshToken(refreshToken): generate new access token
  - logout(refreshToken): revoke token in Redis
- [ ] T070 [P] Create UserProfileService.java implementing:
  - createProfile(userId, ProfileRequest): UserProfile
  - updateProfile(userId, ProfileRequest): updated profile
  - getProfile(userId): UserProfile with addresses
  - changePassword(userId, oldPassword, newPassword): validates current password
  - deactivateAccount(userId): mark INACTIVE (soft delete with 90-day retention)
- [ ] T071 [P] Create AddressService.java implementing:
  - createAddress(userId, AddressRequest): Address
  - updateAddress(userId, addressId, AddressRequest): Address
  - deleteAddress(userId, addressId): void
  - setPrimaryAddress(userId, addressId, type): ensure only one primary per type
  - getPrimaryAddress(userId, type): fetch default address for checkout
- [ ] T072 [P] Create UserRoleService.java implementing:
  - grantRole(userId, roleName): add role (admin only)
  - revokeRole(userId, roleName): remove role
  - hasRole(userId, roleName): boolean check
- [ ] T073 [P] Create UserValidationService with constraints:
  - Email: valid RFC 5322 + unique + lowercase
  - Password: min 12 chars, 1 uppercase, 1 digit, 1 special char
  - Username: 3-50 chars, alphanumeric + underscore
  - Phone: E.164 format if provided

### Controllers & API Endpoints

- [ ] T074 Create AuthController.java with endpoints (7 total):
  - POST /api/v1/auth/register (public)
  - POST /api/v1/auth/verify-email (public, token in body)
  - POST /api/v1/auth/login (public, email + password)
  - POST /api/v1/auth/refresh-token (public, refresh_token)
  - POST /api/v1/auth/logout (authenticated, revoke refresh token)
  - POST /api/v1/auth/forgot-password (public, send email to addr on file)
  - POST /api/v1/auth/reset-password (public, token + new password)
- [ ] T075 Create UserProfileController.java with endpoints (5 total):
  - GET /api/v1/users/me (current user profile)
  - PUT /api/v1/users/me (update profile)
  - PUT /api/v1/users/me/password (change password)
  - DELETE /api/v1/users/me (deactivate account, requires password confirmation)
  - GET /api/v1/users/me/addresses (list user addresses)
- [ ] T076 Create AddressController.java with endpoints (6 total):
  - GET /api/v1/users/me/addresses (list, filterable by type)
  - POST /api/v1/users/me/addresses (create)
  - PUT /api/v1/users/me/addresses/{id} (update)
  - DELETE /api/v1/users/me/addresses/{id} (delete)
  - PUT /api/v1/users/me/addresses/{id}/set-primary (set as default for type)
  - GET /api/v1/users/me/addresses/primary/{type} (get default address)
- [ ] T077 Create AdminUserController.java with endpoints (5 total, admin only):
  - GET /api/v1/admin/users (list all users with filters)
  - GET /api/v1/admin/users/{id} (get user details)
  - PATCH /api/v1/admin/users/{id}/status (change status: ACTIVE/INACTIVE/SUSPENDED)
  - POST /api/v1/admin/users/{id}/roles (grant role)
  - DELETE /api/v1/admin/users/{id}/roles/{roleName} (revoke role)

### Domain Events

- [ ] T078 [P] Create UserRegisteredEvent.java
- [ ] T079 [P] Create UserEmailVerifiedEvent.java
- [ ] T080 [P] Create UserProfileUpdatedEvent.java
- [ ] T081 Create UserEventPublisher.java

### Unit Tests (80%+ coverage)

- [ ] T082 [P] Create UserAuthenticationServiceTest.java with tests for:
  - registerUser validation
  - login with valid/invalid credentials
  - JWT token generation/refresh
  - password hashing (bcrypt)
- [ ] T083 [P] Create AddressServiceTest.java with tests for:
  - setPrimaryAddress prevents conflicts
  - address type filtering
- [ ] T084 [P] Create UserValidationServiceTest.java for email/password/username validators

### Contract Tests

- [ ] T085 Create UsersApplicationModuleTest.java with @ApplicationModuleTest

### Integration Tests

- [ ] T086 Create UserIntegrationTest.java testing:
  - Register → Verify email → Login → Get profile flow
  - Add addresses → Set primary flow

**Checkpoint**: All Users tests pass; Coverage ≥80%; Auth working end-to-end

---

## Phase 5: Shopping Cart Module

**Goal**: Cart management, item operations, inventory reservations, promotions application

**Independent Test**: `./gradlew test --tests "*cart*"` passes all tests

### Data Models & Repositories

- [ ] T087 [P] Create Cart.java entity in cart/domain/ with:
  - id, user_id (nullable for anonymous), session_id, status (ACTIVE/ABANDONED/CHECKED_OUT), created_at, expires_at
  - Unique constraint: (user_id, status) where status='ACTIVE' (one active cart per user)
- [ ] T088 [P] Create CartItem.java entity with: id, cart_id, product_variant_id, quantity, price_at_time, discount_applied
- [ ] T089 [P] Create Reservation.java entity with: id, cart_item_id, product_variant_id, quantity, created_at, expires_at
- [ ] T090 [P] Create CartPromotion.java entity with: id, cart_id, promotion_id, coupon_code,discount_amount
- [ ] T091 [P] Create @ApplicationModule(displayName = "Cart") in cart/package-info.java
- [ ] T092 [P] Create CartRepository.java with:
  - findActiveByUserId(UUID userId)
  - findBySessionId(String sessionId)
  - findExpiredCarts(LocalDateTime threshold)
- [ ] T093 [P] Create CartItemRepository.java with:
  - findByCartId(UUID cartId)
  - findByCartIdAndProductVariantId(UUID cartId, UUID variantId)
- [ ] T094 [P] Create ReservationRepository.java with:
  - findExpiredReservations(LocalDateTime threshold)
  - findByProductVariantId(UUID variantId) (for available qty calculation)

### Services & Business Logic

- [ ] T095 Create CartService.java implementing:
  - getOrCreateCart(userId, sessionId): Cart (returns active or creates new)
  - addItem(cartId, variantId, quantity): calls InventoryService.reserveInventory, creates Reservation
  - updateItemQuantity(cartItemId, newQuantity): adjusts reservation
  - removeItem(cartItemId): calls InventoryService.releaseReservation
  - clearCart(cartId): void
  - calculateTotals(cartId): returns {subtotal, discount_total, tax_total, shipping_total, grand_total}
- [ ] T096 [P] Create CartPromotionService.java implementing:
  - applyCoupon(cartId, couponCode): calls Promotions module POST /coupons/validate, creates CartPromotion
  - removeCoupon(cartId, couponCode): void
  - validateCoupon(cartId, couponCode): returns validation result with discount preview
- [ ] T097 [P] Create CheckoutService.java implementing:
  - prepareCheckout(cartId, shippingMethod, billingAddr, shippingAddr): verifies inventory availability, recalculates prices (flash sale corner case), locks cart
  - confirmCheckout(cartId, paymentToken, transactionId): creates Order via Orders module, clears cart, publishes CheckoutCompleted event
- [ ] T098 [P] Create CartExpirationService.java with scheduled task:
  - cleanupExpiredCarts(): every 1 hour, find carts expired >24h, release reservations, mark ABANDONED, publish CartAbandoned event
- [ ] T099 [P] Create CartValidationService validating:
  - Item quantity between 1-1000
  - Product variant exists and is in stock
  - Product price hasn't changed >10% since added (warn user on checkout)

### Controllers & API Endpoints

- [ ] T100 Create CartController.java with endpoints (12 total):
  - GET /api/v1/cart (get current user's active cart or create)
  - POST /api/v1/cart/items (add item, returns updated cart)
  - PUT /api/v1/cart/items/{id} (update quantity)
  - DELETE /api/v1/cart/items/{id} (remove item)
  - POST /api/v1/cart/clear (empty cart)
  - POST /api/v1/cart/apply-coupon (apply promo code)
  - DELETE /api/v1/cart/remove-coupon (remove coupon)
  - GET /api/v1/cart/validate-coupon (preview discount without applying)
  - POST /api/v1/cart/prepare-checkout (lock for checkout, recalc totals)
  - POST /api/v1/cart/confirm-checkout (create order, payment success handler)
  - GET /api/v1/cart/history (list past carts, filterable by status)
  - POST /api/v1/cart/recover/{old_cart_id} (restore abandoned cart items)

### Domain Events

- [ ] T101 [P] Create CartItemAddedEvent.java
- [ ] T102 [P] Create CartAbandonedEvent.java (triggers email recovery)
- [ ] T103 [P] Create CheckoutStartedEvent.java
- [ ] T104 Create CartEventPublisher.java

### Unit Tests (80%+ coverage)

- [ ] T105 [P] Create CartServiceTest.java with tests for:
  - addItem with successful reservation
  - addItem with OutOfStockException
  - updateQuantity with inventory adjustment
  - calculateTotals with discounts/taxes
- [ ] T106 [P] Create CartPromotionServiceTest.java with tests for:
  - applyCoupon (valid, invalid, expired coupons)
  - calculateDiscount edge cases (max discount cap)
- [ ] T107 [P] Create CheckoutServiceTest.java with tests for:
  - prepareCheckout price verification (flash sale detection)
  - confirmCheckout order creation

### Contract Tests

- [ ] T108 Create CartApplicationModuleTest.java validating module boundaries

### Integration Tests

- [ ] T109 Create CartIntegrationTest.java testing:
  - Add product → add quantity → apply coupon → prepare checkout → confirm checkout flow
  - Cart expiration and recovery

**Checkpoint**: Cart logic complete; E2E checkout flow testable end-to-end (pre-payment)

---

## Phase 6: Orders Module

**Goal**: Order lifecycle, payment coordination, shipment tracking, returns management

**Independent Test**: `./gradlew test --tests "*orders*"` passes all tests

### Data Models & Repositories

- [ ] T110 [P] Create Order.java entity in orders/domain/ with:
  - id, order_number (unique, human-readable), user_id, status (PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED/REFUNDED)
  - Totals: subtotal, tax_total, shipping_total, discount_total, grand_total
  - Dates: order_date, confirmed_date, shipped_date, delivered_date
  - Addresses: billing_address_id, shipping_address_id (snapshots, not FK to prevent cascade issues)
- [ ] T111 [P] Create OrderItem.java entity with: id, order_id, product_variant_id, product_sku (snapshot), quantity, unit_price, line_discount, line_total
- [ ] T112 [P] Create OrderShipment.java entity with: id, order_id, carrier, tracking_number, status (PENDING/IN_TRANSIT/OUT_FOR_DELIVERY/DELIVERED), shipped_at, estimated_delivery, delivered_at
- [ ] T113 [P] Create OrderPayment.java entity with: id, order_id, payment_method, amount, status (PENDING/AUTHORIZED/CAPTURED/FAILED/REFUNDED), transaction_id (external processor ID), processor_response (JSON)
- [ ] T114 [P] Create @ApplicationModule(displayName = "Orders") in orders/package-info.java
- [ ] T115 [P] Create OrderRepository.java with:
  - findByOrderNumber(String orderNumber)
  - findByUserId(UUID userId, Pageable)
  - findByStatus(OrderStatus status, Pageable)
- [ ] T116 [P] Create OrderShipmentRepository.java with:
  - findByTrackingNumber(String tracking)
  - findByOrderId(UUID orderId)
- [ ] T117 [P] Create OrderPaymentRepository.java with:
  - findByTransactionId(String transactionId) (external idempotency key)

### Services & Business Logic

- [ ] T118 Create OrderService.java implementing:
  - createOrderFromCart(cartId, checkoutToken, paymentInfo): called by Cart module post-checkout
    - Verify cart items still in stock (final check before order creation)
    - Create Order + OrderItems (snapshots of product info for historical record)
    - Create OrderPayment record
    - Transaction scope: SERIALIZABLE isolation for consistency
    - Emit OrderConfirmedEvent on success
  - getOrder(orderId, userId): return full order details with items + shipment
  - cancelOrder(orderId, reason): only if PENDING or CONFIRMED; trigger refund if payment captured
  - refundOrder(orderId, amount, reason): partial or full refund
- [ ] T119 [P] Create OrderShipmentService.java implementing:
  - createShipment(orderId, carrier, tracking): admin creates shipment post-pickup
  - updateShipmentStatus(tracking, status, location): webhook handler for carrier updates → emits OrderShippedEvent
  - getShipmentTracking(tracking): public endpoint for tracking (no auth required)
- [ ] T120 [P] Create OrderReturnService.java implementing:
  - initiateReturn(orderId, itemId, reason, description): creates return record, generates RMA number, generates return shipping label
  - approveReturn(returnId): admin approves, enables refund processing
  - processReturnRefund(returnId, amount): execute refund API call
- [ ] T121 [P] Create OrderValidationService with constraints:
  - Order totals = cart totals ± rounding tolerance (< 0.01)
  - Status transitions enforce business rules (no PENDING → DELIVERED direct)
  - Refunds only allowed within 30 days of delivery

### Controllers & API Endpoints

- [ ] T122 Create OrderController.java with endpoints (10 total):
  - GET /api/v1/orders (list user's orders with filtering + pagination)
  - GET /api/v1/orders/{id} (full order details)
  - PATCH /api/v1/orders/{id}/cancel (cancel if PENDING/CONFIRMED)
  - POST /api/v1/orders/{id}/refund (initiate refund)
  - GET /api/v1/orders/{id}/payment (payment details + refund history)
  - GET /api/v1/orders/{id}/shipment (shipment tracking info)
  - POST /api/v1/orders/{id}/return (initiate return)
  - GET /api/v1/orders/{id}/returns (list returns for order)
  - POST /api/v1/orders/{id}/shipment/webhook (carrier webhook handler)
  - GET /api/tracking/{tracking_number} (public shipment tracking, no auth)
- [ ] T123 Create AdminOrderController.java with endpoints (3 total, admin only):
  - GET /api/v1/admin/orders (list all orders with admin filters)
  - PATCH /api/v1/admin/orders/{id}/status (force status change, admin override)
  - POST /api/v1/admin/orders/{id}/shipment (manually create shipment record)

### Domain Events

- [ ] T124 [P] Create OrderConfirmedEvent.java (payment success)
- [ ] T125 [P] Create OrderShippedEvent.java (tracking available)
- [ ] T126 [P] Create OrderDeliveredEvent.java (delivery confirmed)
- [ ] T127 [P] Create OrderRefundedEvent.java
- [ ] T128 Create OrderEventPublisher.java

### Unit Tests (80%+ coverage)

- [ ] T129 [P] Create OrderServiceTest.java with tests for:
  - createOrderFromCart inventory verification
  - cancelOrder status transition validation
  - refundOrder 30-day window check
- [ ] T130 [P] Create OrderShipmentServiceTest.java with tests for:
  - updateShipmentStatus event emission
- [ ] T131 [P] Create OrderReturnServiceTest.java with tests for:
  - initiateReturn RMA generation
  - approveReturn refund trigger

### Contract Tests

- [ ] T132 Create OrdersApplicationModuleTest.java

### Integration Tests

- [ ] T133 Create OrderIntegrationTest.java testing:
  - Create → Confirm → Ship → Deliver → Return flow (end-to-end)

**Checkpoint**: Order system complete; payment webhook integration tested

---

## Phase 7: Promotions Module

**Goal**: Discount campaigns, coupon codes, flexible rules engine, analytics

**Independent Test**: `./gradlew test --tests "*promotions*"` passes all tests

### Data Models & Repositories

- [ ] T134 [P] Create Promotion.java entity in promotions/domain/ with:
  - id, name, type (PERCENTAGE_DISCOUNT/FIXED_DISCOUNT/FREE_SHIPPING/BOGO)
  - discount_value (0-100 for % or absolute amount), max_discount_amount (cap)
  - start_date, end_date, usage_limit, current_usage_count
  - priority, status (DRAFT/ACTIVE/PAUSED/EXPIRED/ARCHIVED)
- [ ] T135 [P] Create PromotionRule.java entity with: id, promotion_id, rule_type (PRODUCT_INCLUDE/CATEGORY_INCLUDE/USER_SEGMENT), condition_json (flexible structure)
- [ ] T136 [P] Create CouponCode.java entity with: id, promotion_id, code (unique, case-insensitive), usage_limit, current_usage, expiry_date, is_active
- [ ] T137 [P] Create PromotionUsage.java entity with: id, promotion_id, coupon_code_id, order_id, user_id, discount_amount, used_at
- [ ] T138 [P] Create @ApplicationModule(displayName = "Promotions") in promotions/package-info.java
- [ ] T139 [P] Create PromotionRepository.java with:
  - findByStatusAndBetweenDates(status, startDate, endDate)
  - findAllActive(LocalDateTime now)
  - findByPriorityDescAndStatus(status)
- [ ] T140 [P] Create CouponCodeRepository.java with:
  - findByCode(String code) (case-insensitive)
  - findExpiredCoupons(LocalDateTime threshold)

### Services & Business Logic

- [ ] T141 Create PromotionService.java implementing:
  - createPromotion(PromotionRequest): Promotion (admin only)
  - updatePromotion(promotionId, UpdateRequest): Promotion
  - changeStatus(promotionId, newStatus): validate state transitions
  - deletePromotion (archive, not hard delete)
- [ ] T142 [P] Create PromotionRulesEngine.java implementing:
  - evaluatePromotion(promotion, cartItems, user): boolean (does promotion apply to this cart?)
  - Handles all rule types: PRODUCT_INCLUDE, CATEGORY_INCLUDE, USER_SEGMENT, NEW_CUSTOMER_ONLY
  - Handles rule combinations (AND/OR operators)
  - calculateDiscount(promotion, cartSubtotal): returns discount amount respecting max_discount_amount cap
- [ ] T143 [P] Create CouponCodeService.java implementing:
  - generateCoupons(promotionId, count, prefix): bulk generation for campaign distribution
  - validateCoupon(code, cart): returns validation result (valid? applicable? reasons if not)
  - applyCoupon(code, orderId, userId, discountAmount): increment usage, mark as used
- [ ] T144 [P] Create PromotionExpirationService.java with scheduled task:
  - expirePromotions(): every hour, auto-expire promotions past end_date, publish PromotionExpiredEvent
- [ ] T145 [P] Create PromotionValidationService with constraints:
  - start_date < end_date
  - discount_value logically valid for type
  - usage_limit > 0 if set

### Controllers & API Endpoints

- [ ] T146 Create PromotionAdminController.java with endpoints (9 total, admin only):
  - GET /api/v1/promotions (list with filtering)
  - POST /api/v1/promotions (create)
  - GET /api/v1/promotions/{id}
  - PUT /api/v1/promotions/{id}
  - PATCH /api/v1/promotions/{id}/status
  - DELETE /api/v1/promotions/{id} (archive)
  - POST /api/v1/promotions/{id}/rules (add rule)
  - DELETE /api/v1/promotions/{id}/rules/{ruleId} (remove rule)
  - POST /api/v1/promotions/{id}/coupons (bulk generate codes)
- [ ] T147 Create CouponController.java with endpoints (5 total):
  - POST /api/v1/coupons/validate (called by Cart module, public with rate limiting)
  - POST /api/v1/coupons/{code}/apply (called by Orders module post-confirmation)
  - GET /api/v1/promotions/{id}/coupons (admin, list codes for campaign)
  - PUT /api/v1/promotions/{id}/coupons/{couponId} (admin, update limit/status)
  - GET /api/v1/promotions/{id}/analytics (admin, usage metrics + revenue impact)

### Domain Events

- [ ] T148 [P] Create PromotionActivatedEvent.java
- [ ] T149 [P] Create PromotionExpiredEvent.java
- [ ] T150 [P] Create CouponUsedEvent.java
- [ ] T151 Create PromotionEventPublisher.java

### Unit Tests

- [ ] T152 [P] Create PromotionRulesEngineTest.java with tests for:
  - evaluatePromotion with product inclusion rules
  - evaluatePromotion with new customer segment
  - calculateDiscount with max_discount_amount cap
  - Rule combination (AND/OR) logic
- [ ] T153 [P] Create CouponCodeServiceTest.java with tests for:
  - validateCoupon (valid, invalid, expired, usage limit exceeded)

### Contract Tests

- [ ] T154 Create PromotionsApplicationModuleTest.java

### Integration Tests

- [ ] T155 Create PromotionIntegrationTest.java testing:
  - Create promotion → generate coupons → validate coupon → apply to order flow

**Checkpoint**: Promotion engine fully functional; rules engine tested thoroughly

---

## Phase 8: Templates Module

**Goal**: Dynamic page builder for landing pages, category pages, product pages with versioning

**Independent Test**: `./gradlew test --tests "*templates*"` passes all tests

### Data Models & Repositories

- [ ] T156 [P] Create Template.java entity in templates/domain/ with:
  - id, name, type (LANDING_PAGE/CATEGORY_PAGE/PRODUCT_PAGE/CUSTOM)
  - slug (unique, URL-safe), status (DRAFT/PUBLISHED/ARCHIVED)
  - version, published_version, created_by, published_at
- [ ] T157 [P] Create TemplateBlock.java entity with:
  - id, template_id, block_type (HERO/PRODUCT_GRID/CATEGORY_LIST/RICH_TEXT/etc)
  - block_order (position in template), content_json (flexible block content)
  - is_visible (can toggle without deleting)
- [ ] T158 [P] Create TemplateMeta.java entity with:
  - id, template_id, page_title (SEO), page_description, og_title, og_image_url
  - keywords, canonical_url, robots_directive, structured_data_json (JSON-LD)
- [ ] T159 [P] Create TemplateVersion.java entity with:
  - id, template_id, version_number, content_snapshot_json (immutable snapshot)
  - created_at, created_by, change_note
- [ ] T160 [P] Create PageContent.java entity with:
  - id, template_id, entity_id, entity_type (PRODUCT/CATEGORY/LANDING_PAGE)
  - Maps templates to products/categories (1-to-many)
- [ ] T161 [P] Create @ApplicationModule(displayName = "Templates") in templates/package-info.java
- [ ] T162 [P] Create TemplateRepository.java with:
  - findBySlug(String slug)
  - findByType(TemplateType type)
  - findByStatus(TemplateStatus status)
- [ ] T163 [P] Create TemplateBlockRepository.java with:
  - findByTemplateIdOrderByBlockOrder(UUID templateId)
- [ ] T164 [P] Create TemplateVersionRepository.java with:
  - findByTemplateIdAndVersionNumber(UUID templateId, int version)
  - findByTemplateIdOrderByVersionNumberDesc(UUID templateId) (history)

### Services & Business Logic

- [ ] T165 Create TemplateService.java implementing:
  - createTemplate(TemplateRequest): Template (admin)
  - updateTemplate(templateId, UpdateRequest): Template (admin)
  - publishTemplate(templateId, changeNote): version++, snapshot blocks/meta, emits TemplatePublishedEvent
  - unpublishTemplate(templateId): revert to DRAFT
  - archiveTemplate(templateId): ARCHIVED (not deletable)
  - revertToVersion(templateId, versionNumber): create new DRAFT from historical snapshot
- [ ] T166 [P] Create TemplateBlockService.java implementing:
  - addBlock(templateId, blockRequest): TemplateBlock
  - updateBlock(blockId, updateRequest): TemplateBlock
  - toggleVisibility(blockId, isVisible): update without delete (preserve order)
  - deleteBlock(blockId): remove block
  - reorderBlocks(templateId, blockOrders): reorder all blocks
  - Validates block_content_json matches block_type schema
- [ ] T167 [P] Create TemplateMetaService.java implementing:
  - updateMeta(templateId, metaRequest): TemplateMeta
  - Validates SEO title length (50-60 chars), description (120-160 chars)
  - Ensures canonical URL is valid
- [ ] T168 [P] Create PageContentService.java implementing:
  - mapTemplateToEntity(templateId, entityId, entityType): link template to product/category
  - unmapTemplateFromEntity(templateId, entityId, entityType): unlink
  - getTemplateForEntity(entityId, entityType): fetch template for rendering
- [ ] T169 [P] Create TemplateContentProvider.java (support class for retrieving product/category data for template rendering)

### Controllers & API Endpoints

- [ ] T170 Create TemplateAdminController.java with endpoints (14 total, admin only):
  - GET /api/v1/templates (list with pagination + filtering)
  - POST /api/v1/templates (create)
  - GET /api/v1/templates/{id}
  - PUT /api/v1/templates/{id} (update metadata)
  - DELETE /api/v1/templates/{id} (archive)
  - POST /api/v1/templates/{id}/blocks (add block)
  - PUT /api/v1/templates/{id}/blocks/{blockId} (update block)
  - PATCH /api/v1/templates/{id}/blocks/{blockId}/visibility (toggle visibility)
  - DELETE /api/v1/templates/{id}/blocks/{blockId} (delete block)
  - POST /api/v1/templates/{id}/blocks/reorder (reorder)
  - PUT /api/v1/templates/{id}/meta (update SEO)
  - POST /api/v1/templates/{id}/publish (publish version)
  - POST /api/v1/templates/{id}/unpublish (revert to draft)
  - GET /api/v1/templates/{id}/versions (version history)
- [ ] T171 Create TemplateVersionController.java with endpoints (3 total, admin only):
  - GET /api/v1/templates/{id}/versions/{versionNumber}
  - POST /api/v1/templates/{id}/revert/{versionNumber}
  - GET /api/v1/templates/{id}/entities (list templates mapped to entities)
- [ ] T172 Create 템platPublicController.java with endpoint (1 total, public):
  - GET /api/templates/{slug} (fetch published template for frontend rendering, no auth, highly cached)

### Domain Events

- [ ] T173 [P] Create TemplatePublishedEvent.java
- [ ] T174 [P] Create TemplateArchivedEvent.java
- [ ] T175 Create TemplateEventPublisher.java

### Unit Tests

- [ ] T176 [P] Create TemplateBlockServiceTest.java with tests for:
  - Block content validation per block_type
  - reorderBlocks maintains order consistency
- [ ] T177 [P] Create TemplateVersionServiceTest.java with tests for:
  - publishTemplate snapshot creation
  - revertToVersion generates new draft

### Contract Tests

- [ ] T178 Create TemplatesApplicationModuleTest.java

### Integration Tests

- [ ] T179 Create TemplateIntegrationTest.java testing:
  - Create template → add blocks → add meta → publish → map to category flow

**Checkpoint**: Page builder complete; template versioning working

---

## Phase 9: Integration & Polish

**Goal**: End-to-end validation, performance testing, security review, deployment readiness

### End-to-End Integration

- [ ] T180 Create E2ECheckoutFlowTest.java testing complete flow:
  - Register user → Create profile + addresses → Add products to cart → Apply coupon → Checkout → Order confirmation
  - Verify inventory reservations → release on success
  - Verify payment webhook received → order status updated
  - Verify shipment tracking available
- [ ] T181 Create E2EOrderReturnFlowTest.java testing:
  - Order delivered → Initiate return → Approve return → Process refund → Verify refund received
- [ ] T182 Create AdminTemplateAndPromotionFlowTest.java testing:
  - Admin creates landing page template → maps to promotion → promotion goes live
  - Template published → cache invalidated → rendered on frontend

### Performance Testing

- [ ] T183 Create LoadTestCheckout.java (k6 or JMeter script):
  - Simulate 100 concurrent users adding 5 items each + 50% apply coupon + 30% checkout
  - Target: < 200ms p95 for GET endpoints, < 1s for POST /orders
  - Verify no connection pool exhaustion
- [ ] T184 Create LoadTestSearch.java:
  - Simulate 500 concurrent product searches (vary queries, filters)
  - Target: < 500ms p95 for search

### Database Optimization

- [ ] T185 Analyze & optimize indexes:
  - Run EXPLAIN ANALYZE on all critical queries
  - Verify indexes created per data-model.md
  - Measure query times; any >200ms investigated
- [ ] T186 Performance baseline report:
  - Document p50, p95, p99 latencies for all API endpoints
  - Document database query performance

### Security Review

- [ ] T187 Security audit checklist:
  - [ ] SQL injection prevention (parameterized queries verified)
  - [ ] CSRF protection enabled
  - [ ] Rate limiting configured (auth endpoints: 5 reqs/min; public: 100/min)
  - [ ] JWT token expiry validated (15 min access, 7 day refresh)
  - [ ] Password hashing bcrypt cost ≥ 12
  - [ ] No secrets in code/logs
  - [ ] PCI DSS compliance for payment data (no storage, only processor token)
  - [ ] CORS configuration: only allow known frontend origins
- [ ] T188 Security penetration testing:
  - Test input validation (XSS, command injection)
  - Test authorization boundaries (user A cannot access user B's data)

### Code Quality & Coverage

- [ ] T189 Code coverage validation:
  - Run `./gradlew codeCoverageReport`
  - Verify all modules ≥ 80% coverage
  - Flag low-coverage areas for additional test cases
- [ ] T190 Static analysis:
  - Run `./gradlew check` (spotbugs, PMD, checkstyle)
  - Fix all critical issues; document/approve warnings
- [ ] T191 Code review checklist:
  - All merge requests reviewed by ≥ 1 peer
  - Modulith module boundaries verified in review
  - API contract adherence checked

### Documentation & Deployment

- [ ] T192 Update API documentation:
  - Generate OpenAPI/Swagger docs: `./gradlew generateOpenApiDocs`
  - Export Postman collection with all 95+ endpoints
  - Document authentication flow with token examples
- [ ] T193 DevOps preparation:
  - Create Dockerfile for Spring Boot application
  - Create Kubernetes deployment manifest (if applicable)
  - Setup CI/CD pipeline: GitHub Actions → lint → test → build → deploy-staging
- [ ] T194 Production readiness checklist:
  - [ ] Database backup strategy (daily, 30-day retention)
  - [ ] Logging aggregation setup (centralized logs)
  - [ ] Monitoring alerts (p95 latency, error rate, DB connection pool)
  - [ ] Graceful shutdown handling (drain in-flight requests)
  - [ ] Health check endpoints (/actuator/health)
- [ ] T195 Deployment dry-run:
  - Deploy to staging environment
  - Run smoke tests against staging
  - Verify all integrations (email, payment processor, carriers)

### Stakeholder Sign-off

- [ ] T196 Product Owner sign-off:
  - Review feature completeness vs. specification
  - Validate all user journeys working end-to-end
  - Approve for production release
- [ ] T197 QA lead sign-off:
  - Verify 80%+ test coverage
  - Confirm all blocking bugs resolved
  - Approve test results report
- [ ] T198 Technical lead sign-off:
  - Verify Modulith boundaries respected
  - Confirm performance SLAs met
  - Approve architectural integrity

**Checkpoint**: All systems tested, documented, approved for production

---

## Task Dependency Graph

```
Phase 1 Setup (T001-T007)
    ↓
Phase 2 Foundational (T008-T032)
    ├→ Phase 3 Catalog (T033-T060)
    ├→ Phase 4 Users (T061-T086)
    ├→ Phase 5 Cart (T087-T109) [depends on Catalog + Users]
    ├→ Phase 6 Orders (T110-T133) [depends on Cart]
    ├→ Phase 7 Promotions (T134-T155) [depends on Cart + Orders]
    └→ Phase 8 Templates (T156-T179)
        ↓
Phase 9 Integration (T180-T198)
```

---

## Parallel Execution Strategy

**Week 1**: Phase 1 + 2 (sequential)
**Weeks 2-3**: Phase 3 (Catalog) + Phase 4 (Users) in parallel, Phase 5 (Cart) begins end of week 3
**Weeks 4-5**: Phase 5 (Cart) + Phase 6 (Orders) in parallel
**Weeks 6-7**: Phase 7 (Promotions) + Phase 8 (Templates) in parallel
**Week 8**: Phase 9 (Integration & Polish)

---

## Success Criteria

✅ **Coverage**: All modules ≥ 80% test coverage  
✅ **Performance**: All API endpoints meet SLA targets (< 200ms p95)  
✅ **Integration**: E2E checkout flow passes end-to-end test  
✅ **Security**: Security audit clean, no critical vulnerabilities  
✅ **Documentation**: OpenAPI docs generated, Postman collection exported  
✅ **Modulith**: Zero module boundary violations  
✅ **Sign-offs**: Product Owner, QA Lead, Technical Lead all approved

---

**Ready for Implementation**: All tasks sequenced, dependencies mapped, success criteria defined.

Execute via: `./gradlew bootRun` (local testing) → staging deployment → production release

