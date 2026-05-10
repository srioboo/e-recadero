# Data Model: Backend E-Commerce Spring Boot Application

**Date**: 2026-05-09 | **Feature**: 002-backend-ecommerce | **Status**: Complete

## Overview

This document defines the entity relationships, database schemas, validation rules, and state machines for all six modules. Each module owns its data exclusively; cross-module queries are prohibited. All relationships enforce consistency via transactions (pessimistic for critical flows) and events (optimistic for notifications).

---

## Module 1: Catalog (Products & Categories)

### Entities

#### Category
```
Table: catalog.category
- id (UUID, PK)
- name (VARCHAR 255, NOT NULL, UNIQUE)
- slug (VARCHAR 255, NOT NULL, UNIQUE, indexed)
- description (TEXT)
- parent_category_id (UUID, FK)
- image_url (VARCHAR 512)
- sort_order (INT, default 0)
- status (ENUM: ACTIVE, INACTIVE, ARCHIVED; default ACTIVE)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- created_by (UUID, FK → users.user)
- updated_by (UUID, FK → users.user)

Indexes:
- parent_category_id (enable hierarchy traversal)
- slug (URL routing)
- status (filtering by active categories)

Validation:
- name: 1-255 chars, non-empty
- slug: must match regex [a-z0-9-]+$
- parent_category_id: if set, must reference existing category (no cycles allowed)
```

#### Product
```
Table: catalog.product
- id (UUID, PK)
- sku (VARCHAR 50, NOT NULL, UNIQUE, indexed)
- name (VARCHAR 255, NOT NULL)
- description (TEXT)
- long_description (TEXT)
- category_id (UUID, FK → catalog.category, NOT NULL, indexed)
- base_price (DECIMAL 10,2, NOT NULL)
- cost_price (DECIMAL 10,2, NOT NULL, for margin calculation)
- status (ENUM: DRAFT, PUBLISHED, ARCHIVED; default DRAFT)
- featured (BOOLEAN, default false)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- created_by (UUID, FK → users.user)
- updated_by (UUID, FK → users.user)
- version (BIGINT, for optimistic locking, default 0)

Indexes:
- category_id (filtering products by category)
- sku (SKU lookup)
- status (published products only)
- (category_id, status) (common combined filter)

Validation:
- sku: must be unique; format [A-Z0-9]+ (alphanumeric only)
- base_price: must be ≥ cost_price
- name: 1-255 chars, non-empty
- category_id: must reference existing category
```

#### ProductAttribute
```
Table: catalog.product_attribute
- id (UUID, PK)
- product_id (UUID, FK → catalog.product, NOT NULL, indexed)
- name (VARCHAR 100, NOT NULL)
- type (ENUM: TEXT, SELECT, NUMBER, DATE_RANGE; default TEXT)
- values (JSON, nullable)
  Example: {"colors": ["Red", "Blue", "Green"], "sizes": ["S", "M", "L", "XL"]}
- display_order (INT, default 0)

Unique constraint: (product_id, name)

Validation:
- name: 1-100 chars
- values: valid JSON; max 10 values per attribute
```

#### ProductVariant
```
Table: catalog.product_variant
- id (UUID, PK)
- product_id (UUID, FK → catalog.product, NOT NULL, indexed)
- sku (VARCHAR 50, NOT NULL, UNIQUE, indexed)
  Example: "SHIRT-RED-M" (combination of product SKU + variant attributes)
- variant_attributes (JSON)
  Example: {"color": "Red", "size": "M"}
- price (DECIMAL 10,2, NOT NULL, variant-specific price override)
- weight (DECIMAL 8,3, grams, nullable)
- dimensions_json (JSON, nullable)
  Example: {"length_cm": 30, "width_cm": 20, "height_cm": 15}
- status (ENUM: ACTIVE, INACTIVE; default ACTIVE)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- version (BIGINT, for optimistic locking)

Indexes:
- product_id
- sku (variant lookup)
- (product_id, status)

Validation:
- sku: must be unique; inherited from parent product sku
- price: must be positive
- variant_attributes: must match product's attributes schema
```

#### Inventory
```
Table: catalog.inventory
- id (UUID, PK)
- product_variant_id (UUID, FK → catalog.product_variant, NOT NULL, indexed)
- warehouse_id (UUID, NOT NULL, indexed)
  Note: warehouse_id managed by Inventory service (could be hardcoded for MVP: "DEFAULT_WAREHOUSE")
- quantity_on_hand (INT, NOT NULL, default 0)
- reserved_quantity (INT, NOT NULL, default 0)
  reserved_quantity = items in shopping carts (not yet confirmed orders)
- reorder_level (INT, default 10)
- reorder_quantity (INT, default 50)
- last_counted_at (TIMESTAMP, nullable)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- version (BIGINT, for optimistic locking)

Unique constraint: (product_variant_id, warehouse_id)

Indexes:
- (product_variant_id, warehouse_id) (lookup)
- warehouse_id (inventory by warehouse)

Calculated Fields:
- available_quantity = quantity_on_hand - reserved_quantity

Validation:
- quantity_on_hand: int ≥ 0
- reserved_quantity: int ≥ 0, must be ≤ quantity_on_hand
```

