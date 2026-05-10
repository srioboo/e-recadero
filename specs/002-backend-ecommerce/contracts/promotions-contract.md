# Promotions Module Contract

**Module**: Promotions  
**Status**: API Contract  
**Version**: 1.0.0

## Overview

The Promotions Module manages discounts, coupons, and promotions. It provides rules-based promotion application, coupon validation, and usage tracking for marketing campaigns.

### Responsibilities
- Promotion CRUD and lifecycle management
- Coupon code generation and validation
- Promotion rules engine (product, category, customer segment conditions)
- Usage tracking and analytics
- Automatic expiration and scheduling

---

## REST API Endpoints

### Promotion Management (Admin)

#### GET /api/v1/promotions
List promotions with filtering (admin only).

**Query Parameters**:
```
- status: DRAFT|ACTIVE|PAUSED|EXPIRED|ARCHIVED (optional)
- type: PERCENTAGE_DISCOUNT|FIXED_DISCOUNT|FREE_SHIPPING|BOGO (optional)
- from_date: ISO date (optional, active on or after)
- to_date: ISO date (optional, active on or before)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "promotion_id": "uuid-promo-1",
      "name": "Welcome 10% Off",
      "type": "PERCENTAGE_DISCOUNT",
      "discount_value": 10.0,
      "status": "ACTIVE",
      "start_date": "2026-05-01T00:00:00Z",
      "end_date": "2026-05-31T23:59:59Z",
      "usage_limit": 1000,
      "current_usage": 245,
      "priority": 10,
      "created_by": "uuid-admin-1",
      "created_at": "2026-05-01T10:00:00Z"
    }
  ],
  "pagination": { "total": 15, "limit": 20, "offset": 0 }
}
```

#### POST /api/v1/promotions
Create new promotion (admin only).

**Request**:
```json
{
  "name": "Summer Flash Sale 50% Off",
  "promotion_type": "PERCENTAGE_DISCOUNT",
  "discount_type": "PERCENTAGE",
  "discount_value": 50.0,
  "max_discount_amount": 500.00,
  "minimum_order_amount": 100.00,
  "start_date": "2026-06-01T00:00:00Z",
  "end_date": "2026-06-30T23:59:59Z",
  "usage_limit": 5000,
  "priority": 20,
  "rules": [
    {
      "rule_type": "CATEGORY_INCLUDE",
      "condition_json": {
        "category_ids": ["uuid-cat-1", "uuid-cat-2"]
      }
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "promotion_id": "uuid-promo-1",
  "name": "Summer Flash Sale 50% Off",
  "status": "ACTIVE",
  "created_at": "2026-05-20T10:00:00Z"
}
```

#### GET /api/v1/promotions/{promotion_id}
Get promotion details (admin only).

**Response** (200 OK):
```json
{
  "promotion_id": "uuid-promo-1",
  "name": "Summer Flash Sale 50% Off",
  "promotion_type": "PERCENTAGE_DISCOUNT",
  "discount_type": "PERCENTAGE",
  "discount_value": 50.0,
  "max_discount_amount": 500.00,
  "minimum_order_amount": 100.00,
  "applicable_to_sale_items": true,
  "status": "ACTIVE",
  "start_date": "2026-06-01T00:00:00Z",
  "end_date": "2026-06-30T23:59:59Z",
  "usage_limit": 5000,
  "current_usage": 1200,
  "priority": 20,
  "rules": [
    {
      "rule_id": "uuid-rule-1",
      "rule_type": "CATEGORY_INCLUDE",
      "condition_json": {
        "category_ids": ["uuid-cat-1", "uuid-cat-2"]
      }
    }
  ],
  "coupons": [
    {
      "coupon_code": "SUMMER50",
      "usage_limit": 100,
      "current_usage": 45,
      "is_active": true
    }
  ],
  "created_by": "uuid-admin-1",
  "created_at": "2026-05-20T10:00:00Z"
}
```

#### PUT /api/v1/promotions/{promotion_id}
Update promotion (admin only).

**Response** (200 OK): Updated promotion object

#### PATCH /api/v1/promotions/{promotion_id}/status
Change promotion status (admin only).

**Request**:
```json
{
  "status": "PAUSED",
  "reason": "Manual pause during review"
}
```

**Response** (200 OK): Updated promotion object

#### DELETE /api/v1/promotions/{promotion_id}
Archive promotion (soft delete, historical records retained).

**Response** (204 No Content)

