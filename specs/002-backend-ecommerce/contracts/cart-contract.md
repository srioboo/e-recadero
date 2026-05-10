# Shopping Cart Module Contract

**Module**: Shopping Cart  
**Status**: API Contract  
**Version**: 1.0.0

## Overview

The Shopping Cart Module manages cart operations, cart items, inventory reservations, and cart promotion tracking. It handles adding/removing items, calculating totals, and preparing carts for checkout.

### Responsibilities
- Cart creation and management
- Cart item management (add, update, remove)
- Inventory reservation
- Promotion/coupon application
- Cart expiration and cleanup

---

## REST API Endpoints

### Cart Operations

#### GET /api/v1/cart
Get current user's active cart (or create if none exists).

**Response** (200 OK):
```json
{
  "cart_id": "uuid",
  "user_id": "uuid",
  "status": "ACTIVE",
  "items": [
    {
      "cart_item_id": "uuid-item-1",
      "product_variant_id": "uuid-var-1",
      "product_name": "Smartphone X",
      "product_sku": "PHONE-001-BLK-64GB",
      "variant_attributes": {"color": "Black", "storage": "64GB"},
      "quantity": 1,
      "price_at_time": 999.99,
      "discount_applied": 0.00,
      "line_total": 999.99,
      "is_in_stock": true
    }
  ],
  "applied_promotions": [
    {
      "promotion_id": "uuid-promo-1",
      "coupon_code": "WELCOME10",
      "discount_amount": 100.00
    }
  ],
  "calculations": {
    "subtotal": 999.99,
    "discount_total": 100.00,
    "tax_total": 0.00,
    "shipping_total": 0.00,
    "grand_total": 899.99
  },
  "created_at": "2026-05-09T10:00:00Z",
  "expires_at": "2026-05-10T10:00:00Z"
}
```

#### POST /api/v1/cart/items
Add item to cart.

**Request**:
```json
{
  "product_variant_id": "uuid-var-1",
  "quantity": 1
}
```

**Response** (201 Created):
```json
{
  "cart_item_id": "uuid-item-1",
  "cart_id": "uuid",
  "product_variant_id": "uuid-var-1",
  "quantity": 1,
  "price_at_time": 999.99,
  "line_total": 999.99,
  "message": "Item added to cart",
  "cart_updated": {
    "total_items": 1,
    "grand_total": 999.99
  }
}
```

**Error** (409 Conflict - Out of Stock):
```json
{
  "error_code": "OUT_OF_STOCK",
  "message": "Requested quantity exceeds available stock",
  "details": {
    "requested_quantity": 10,
    "available_quantity": 3
  }
}
```

#### PUT /api/v1/cart/items/{cart_item_id}
Update cart item quantity.

**Request**:
```json
{
  "quantity": 2
}
```

**Response** (200 OK):
```json
{
  "cart_item_id": "uuid-item-1",
  "quantity": 2,
  "line_total": 1999.98,
  "cart_updated": {
    "total_items": 2,
    "grand_total": 1899.98
  }
}
```

#### DELETE /api/v1/cart/items/{cart_item_id}
Remove item from cart.

**Response** (204 No Content)

#### POST /api/v1/cart/clear
Clear entire cart.

**Response** (200 OK):
```json
{
  "message": "Cart cleared successfully",
  "items_removed": 3
}
```

---

### Promotions & Coupons

#### POST /api/v1/cart/apply-coupon
Apply coupon code to cart.

**Request**:
```json
{
  "coupon_code": "WELCOME10"
}
```

**Response** (200 OK):
```json
{
  "promotion_id": "uuid-promo-1",
  "coupon_code": "WELCOME10",
  "discount_amount": 100.00,
  "discount_type": "PERCENTAGE",
  "discount_value": 10.0,
  "cart_updated": {
    "discount_total": 100.00,
    "grand_total": 899.99
  }
}
```

**Error** (400 Bad Request - Invalid Coupon):
```json
{
  "error_code": "INVALID_COUPON",
  "message": "Coupon code INVALID123 not found or expired",
  "details": {
    "coupon_code": "INVALID123"
  }
}
```

**Error** (409 Conflict - Coupon Not Applicable):
```json
{
  "error_code": "COUPON_NOT_APPLICABLE",
  "message": "Coupon HIGHMINIMUM requires minimum order of $500",
  "details": {
    "minimum_order_amount": 500.00,
    "current_cart_total": 299.99
  }
}
```

#### DELETE /api/v1/cart/remove-coupon
Remove applied coupon from cart.

**Response** (200 OK):
```json
{
  "message": "Coupon removed",
  "cart_updated": {
    "discount_total": 0.00,
    "grand_total": 999.99
  }
}
```

#### GET /api/v1/cart/validate-coupon
Validate coupon without applying (preview discount).

**Query Parameters**:
```
- coupon_code: string (required)
```

**Response** (200 OK):
```json
{
  "is_valid": true,
  "coupon_code": "WELCOME10",
  "discount_amount": 100.00,
  "discount_type": "PERCENTAGE",
  "discount_value": 10.0,
  "conditions_met": true,
  "conditions": {
    "minimum_order_amount_met": true,
    "usage_limit_not_exceeded": true,
    "expiry_valid": true
  }
}
```

---

### Cart Checkout Preparation

#### POST /api/v1/cart/prepare-checkout
Prepare cart for checkout (recalculate prices, verify inventory, lock cart).