### State Machine: Product Lifecycle
```
DRAFT → PUBLISHED → ARCHIVED (one-way to ARCHIVED)
           ↓
        INACTIVE (temporary suspension)

Transitions:
- DRAFT → PUBLISHED: Product has price, category, main image, description
- PUBLISHED → ARCHIVED: Admin action; cannot revert
- PUBLISHED ↔ INACTIVE: Toggle availability (temporary)

Event Triggers:
- ProductPublished → Catalog indexing, cache update
- ProductArchived → Remove from promotions, carts, templates
```

### Key Business Rules
- A product must belong to exactly one category at any time
- SKU cannot be changed after product is published
- Category hierarchy supports max 5 levels deep (prevent circular references)
- Inventory available = on_hand - reserved; cannot go negative
- Prices must be ≥ cost_price (margin validation in service layer)

---

## Module 2: Users (Accounts & Profiles)

### Entities

#### User
```
Table: users.user
- id (UUID, PK)
- email (VARCHAR 255, NOT NULL, UNIQUE, indexed)
- password_hash (VARCHAR 500, NOT NULL)
- username (VARCHAR 100, NOT NULL, UNIQUE, indexed)
- status (ENUM: ACTIVE, INACTIVE, SUSPENDED, DELETED; default ACTIVE)
- email_verified (BOOLEAN, default false)
- email_verified_at (TIMESTAMP, nullable)
- phone_verified (BOOLEAN, default false)
- phone_verified_at (TIMESTAMP, nullable)
- last_login_at (TIMESTAMP, nullable)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- deleted_at (TIMESTAMP, nullable, for soft deletes)

Indexes:
- email
- username
- status
- (status, created_at DESC) (active users joined recently)

Validation:
- email: valid RFC 5322 format, lowercase
- password_hash: bcrypt (min cost 12)
- username: 3-50 chars, alphanumeric + underscore only
- status: enforced by application (no DELETED state in queries without special flag)
```

#### UserProfile
```
Table: users.user_profile
- id (UUID, PK)
- user_id (UUID, FK → users.user, NOT NULL, UNIQUE, indexed)
- first_name (VARCHAR 100, nullable)
- last_name (VARCHAR 100, nullable)
- phone (VARCHAR 20, nullable)
- avatar_url (VARCHAR 512, nullable)
- bio (TEXT, nullable, max 500 chars)
- preferred_locale (VARCHAR 5, default 'en', indexed)
  Valid: en, es, fr, de, etc.
- preferred_currency (VARCHAR 3, default 'USD')
- newsletter_subscribed (BOOLEAN, default false)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- user_id (fast lookup)
- preferred_locale (localization queries)

Validation:
- first_name, last_name: 1-100 chars each
- phone: valid format (E.164) or nullable
- avatar_url: valid image URL
```

#### Address
```
Table: users.address
- id (UUID, PK)
- user_id (UUID, FK → users.user, NOT NULL, indexed)
- type (ENUM: BILLING, SHIPPING, OTHER; NOT NULL)
- street_address (VARCHAR 255, NOT NULL)
- street_address_2 (VARCHAR 255, nullable)
- city (VARCHAR 100, NOT NULL)
- state_province (VARCHAR 100, NOT NULL)
- postal_code (VARCHAR 20, NOT NULL)
- country_code (CHAR 2, NOT NULL, ISO 3166-1 alpha-2)
- is_primary (BOOLEAN, default false)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- (user_id, type) (fetch billing/shipping addresses)
- (user_id, is_primary) (default address lookup)

Constraints:
- At most one primary SHIPPING address per user
- At most one primary BILLING address per user

Validation:
- street_address: 1-255 chars, non-empty
- city, state_province: non-empty
- postal_code: format depends on country_code
- country_code: must be valid ISO code
```

#### UserRole
```
Table: users.user_role
- id (UUID, PK)
- user_id (UUID, FK → users.user, NOT NULL, indexed)
- role_name (VARCHAR 50, NOT NULL)
  Valid: CUSTOMER, ADMIN, VENDOR, SUPPORT, ANALYST
- granted_at (TIMESTAMP, NOT NULL)
- granted_by (UUID, FK → users.user, nullable)

Unique constraint: (user_id, role_name)

Indexes:
- (user_id, role_name)
- role_name (admin queries)

Validation:
- role_name: must be from enum list
```

### State Machine: User Lifecycle
```
[Register] → ACTIVE (after email verification)
   ↓
INACTIVE (user deactivated account)  ← → ACTIVE (reactivate)
   ↓
SUSPENDED (admin action: policy violation)
   ↓
DELETED (after 90-day retention period, soft delete)

Event Triggers:
- UserRegistered → Send verification email
- UserEmailVerified → Activate account, Welcome email
- UserDeactivated → Preserve data for 90 days before purge
```