---

### Promotion Rules

#### POST /api/v1/promotions/{promotion_id}/rules
Add rule to promotion (admin only).

**Request**:
```json
{
  "rule_type": "PRODUCT_INCLUDE",
  "condition_json": {
    "product_ids": ["uuid-prod-1", "uuid-prod-2"]
  }
}
```

**Response** (201 Created):
```json
{
  "rule_id": "uuid-rule-1",
  "promotion_id": "uuid-promo-1",
  "rule_type": "PRODUCT_INCLUDE",
  "condition_json": { ... }
}
```

#### DELETE /api/v1/promotions/{promotion_id}/rules/{rule_id}
Remove rule from promotion (admin only).

**Response** (204 No Content)

---

### Coupon Code Management

#### POST /api/v1/promotions/{promotion_id}/coupons
Generate coupon codes (bulk generation).

**Request**:
```json
{
  "count": 100,
  "code_prefix": "SUMMER",
  "usage_limit": 1,
  "expiry_date": "2026-06-30T23:59:59Z",
  "discount_value_override": null
}
```

**Response** (201 Created):
```json
{
  "generated_count": 100,
  "coupon_codes": [
    "SUMMER-001-ABC",
    "SUMMER-002-DEF",
    "SUMMER-003-GHI",
    ...
  ],
  "download_link": "http://api.example.com/admin/coupons/batch-20260520-xyz/download"
}
```

#### GET /api/v1/promotions/{promotion_id}/coupons
List coupon codes for promotion (admin only).

**Query Parameters**:
```
- is_active: true|false (optional)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "coupon_id": "uuid-coupon-1",
      "code": "SUMMER-001-ABC",
      "usage_limit": 1,
      "current_usage": 0,
      "is_active": true,
      "expiry_date": "2026-06-30T23:59:59Z",
      "created_at": "2026-05-20T10:00:00Z"
    }
  ],
  "pagination": { "total": 100, "limit": 20, "offset": 0 }
}
```

#### PUT /api/v1/promotions/{promotion_id}/coupons/{coupon_id}
Update coupon (admin only).

**Request**:
```json
{
  "is_active": false,
  "usage_limit": 5
}
```

**Response** (200 OK): Updated coupon object

---

### Coupon Validation (Cart/Checkout)

#### POST /api/v1/coupons/validate
Validate coupon without applying (called by Cart module).

**Request**:
```json
{
  "coupon_code": "SUMMER50",
  "cart_items": [
    {
      "product_id": "uuid-prod-1",
      "product_variant_id": "uuid-var-1",
      "category_id": "uuid-cat-1",
      "quantity": 2,
      "price": 99.99
    }
  ],
  "subtotal": 199.98,
  "user_id": "uuid-user-1"
}
```

**Response** (200 OK):
```json
{
  "is_valid": true,
  "coupon_code": "SUMMER50",
  "promotion_id": "uuid-promo-1",
  "discount_type": "PERCENTAGE",
  "discount_value": 50.0,
  "max_discount_amount": 500.00,
  "estimated_discount": 99.99,
  "conditions_met": {
    "minimum_order_met": true,
    "product_eligible": true,
    "usage_limit_not_exceeded": true,
    "expiry_valid": true,
    "campaign_active": true
  }
}
```

**Error** (400 Bad Request - Invalid Coupon):
```json
{
  "error_code": "INVALID_COUPON",
  "message": "Coupon code INVALID123 not found",
  "conditions_met": {}
}
```

**Error** (409 Conflict - Coupon Not Applicable):
```json
{
  "error_code": "COUPON_NOT_APPLICABLE",
  "message": "Coupon requires minimum order of $500; current subtotal is $199.98",
  "conditions_met": {
    "minimum_order_met": false,
    "product_eligible": true,
    "usage_limit_not_exceeded": true,
    "expiry_valid": true
  }
}
```

#### POST /api/v1/coupons/{coupon_code}/apply
Apply coupon and record usage (called by Orders module after order confirmation).

**Request**:
```json
{
  "coupon_code": "SUMMER50",
  "order_id": "uuid-order-1",
  "user_id": "uuid-user-1",
  "discount_amount": 99.99
}
```

**Response** (200 OK):
```json
{
  "promotion_usage_id": "uuid-usage-1",
  "promotion_id": "uuid-promo-1",
  "coupon_code": "SUMMER50",
  "discount_amount": 99.99,
  "applied_at": "2026-05-09T15:30:00Z"
}
```