**Request**:
```json
{
  "shipping_method_id": "uuid-shipping-1",
  "billing_address_id": "uuid-addr-1",
  "shipping_address_id": "uuid-addr-2"
}
```

**Response** (200 OK):
```json
{
  "cart_id": "uuid",
  "status": "LOCKED_FOR_CHECKOUT",
  "checkout_token": "checkout-token-xyz",
  "calculations": {
    "subtotal": 999.99,
    "tax_total": 89.99,
    "shipping_total": 15.00,
    "discount_total": 100.00,
    "grand_total": 1004.98
  },
  "estimated_delivery": "2026-05-12T23:59:59Z",
  "expires_at": "2026-05-09T15:30:00Z",
  "message": "Cart ready for checkout. Please complete payment within 30 minutes."
}
```

**Error** (409 Conflict - Cart Changed):
```json
{
  "error_code": "CART_MODIFIED",
  "message": "Product prices changed since last update. Please review your cart.",
  "price_changes": [
    {
      "product_variant_id": "uuid-var-1",
      "old_price": 999.99,
      "new_price": 899.99
    }
  ]
}
```

#### POST /api/v1/cart/confirm-checkout
Confirm checkout and create order (called after payment success).

**Request**:
```json
{
  "checkout_token": "checkout-token-xyz",
  "payment_method": "CREDIT_CARD",
  "transaction_id": "stripe-charge-id"
}
```

**Response** (201 Created):
```json
{
  "order_id": "uuid-order-1",
  "order_number": "ORD-20260509-XYZ123",
  "cart_id": "uuid",
  "grand_total": 1004.98,
  "message": "Order created successfully. Check your email for confirmation.",
  "next_step": "/orders/ORD-20260509-XYZ123"
}
```

---

### Cart Recovery & Listing

#### GET /api/v1/cart/history
Get list of past carts (checked out or abandoned).

**Query Parameters**:
```
- status: CHECKED_OUT|ABANDONED (optional)
- limit: int (default 10)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "cart_id": "uuid-1",
      "items_count": 3,
      "grand_total": 1004.98,
      "status": "CHECKED_OUT",
      "created_at": "2026-05-09T10:00:00Z",
      "expires_at": "2026-05-10T10:00:00Z"
    }
  ],
  "pagination": { "total": 5, "limit": 10, "offset": 0 }
}
```

#### POST /api/v1/cart/recover/{old_cart_id}
Recover abandoned cart (restore items).

**Response** (200 OK):
```json
{
  "cart_id": "uuid-new",
  "message": "Abandoned cart recovered",
  "items_restored": 2,
  "warnings": [
    {
      "product_variant_id": "uuid-var-2",
      "issue": "PRICE_CHANGED",
      "old_price": 49.99,
      "new_price": 39.99
    }
  ]
}
```

---

## Domain Events

### CartItemAdded
Emitted when item added to cart.

```json
{
  "event_type": "CartItemAdded",
  "event_id": "uuid",
  "aggregate_id": "cart-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "cart_id": "uuid",
    "user_id": "uuid",
    "product_variant_id": "uuid-var-1",
    "quantity": 1,
    "price": 999.99
  }
}
```

### CartAbandoned
Emitted when cart expires without checkout (24 hours).

```json
{
  "event_type": "CartAbandoned",
  "event_id": "uuid",
  "aggregate_id": "cart-uuid",
  "timestamp": "2026-05-10T10:00:00Z",
  "data": {
    "cart_id": "uuid",
    "user_id": "uuid",
    "items_count": 2,
    "grand_total": 999.99,
    "abandoned_at": "2026-05-10T10:00:00Z"
  }
}
```

### CheckoutStarted
Emitted when cart transitions to checkout (inventory locked).

```json
{
  "event_type": "CheckoutStarted",
  "event_id": "uuid",
  "aggregate_id": "cart-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "cart_id": "uuid",
    "user_id": "uuid",
    "items": [...],
    "grand_total": 1004.98,
    "checkout_token": "checkout-token-xyz"
  }
}
```

---

## Error Responses

Standard error format (same as Catalog module).

### 400 Bad Request - Item Quantity Invalid
```json
{
  "error_code": "INVALID_QUANTITY",
  "message": "Quantity must be between 1 and 1000",
  "details": {
    "field": "quantity",
    "provided_value": 2500
  }
}
```

### 404 Not Found - Cart Item Not Found
```json
{
  "error_code": "NOT_FOUND",
  "message": "Cart item not found",
  "details": {
    "resource": "CartItem",
    "resource_id": "uuid-item-1"
  }
}
```

---

## Authentication & Authorization

- **All cart endpoints**: Requires authentication (CUSTOMER role)
- **Cart ownership**: Users can only access their own carts

---

## Performance SLAs

- GET /api/v1/cart: < 100ms
- POST /api/v1/cart/items: < 100ms
- POST /api/v1/cart/apply-coupon: < 200ms
- POST /api/v1/cart/prepare-checkout: < 500ms (includes price recalc + tax calculation)

---

## Cross-Module Dependencies

- **Catalog Module**: GET /api/v1/products/{id}/availability for stock verification
- **Promotions Module**: Coupon validation and discount calculation
- **Orders Module**: Creates order from confirmed cart
- **Email Service**: Listens to CartAbandoned events for recovery emails