### Key Business Rules
- Email must be verified before checkout allowed
- Username and email are globally unique
- User cannot have conflicting addresses (e.g., two primary SHIPPING)
- Soft deletes: DELETED users excluded from queries except admin purge jobs
- Roles determine API access (ADMIN: full access; CUSTOMER: own data only)

---

## Module 3: Shopping Cart

### Entities

#### Cart
```
Table: cart.cart
- id (UUID, PK)
- user_id (UUID, FK → users.user, indexed, nullable)
  Note: Nullable for anonymous carts; identified via session_id
- session_id (VARCHAR 100, nullable, indexed, unique with user_id)
- status (ENUM: ACTIVE, ABANDONED, CHECKED_OUT; default ACTIVE)
- subtotal (DECIMAL 10,2, NOT NULL, default 0.00) [calculated field]
- tax_total (DECIMAL 10,2, NOT NULL, default 0.00) [calculated field]
- shipping_total (DECIMAL 10,2, NOT NULL, default 0.00) [calculated field, nullable - estimated]
- discount_total (DECIMAL 10,2, NOT NULL, default 0.00) [calculated field]
- grand_total (DECIMAL 10,2, NOT NULL, default 0.00) [calculated field]
- coupon_code (VARCHAR 50, nullable, indexed)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- expires_at (TIMESTAMP, NOT NULL)
  Default: created_at + 24 hours

Indexes:
- user_id (user's active cart)
- (user_id, status) (fetch user's carts)
- expires_at (cleanup abandoned carts)
- session_id (anonymous cart lookup)

Unique constraint:
- (user_id, status) where status = 'ACTIVE' (only one active cart per user)
- (session_id, status) where status = 'ACTIVE' and user_id IS NULL (one anonymous active cart)

Calculated Fields (transient, not stored):
- total_items = SUM(CartItem.quantity)
- subtotal = SUM(CartItem.line_total) [without discounts]
- grand_total = subtotal + tax_total + shipping_total - discount_total

Validation:
- coupon_code: 1-50 chars, alphanumeric + dash
- expires_at: must be > created_at
```

#### CartItem
```
Table: cart.cart_item
- id (UUID, PK)
- cart_id (UUID, FK → cart.cart, NOT NULL, indexed)
- product_variant_id (UUID, NOT NULL, indexed)
  Note: Store variant ID, not product ID (supports variants in cart)
- quantity (INT, NOT NULL, default 1)
- price_at_time (DECIMAL 10,2, NOT NULL)
  Snapshot of product_variant.price when added to cart (handles price changes after add)
- discount_applied (DECIMAL 10,2, NOT NULL, default 0.00) [line-level discount]
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- cart_id (fetch cart items)
- (cart_id, product_variant_id) UNIQUE (prevent duplicate items in cart)

Calculated Fields:
- line_total = (price_at_time * quantity) - discount_applied
- is_in_stock = (Inventory.available_quantity > 0)

Validation:
- quantity: int > 0, < 1000 (max qty per item)
- price_at_time: decimal > 0
- product_variant_id: must exist in catalog.product_variant
```

#### Reservation (Inventory Hold)
```
Table: cart.reservation
- id (UUID, PK)
- cart_item_id (UUID, FK → cart.cart_item, NOT NULL, indexed)
- product_variant_id (UUID, FK → catalog.product_variant, NOT NULL, indexed)
- quantity (INT, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)
- expires_at (TIMESTAMP, NOT NULL)
  Default: created_at + 24 hours (matches cart expiry)

Indexes:
- (cart_item_id, product_variant_id)
- expires_at (cleanup expired reservations)
- product_variant_id (inventory availability calculation)

Validation:
- quantity: int > 0
- product_variant_id must match cart_item.product_variant_id (integrity check)
```

#### CartPromotion
```
Table: cart.cart_promotion
- id (UUID, PK)
- cart_id (UUID, FK → cart.cart, NOT NULL, indexed)
- promotion_id (UUID, NOT NULL, indexed)
  Note: Foreign key to promotions.promotion (cross-module reference allowed for lookups)
- coupon_code (VARCHAR 50, nullable, indexed)
- discount_amount (DECIMAL 10,2, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)

Indexes:
- (cart_id, promotion_id) UNIQUE (one promotion per cart)

Validation:
- discount_amount: decimal > 0
- promotion_id: must be valid (checked via REST call to Promotions module)
```

### State Machine: Cart Lifecycle
```
ACTIVE (items being added/removed)
   ↓ [checkout]
CHECKED_OUT (converted to order)
   ↓
[24h timeout without activity]
ABANDONED (cleanup job reclaims inventory)

Event Triggers:
- CartItemAdded → Reserve inventory
- CartItemRemoved → Release reservation
- CartAbandoned (24h timeout) → Release reservations, email reminder
- CheckoutStarted → Lock cart from modifications
```