---

### Promotion Analytics (Admin)

#### GET /api/v1/promotions/{promotion_id}/analytics
Get promotion performance metrics (admin only).

**Query Parameters**:
```
- from_date: ISO date (optional)
- to_date: ISO date (optional)
```

**Response** (200 OK):
```json
{
  "promotion_id": "uuid-promo-1",
  "promotion_name": "Summer Flash Sale 50% Off",
  "metrics": {
    "total_orders": 350,
    "unique_users": 280,
    "total_discount_amount": 17500.00,
    "average_discount_per_order": 50.00,
    "usage_rate": "35%",
    "revenue_impact": 175000.00
  },
  "daily_breakdown": [
    {
      "date": "2026-06-01",
      "orders": 50,
      "discount_amount": 2500.00
    },
    {
      "date": "2026-06-02",
      "orders": 48,
      "discount_amount": 2400.00
    }
  ],
  "top_products": [
    {
      "product_id": "uuid-prod-1",
      "product_name": "Smartphone X",
      "category_id": "uuid-cat-1",
      "usage_count": 120
    }
  ]
}
```

#### GET /api/v1/promotions/{promotion_id}/usage
Get detailed usage history (admin only).

**Query Parameters**:
```
- filter: recent|high_discount|by_user (optional)
- limit: int (1-50, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "usage_id": "uuid-usage-1",
      "order_id": "uuid-order-1",
      "user_id": "uuid-user-1",
      "coupon_code": "SUMMER50",
      "discount_amount": 99.99,
      "used_at": "2026-06-05T14:30:00Z"
    }
  ],
  "pagination": { "total": 1200, "limit": 20, "offset": 0 }
}
```

---

## Domain Events

### PromotionActivated
Emitted when promotion becomes active.

```json
{
  "event_type": "PromotionActivated",
  "event_id": "uuid",
  "aggregate_id": "promotion-uuid",
  "timestamp": "2026-06-01T00:00:00Z",
  "data": {
    "promotion_id": "uuid",
    "promotion_name": "Summer Flash Sale 50% Off",
    "start_date": "2026-06-01T00:00:00Z"
  }
}
```

### PromotionExpired
Emitted when promotion auto-expires (end_date reached).

```json
{
  "event_type": "PromotionExpired",
  "event_id": "uuid",
  "aggregate_id": "promotion-uuid",
  "timestamp": "2026-06-30T23:59:59Z",
  "data": {
    "promotion_id": "uuid",
    "promotion_name": "Summer Flash Sale 50% Off",
    "total_usage": 1200,
    "total_discount": 17500.00
  }
}
```

### CouponUsed
Emitted when coupon is applied to order.

```json
{
  "event_type": "CouponUsed",
  "event_id": "uuid",
  "aggregate_id": "coupon-code",
  "timestamp": "2026-06-05T14:30:00Z",
  "data": {
    "coupon_code": "SUMMER50",
    "promotion_id": "uuid-promo-1",
    "order_id": "uuid-order-1",
    "user_id": "uuid-user-1",
    "discount_amount": 99.99
  }
}
```

---

## Error Responses

### 409 Conflict - Invalid Promotion State
```json
{
  "error_code": "INVALID_PROMOTION_STATE",
  "message": "Cannot activate promotion that has already expired",
  "details": {
    "promotion_status": "EXPIRED",
    "end_date": "2026-05-31T23:59:59Z"
  }
}
```

### 409 Conflict - Usage Limit Exceeded
```json
{
  "error_code": "USAGE_LIMIT_EXCEEDED",
  "message": "Coupon usage limit reached",
  "details": {
    "coupon_code": "SUMMER50",
    "usage_limit": 100,
    "current_usage": 100
  }
}
```

---

## Authentication & Authorization

- **Public endpoints** (validation): No auth required; rate-limited by IP
- **Admin endpoints**: Requires ADMIN role

---

## Performance SLAs

- POST /api/v1/coupons/validate: < 100ms (in-memory rules engine)
- GET /api/v1/promotions: < 200ms
- GET /api/v1/promotions/{id}/analytics: < 500ms (complex query aggregation)

---

## Cross-Module Dependencies

- **Cart Module**: Calls POST /api/v1/coupons/validate for coupon validation
- **Orders Module**: Calls POST /api/v1/coupons/{code}/apply after order confirmation
- **Analytics Service**: Subscribes to CouponUsed events for real-time dashboards