### Key Business Rules
- Each user has at most ONE active cart at a time
- Anonymous carts identified via session_id; stored for 24 hours
- Inventory reserved when item added to cart; released if removed or cart expires
- Prices snapshot when item added; updates when checkout begins (flash sales trigger price recalc)
- Promotions applied at checkout time (not stored in cart until confirmed)

---

## Module 4: Orders

### Entities

#### Order
```
Table: orders.order
- id (UUID, PK)
- order_number (VARCHAR 20, NOT NULL, UNIQUE, indexed)
  Format: ORD-{timestamp}-{randomId} (e.g., ORD-20260509-ABC123)
- user_id (UUID, NOT NULL, FK → users.user, indexed)
- status (ENUM: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED; default PENDING)
- order_date (TIMESTAMP, NOT NULL)
- confirmed_date (TIMESTAMP, nullable)
- shipped_date (TIMESTAMP, nullable)
- delivered_date (TIMESTAMP, nullable)
- subtotal (DECIMAL 10,2, NOT NULL)
- tax_total (DECIMAL 10,2, NOT NULL)
- shipping_total (DECIMAL 10,2, NOT NULL)
- discount_total (DECIMAL 10,2, NOT NULL)
- grand_total (DECIMAL 10,2, NOT NULL)
- currency (VARCHAR 3, NOT NULL, default 'USD')
- billing_address_id (UUID, FK → users.address, NOT NULL)
- shipping_address_id (UUID, FK → users.address, NOT NULL)
- shipping_method_id (UUID, nullable)
  Note: Warehouse/fulfillment system tracks shipping methods
- notes (TEXT, nullable)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- version (BIGINT, for optimistic locking)

Indexes:
- order_number (order lookup)
- (user_id, status) (user's orders by status)
- (status, created_at DESC) (admin queries: recent orders)
- confirmed_date (shipped status reporting)

Validation:
- order_number: auto-generated, immutable
- status: valid enum transition only (see state machine)
- total fields: decimal, non-negative; grand_total = subtotal + tax_total + shipping_total - discount_total
- user_id, billing_address_id, shipping_address_id: must exist (FK constraints)
```

#### OrderItem
```
Table: orders.order_item
- id (UUID, PK)
- order_id (UUID, FK → orders.order, NOT NULL, indexed)
- product_variant_id (UUID, NOT NULL)
  Note: Store variant ID snapshot (immutable after order)
- product_sku (VARCHAR 50, NOT NULL)
  Snapshot of product_variant.sku for historical tracking
- product_name (VARCHAR 255, NOT NULL)
  Snapshot for historical record (avoids cascading product name changes)
- quantity (INT, NOT NULL)
- unit_price (DECIMAL 10,2, NOT NULL)
  Price at time of order confirmation (not current product price)
- line_discount (DECIMAL 10,2, NOT NULL, default 0.00)
- line_total (DECIMAL 10,2, NOT NULL)
  = (unit_price * quantity) - line_discount
- created_at (TIMESTAMP, NOT NULL)

Indexes:
- order_id (fetch order details)
- (order_id, product_variant_id)

Validation:
- quantity: int > 0
- unit_price: decimal > 0
- line_total: decimal ≥ 0
```

#### OrderShipment
```
Table: orders.order_shipment
- id (UUID, PK)
- order_id (UUID, FK → orders.order, NOT NULL, indexed)
- carrier (VARCHAR 50, NOT NULL)
  Examples: FEDEX, UPS, DHL, USPS, COURIER_LOCAL
- tracking_number (VARCHAR 100, NOT NULL)
- status (ENUM: PENDING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, LOST; default PENDING)
- shipped_at (TIMESTAMP, nullable)
- estimated_delivery_at (TIMESTAMP, nullable)
- delivered_at (TIMESTAMP, nullable)
- delivery_signature_required (BOOLEAN, default false)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- order_id
- tracking_number (shipment tracking lookup)
- (status, created_at DESC)

Validation:
- carrier: valid enum value
- tracking_number: non-empty, unique within carrier context
- status transitions: PENDING → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED (see state machine)
```

#### OrderPayment
```
Table: orders.order_payment
- id (UUID, PK)
- order_id (UUID, FK → orders.order, NOT NULL, indexed)
- payment_method (ENUM: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL, APPLE_PAY, GOOGLE_PAY)
- amount (DECIMAL 10,2, NOT NULL)
- currency (VARCHAR 3, NOT NULL)
- status (ENUM: PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED, CANCELLED; default PENDING)
- transaction_id (VARCHAR 100, NOT NULL, indexed)
  External payment processor ID (Stripe charge ID, PayPal transaction ID, etc.)
- processor_response (TEXT, nullable)
  Raw JSON response from payment processor (for debugging)
- processed_at (TIMESTAMP, nullable)
- refund_amount (DECIMAL 10,2, NOT NULL, default 0.00)
- refund_reason (VARCHAR 255, nullable)
- refunded_at (TIMESTAMP, nullable)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- order_id (order's payment)
- transaction_id (idempotency; prevent duplicate charges)
- status

Validation:
- amount: decimal > 0
- payment_method: valid enum
- transaction_id: unique globally (prevent duplicate payments)
- refund_amount: <= amount; only if status = REFUNDED
```

### State Machine: Order Lifecycle
```
PENDING (awaiting payment)
   ↓ [payment authorized]
CONFIRMED (payment confirmed; ready to ship)
   ↓ [fulfillment picks/packs]
SHIPPED (in transit)
   ↓ [delivery confirmed]
DELIVERED (completed successfully)

Side flows:
PENDING → CANCELLED (user cancels before payment or payment fails after timeout)
CONFIRMED → CANCELLED (user cancels before shipment)
CANCELLED → REFUNDED (auto-trigger if payment was captured)

Event Triggers:
- OrderPending → Inventory reservation confirmed; awaiting payment
- OrderConfirmed → Payment successful; fulfillment notified
- OrderShipped → Tracking info sent to customer email
- OrderDelivered → Completion; enable review/return window
- OrderCancelled → Release inventory; initiate refund
```

### Key Business Rules
- Order immutable after creation (no quantity changes; must cancel + reorder)
- Order totals must match cart totals (price changes between cart → checkout recalc only once)
- Payment must be authorized before order transitions to CONFIRMED
- Shipment tracking auto-notifies customer on status changes
- Refunds only allowed within 30 days of delivery
- Order number must be globally unique and human-readable for customer support

---

## Module 5: Promotions

### Entities

#### Promotion
```
Table: promotions.promotion
- id (UUID, PK)
- name (VARCHAR 255, NOT NULL)
- description (TEXT, nullable)
- promotion_type (ENUM: PERCENTAGE_DISCOUNT, FIXED_DISCOUNT, FREE_SHIPPING, BOGO; default PERCENTAGE_DISCOUNT)
- discount_type (ENUM: PERCENTAGE, FIXED_AMOUNT; default PERCENTAGE)
- discount_value (DECIMAL 8,2, NOT NULL)
  If PERCENTAGE: 0-100; If FIXED_AMOUNT: absolute value in currency
- max_discount_amount (DECIMAL 10,2, nullable)
  Cap on total discount per order (e.g., max $50 off)
- minimum_order_amount (DECIMAL 10,2, nullable)
  Promotion applies only if order > amount
- applicable_to_sale_items (BOOLEAN, default true)
- status (ENUM: DRAFT, ACTIVE, PAUSED, EXPIRED, ARCHIVED; default DRAFT)
- start_date (TIMESTAMP, NOT NULL)
- end_date (TIMESTAMP, NOT NULL)
- usage_limit (INT, nullable)
  null = unlimited uses
- current_usage_count (INT, default 0)
- priority (INT, default 0)
  Higher priority = applied first (order matters for BOGO, stacking rules)
- created_by (UUID, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- version (BIGINT, for optimistic locking)

Indexes:
- status (active promotions only)
- (start_date, end_date) (temporal queries)
- priority DESC (rule ordering)

Validation:
- name: 1-255 chars, non-empty
- discount_value: must be positive; if % must be 0-100
- start_date < end_date (promotion duration validity)
- usage_limit: null or int > 0
```

#### PromotionRule
```
Table: promotions.promotion_rule
- id (UUID, PK)
- promotion_id (UUID, FK → promotions.promotion, NOT NULL, indexed)
- rule_type (ENUM: PRODUCT_INCLUDE, PRODUCT_EXCLUDE, CATEGORY_INCLUDE, CATEGORY_EXCLUDE, USER_SEGMENT, CUSTOMER_GROUP)
- condition_operator (ENUM: AND, OR; default AND)
  Determines how multiple rules combine (AND = all must match; OR = any can match)
- condition_json (JSON)
  Flexible structure to store complex conditions:
  Example for PRODUCT_INCLUDE: {"product_ids": ["uuid1", "uuid2"]}
  Example for CATEGORY_INCLUDE: {"category_ids": ["uuid1"], "min_purchase": 50}
  Example for USER_SEGMENT: {"new_customers_only": true, "max_orders": 5}
- applies_on_sale_items (BOOLEAN, default false)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- (promotion_id, rule_type)

Validation:
- condition_json: valid JSON; required fields depend on rule_type
```

#### CouponCode
```
Table: promotions.coupon_code
- id (UUID, PK)
- promotion_id (UUID, FK → promotions.promotion, NOT NULL, indexed)
- code (VARCHAR 50, NOT NULL, UNIQUE, indexed)
  Example: NEWYEAR2026, SUMMER50, VIP-EXCLUSIVE-2026
- discount_value_override (DECIMAL 8,2, nullable)
  If set, overrides promotion.discount_value for this coupon
- usage_limit (INT, nullable)
  Null = use promotion's limit; set here for coupon-specific limit
- current_usage_count (INT, default 0)
- expiry_date (TIMESTAMP, nullable)
  Null = use promotion.end_date; set here for coupon-specific expiry
- is_active (BOOLEAN, default true)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- code (coupon validation at checkout)
- is_active (filter active coupons only)

Validation:
- code: 4-50 chars, alphanumeric + dash/underscore; uppercase
- discount_value_override: if set, must be positive
- expiry_date: if set, must be > created_at
```

#### PromotionUsage
```
Table: promotions.promotion_usage
- id (UUID, PK)
- promotion_id (UUID, FK → promotions.promotion, NOT NULL, indexed)
- coupon_code_id (UUID, nullable, FK → promotions.coupon_code, indexed)
- order_id (UUID, FK → orders.order, NOT NULL, indexed, unique)
  One promotion per order (to prevent stacking)
- user_id (UUID, NOT NULL, indexed)
- discount_amount (DECIMAL 10,2, NOT NULL)
- used_at (TIMESTAMP, NOT NULL)

Indexes:
- (promotion_id, used_at DESC) (usage trends)
- (user_id, promotion_id) (user's promotion history)

Validation:
- discount_amount: decimal > 0
- used_at: must be between promotion.start_date and end_date
```

### State Machine: Promotion Lifecycle
```
DRAFT (created but not active)
   ↓ [admin enables]
ACTIVE (currently running)
   ↓ [manual pause or automatic on end_date]
PAUSED or EXPIRED
   ↓ [archive]
ARCHIVED (historical record)

Event Triggers:
- PromotionActivated → Make eligible for checkout
- PromotionExpired → Auto-deactivate at end_date (scheduled job)
- PromotionUsed → Increment usage counter; check limits
```

### Key Business Rules
- Promotion cannot be applied after end_date expires
- Usage limit cannot be exceeded (enforce in service layer)
- Coupon code unique globally; case-insensitive lookup
- Cannot stack multiple promotions on one order (one promotion_id per PromotionUsage)
- Discount value cannot exceed order total
- New customer promotions identified via User.created_at + order count rules
- Promotions applied at checkout (cart shows estimated discount; final calculated on order confirmation)

---

## Module 6: Templates

### Entities

#### Template
```
Table: templates.template
- id (UUID, PK)
- name (VARCHAR 255, NOT NULL)
- type (ENUM: LANDING_PAGE, CATEGORY_PAGE, PRODUCT_PAGE, CUSTOM; NOT NULL)
- slug (VARCHAR 255, NOT NULL, UNIQUE, indexed)
  URL-friendly identifier (e.g., "summer-sale-2026", "electronics-category")
- status (ENUM: DRAFT, PUBLISHED, ARCHIVED; default DRAFT)
- version (INT, default 1)
  Auto-incremented on each publication
- published_version (INT, nullable)
  Reference to the currently live version
- created_by (UUID, FK → users.user, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)
- published_at (TIMESTAMP, nullable)

Indexes:
- slug (URL routing)
- (status, type) (fetch templates by type and status)
- created_by (user's templates)

Validation:
- name: 1-255 chars, non-empty
- slug: valid URL format [a-z0-9-]+$, unique
- type: valid enum
```

#### TemplateBlock
```
Table: templates.template_block
- id (UUID, PK)
- template_id (UUID, FK → templates.template, NOT NULL, indexed)
- block_type (ENUM: HERO, HEADER, FOOTER, PRODUCT_GRID, CATEGORY_LIST, TESTIMONIALS, CTA, RICH_TEXT, IMAGE_BANNER, FEATURED_PRODUCTS)
- block_name (VARCHAR 100, nullable)
  Custom label for CMS (e.g., "Hero Banner", "Product Grid Section 1")
- block_order (INT, NOT NULL, indexed)
  Position in template; ordered by this field
- content_json (JSON, NOT NULL)
  Flexible structure depending on block_type:
  
  HERO: {
    "title": "Summer Sale 2026",
    "subtitle": "Up to 50% off",
    "background_image_url": "...",
    "cta_text": "Shop Now",
    "cta_link": "/category/summer"
  }
  
  PRODUCT_GRID: {
    "products": ["uuid1", "uuid2", "uuid3"],
    "layout": "3-columns",
    "show_prices": true,
    "show_ratings": true
  }
  
  CATEGORY_LIST: {
    "category_ids": ["uuid1", "uuid2"],
    "items_per_row": 4,
    "show_subcategories": true
  }
  
  RICH_TEXT: {
    "html_content": "<p>Custom content...</p>",
    "text_alignment": "center",
    "background_color": "#ffffff"
  }

- is_visible (BOOLEAN, default true)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Indexes:
- (template_id, block_order) (template rendering order)

Validation:
- block_type: valid enum
- block_order: int >= 0
- content_json: valid JSON; structure validated against block_type schema
```

#### TemplateMeta
```
Table: templates.template_meta
- id (UUID, PK)
- template_id (UUID, FK → templates.template, NOT NULL, UNIQUE, indexed)
- page_title (VARCHAR 255, NOT NULL)
  HTML <title> tag
- page_description (VARCHAR 500, nullable)
  HTML <meta name="description">
- og_title (VARCHAR 255, nullable)
  Open Graph title (social media preview)
- og_description (VARCHAR 500, nullable)
- og_image_url (VARCHAR 512, nullable)
- og_type (VARCHAR 50, default 'website')
- keywords (VARCHAR 500, nullable)
  Comma-separated keywords for SEO
- canonical_url (VARCHAR 512, nullable)
- robots_directive (VARCHAR 50, default 'index,follow')
  e.g., 'noindex,nofollow' for unpublished
- structured_data_json (JSON, nullable)
  Schema.org structured data (JSON-LD)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Validation:
- page_title: 1-60 chars (SEO best practice)
- page_description: 120-160 chars (SEO best practice)
- og_image_url: valid image URL format
- keywords: comma-separated, 5-10 keywords recommended
```

#### TemplateVersion
```
Table: templates.template_version
- id (UUID, PK)
- template_id (UUID, FK → templates.template, NOT NULL, indexed)
- version_number (INT, NOT NULL)
  Matches template.version at publication time
- content_snapshot_json (JSON, NOT NULL)
  Snapshot of all blocks + meta for this version (frozen historical record)
- created_at (TIMESTAMP, NOT NULL)
- created_by (UUID, FK → users.user, NOT NULL)
- published_at (TIMESTAMP, nullable)
- change_note (TEXT, nullable)
  Admin notes on what changed (e.g., "Updated hero image for Q2 campaign")

Unique constraint: (template_id, version_number)

Indexes:
- (template_id, version_number DESC) (fetch specific version)

Validation:
- version_number: int > 0
- content_snapshot_json: valid JSON snapshot of template state
```

#### PageContent
```
Table: templates.page_content
- id (UUID, PK)
- template_id (UUID, FK → templates.template, NOT NULL, indexed)
- entity_id (UUID, NOT NULL, indexed)
  Can be product_id, category_id, or user_id depending on entity_type
- entity_type (ENUM: PRODUCT, CATEGORY, USER, LANDING_PAGE; NOT NULL)
- status (ENUM: DRAFT, PUBLISHED, ARCHIVED; default DRAFT)
- published_at (TIMESTAMP, nullable)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP, NOT NULL)

Unique constraint: (template_id, entity_id, entity_type)

Indexes:
- (entity_id, entity_type) (find which templates use this entity)

Validation:
- entity_type: valid enum
- entity_id: must exist (integrity checked at service layer)
```

### State Machine: Template Lifecycle
```
DRAFT (editing in progress)
   ↓ [admin reviews and publishes]
PUBLISHED (live on website)
   ↓ [version bump; new draft created]
DRAFT ← (create new version from published)
   ↓ [archive]
ARCHIVED (historical record; not used)

Event Triggers:
- TemplatePublished → Update website cache; invalidate CDN
- TemplateArchived → Remove from active pages
```

### Key Business Rules
- Template slug must be unique; used for URL routing
- Blocks ordered by block_order (auto-sorted on fetch)
- Published version immutable; changes create new draft version
- Can revert to previous version (copy TemplateVersion content → new draft)
- SEO metadata required for all published templates
- Blocks reference external entities (products, categories) by ID; entities must exist
- One template per page/entity combination (no conflicting templates for same landing page)

---

## Cross-Module Reference Rules

### Allowed Cross-Module References (via IDs only, not direct queries)
```
Cart → Catalog: references product_variant_id (pricing, availability checks via REST API)
Cart → Users: references user_id (cart ownership)
Orders → Users: references user_id (order ownership), address_id (billing/shipping)
Orders → Catalog: historical references to product_variant_id, SKU (immutable snapshots)
Orders → Cart: implicit link (cart_id → order conversion; kept for audit trail)
Promotions → Orders: references order_id (usage tracking)
Promotions → Catalog: references product_id, category_id (rule conditions)
Templates → Catalog: references product_id, category_id (block content)
Templates → Users: references user_id (created_by, updated_by)
```

### PROHIBITED Cross-Module References
- Cart directly querying Catalog tables (use REST API)
- Catalog directly querying Orders (one-way reference only)
- Users module accessing Cart or Order data (queries go through Cart/Orders modules)
- Any module directly calling another module's private repositories (use application events)

---

## Index Summary (Performance Checklist)

```sql
-- Catalog Module
CREATE INDEX idx_category_status ON catalog.category(status);
CREATE INDEX idx_category_parent ON catalog.category(parent_category_id);
CREATE INDEX idx_product_category ON catalog.product(category_id);
CREATE INDEX idx_product_status ON catalog.product(status);
CREATE INDEX idx_product_category_status ON catalog.product(category_id, status);
CREATE INDEX idx_variant_product ON catalog.product_variant(product_id);
CREATE INDEX idx_inventory_variant ON catalog.inventory(product_variant_id, warehouse_id);

-- Users Module
CREATE INDEX idx_user_email ON users.user(email);
CREATE INDEX idx_user_status ON users.user(status);
CREATE INDEX idx_address_user_type ON users.address(user_id, type);
CREATE INDEX idx_role_user ON users.user_role(user_id);

-- Cart Module
CREATE INDEX idx_cart_user_status ON cart.cart(user_id, status);
CREATE INDEX idx_cart_expires ON cart.cart(expires_at);
CREATE INDEX idx_cartitem_cart ON cart.cart_item(cart_id);
CREATE INDEX idx_reservation_expires ON cart.reservation(expires_at);

-- Orders Module
CREATE INDEX idx_order_user ON orders.order(user_id);
CREATE INDEX idx_order_status ON orders.order(status);
CREATE INDEX idx_order_created ON orders.order(created_at DESC);
CREATE INDEX idx_shipment_tracking ON orders.order_shipment(tracking_number);
CREATE INDEX idx_payment_transaction ON orders.order_payment(transaction_id);

-- Promotions Module
CREATE INDEX idx_promotion_status ON promotions.promotion(status);
CREATE INDEX idx_promotion_active ON promotions.promotion(start_date, end_date);
CREATE INDEX idx_coupon_code ON promotions.coupon_code(code);
CREATE INDEX idx_usage_promotion ON promotions.promotion_usage(promotion_id, used_at DESC);

-- Templates Module
CREATE INDEX idx_template_slug ON templates.template(slug);
CREATE INDEX idx_template_status ON templates.template(status);
CREATE INDEX idx_block_order ON templates.template_block(template_id, block_order);
CREATE INDEX idx_pagecontent_entity ON templates.page_content(entity_id, entity_type);
```

---

## Database Transactions & Consistency

### Critical Transaction Scopes

**1. Add to Cart** (safe with optimistic locking):
```
BEGIN TRANSACTION
  SELECT product_variant WHERE id = ? FOR UPDATE  (pessimistic lock)
  IF stock_quantity < cart_quantity: ROLLBACK with error
  INSERT INTO cart_item (cart_id, product_variant_id, quantity, ...)
  INSERT INTO reservation (cart_item_id, product_variant_id, quantity, expires_at)
  UPDATE product_variant SET reserved_quantity += quantity WHERE version = ? (optimistic lock on version)
  IF version mismatch: RETRY transaction
COMMIT
```

**2. Order Checkout** (strict serialization):
```
BEGIN TRANSACTION ISOLATION LEVEL: SERIALIZABLE
  SELECT cart WHERE id = ? FOR UPDATE (prevent concurrent checkout)
  FOR EACH cart_item:
    SELECT product_variant WHERE id = ? FOR UPDATE
    IF stock_quantity < quantity: ROLLBACK
    UPDATE product_variant SET stock_quantity -= quantity
  DELETE cart_item WHERE cart_id = ?
  DELETE reservation WHERE cart_item_id IN (...)
  INSERT order, order_items, order_payment FROM cart
  COMMIT
  PUBLISH OrderConfirmedEvent (async notification)
```

**3. Promotion Application** (prevent overselling discount):
```
BEGIN TRANSACTION
  SELECT promotion WHERE id = ? FOR UPDATE
  IF current_usage_count >= usage_limit: ROLLBACK with error
  UPDATE promotion SET current_usage_count += 1
  INSERT promotion_usage (promotion_id, order_id, discount_amount, ...)
COMMIT
```

---

## Validation & Constraints Summary

| Entity | Critical Validations |
|--------|---------------------|
| **Category** | No circular parent references; max 5 levels deep |
| **Product** | base_price ≥ cost_price; required category; immutable SKU after published |
| **ProductVariant** | unique SKU; price > 0; attributes match product schema |
| **Inventory** | available = on_hand - reserved ≥ 0; reorder rules sensible |
| **User** | email unique; valid RFC 5322; bcrypt password hash cost ≥ 12 |
| **UserProfile** | preferred_locale valid; phone E  .164 format |
| **Cart** | one active per user; no duplicate items (cart_id + variant_id unique) |
| **CartItem** | quantity > 0 < 1000; price_at_time immutable after creation |
| **Order** | totals accuracy (subtotal + tax + shipping - discount = grand_total) |
| **OrderItem** | immutable snapshots (prevents cascading data changes) |
| **Promotion** | start_date < end_date; discount_value logical per type; priority ordered |
| **CouponCode** | code unique (case-insensitive); expiry > creation; usage limit enforced |
| **Template** | slug unique; published version immutable |

---

Next Step: Proceed to Phase 1 contracts generation (`speckit.plan --phase 1-contracts`) to generate REST API contracts and module boundaries.
