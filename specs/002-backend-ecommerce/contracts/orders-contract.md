# Orders Module Contract

**Module**: Orders  
**Status**: API Contract  
**Version**: 1.0.0

## Overview

The Orders Module manages order creation, status tracking, payment processing, shipment management, and return/refund operations. It provides comprehensive order history and administrative order management.

### Responsibilities
- Order lifecycle management (PENDING → CONFIRMED → SHIPPED → DELIVERED)
- Order item tracking (immutable snapshots)
- Payment processing coordination
- Shipment tracking
- Refund and return management

---

## REST API Endpoints

### Order Operations

#### POST /api/v1/orders
Create order from cart (triggered post-payment, called by Cart module).

**Request**:
```json
{
  "checkout_token": "checkout-token-xyz",
  "payment_method": "CREDIT_CARD",
  "transaction_id": "stripe-charge-id-12345",
  "billing_address_id": "uuid-addr-1",
  "shipping_address_id": "uuid-addr-2",
  "shipping_method_id": "uuid-ship-1",
  "coupon_code": "WELCOME10"
}
```

**Response** (201 Created):
```json
{
  "order_id": "uuid-order-1",
  "order_number": "ORD-20260509-ABC123",
  "user_id": "uuid-user-1",
  "status": "CONFIRMED",
  "items": [
    {
      "order_item_id": "uuid",
      "product_name": "Smartphone X",
      "product_sku": "PHONE-001-BLK-64GB",
      "quantity": 1,
      "unit_price": 999.99,
      "line_total": 999.99
    }
  ],
  "calculations": {
    "subtotal": 999.99,
    "tax_total": 89.99,
    "shipping_total": 15.00,
    "discount_total": 100.00,
    "grand_total": 1004.98
  },
  "payment": {
    "status": "CAPTURED",
    "method": "CREDIT_CARD",
    "transaction_id": "stripe-charge-id-12345"
  },
  "shipping": {
    "method": "EXPRESS",
    "estimated_delivery": "2026-05-12T23:59:59Z"
  },
  "created_at": "2026-05-09T15:30:00Z",
  "confirmed_date": "2026-05-09T15:30:00Z"
}
```

#### GET /api/v1/orders
Get user's orders with filtering and pagination.

**Query Parameters**:
```
- status: PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED|REFUNDED (optional)
- from_date: ISO date (optional)
- to_date: ISO date (optional)
- limit: int (1-100, default 20)
- offset: int (default 0)
- sort: created_at_desc|created_at_asc (default created_at_desc)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "order_id": "uuid-order-1",
      "order_number": "ORD-20260509-ABC123",
      "status": "CONFIRMED",
      "grand_total": 1004.98,
      "items_count": 1,
      "created_at": "2026-05-09T15:30:00Z",
      "confirmed_date": "2026-05-09T15:30:00Z",
      "estimated_delivery": "2026-05-12T23:59:59Z"
    }
  ],
  "pagination": { "total": 5, "limit": 20, "offset": 0 }
}
```

#### GET /api/v1/orders/{order_id}
Get detailed order information.

**Response** (200 OK):
```json
{
  "order_id": "uuid-order-1",
  "order_number": "ORD-20260509-ABC123",
  "user_id": "uuid-user-1",
  "status": "SHIPPED",
  "items": [
    {
      "order_item_id": "uuid",
      "product_name": "Smartphone X",
      "product_sku": "PHONE-001-BLK-64GB",
      "quantity": 1,
      "unit_price": 999.99,
      "line_discount": 0.00,
      "line_total": 999.99
    }
  ],
  "calculations": {
    "subtotal": 999.99,
    "tax_total": 89.99,
    "shipping_total": 15.00,
    "discount_total": 100.00,
    "grand_total": 1004.98
  },
  "addresses": {
    "billing": {
      "street_address": "123 Main St",
      "city": "New York",
      "state_province": "NY",
      "postal_code": "10001",
      "country_code": "US"
    },
    "shipping": {
      "street_address": "456 Oak Ave",
      "city": "Los Angeles",
      "state_province": "CA",
      "postal_code": "90001",
      "country_code": "US"
    }
  },
  "payment": {
    "status": "CAPTURED",
    "method": "CREDIT_CARD",
    "transaction_id": "stripe-charge-id-12345",
    "amount": 1004.98,
    "processed_at": "2026-05-09T15:30:00Z"
  },
  "shipment": {
    "carrier": "FEDEX",
    "tracking_number": "794629991174",
    "status": "IN_TRANSIT",
    "shipped_at": "2026-05-10T08:00:00Z",
    "estimated_delivery": "2026-05-12T23:59:59Z"
  },
  "created_at": "2026-05-09T15:30:00Z",
  "updated_at": "2026-05-10T14:00:00Z"
}
```

#### PATCH /api/v1/orders/{order_id}/cancel
Cancel pending or confirmed order.

**Request**:
```json
{
  "reason": "Out of country travel plans changed"
}
```

**Response** (200 OK):
```json
{
  "order_id": "uuid-order-1",
  "status": "CANCELLED",
  "message": "Order cancelled successfully",
  "refund_initiated": true,
  "estimated_refund_date": "2026-05-16T00:00:00Z"
}
```

**Error** (409 Conflict - Cannot Cancel):
```json
{
  "error_code": "CANNOT_CANCEL",
  "message": "Order already shipped; cannot cancel",
  "details": {
    "current_status": "SHIPPED"
  }
}
```

---

### Payment Management

#### GET /api/v1/orders/{order_id}/payment
Get order payment status.

**Response** (200 OK):
```json
{
  "payment_id": "uuid-payment-1",
  "order_id": "uuid-order-1",
  "status": "CAPTURED",
  "amount": 1004.98,
  "currency": "USD",
  "method": "CREDIT_CARD",
  "transaction_id": "stripe-charge-id-12345",
  "processed_at": "2026-05-09T15:30:00Z",
  "refund_status": "NO_REFUND",
  "refund_history": []
}
```

#### POST /api/v1/orders/{order_id}/refund
Request refund (full or partial).

**Request**:
```json
{
  "amount": 1004.98,
  "reason": "Customer requested return"
}
```

**Response** (200 OK):
```json
{
  "refund_id": "uuid-refund-1",
  "order_id": "uuid-order-1",
  "amount": 1004.98,
  "status": "INITIATED",
  "reason": "Customer requested return",
  "estimated_completion": "2026-05-16T00:00:00Z",
  "message": "Refund initiated. Check your account in 3-5 business days."
}
```

**Error** (409 Conflict - Refund Window Closed):
```json
{
  "error_code": "REFUND_WINDOW_CLOSED",
  "message": "Refunds only available within 30 days of delivery",
  "details": {
    "delivered_date": "2026-04-08T10:00:00Z",
    "days_since_delivery": 31
  }
}
```

---

### Shipment Tracking

#### GET /api/v1/orders/{order_id}/shipment
Get shipment tracking information.

**Response** (200 OK):
```json
{
  "shipment_id": "uuid-shipment-1",
  "order_id": "uuid-order-1",
  "carrier": "FEDEX",
  "tracking_number": "794629991174",
  "status": "IN_TRANSIT",
  "shipped_at": "2026-05-10T08:00:00Z",
  "estimated_delivery": "2026-05-12T23:59:59Z",
  "delivered_at": null,
  "tracking_history": [
    {
      "timestamp": "2026-05-10T08:15:00Z",
      "status": "PICKED_UP",
      "location": "New York Distribution Center"
    },
    {
      "timestamp": "2026-05-11T14:30:00Z",
      "status": "IN_TRANSIT",
      "location": "Chicago Hub"
    }
  ]
}
```

#### POST /api/v1/orders/{order_id}/shipment/webhook
Webhook endpoint for carrier updates (Fedex, UPS, DHL callbacks).

**Request** (from Carrier):
```json
{
  "tracking_number": "794629991174",
  "status": "DELIVERED",
  "timestamp": "2026-05-12T18:45:00Z",
  "location": "Los Angeles, CA",
  "signature_required": true,
  "signature_url": "http://carrier.example.com/signatures/12345"
}
```

**Response** (200 OK):
```json
{
  "received": true,
  "message": "Shipment update recorded"
}
```

---

### Order Returns

#### POST /api/v1/orders/{order_id}/return
Initiate product return.

**Request**:
```json
{
  "order_item_id": "uuid-item-1",
  "reason": "DEFECTIVE",
  "description": "Product arrived with cracked screen"
}
```

**Response** (201 Created):
```json
{
  "return_id": "uuid-return-1",
  "order_id": "uuid-order-1",
  "status": "PENDING_APPROVAL",
  "reason": "DEFECTIVE",
  "return_tracking_number": "RMA-20260509-XYZ",
  "items": 1,
  "estimated_refund": 999.99,
  "next_steps": "We'll review your return. Approval typically takes 1-2 business days.",
  "return_shipping_label": "http://cdn.example.com/return-labels/rma-xyz"
}
```

#### GET /api/v1/orders/{order_id}/returns
Get return requests for order.

**Response** (200 OK):
```json
{
  "data": [
    {
      "return_id": "uuid-return-1",
      "order_id": "uuid-order-1",
      "status": "APPROVED",
      "reason": "DEFECTIVE",
      "items_count": 1,
      "estimated_refund": 999.99,
      "created_at": "2026-05-09T16:00:00Z"
    }
  ]
}
```

---

### Admin Order Management

#### GET /api/v1/admin/orders
List all orders with admin filters.

**Query Parameters**:
```
- status: PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED|REFUNDED (optional)
- user_id: UUID (optional, filter by user)
- from_date: ISO date (optional)
- to_date: ISO date (optional)
- min_total: decimal (optional)
- max_total: decimal (optional)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "order_id": "uuid-order-1",
      "order_number": "ORD-20260509-ABC123",
      "user_id": "uuid-user-1",
      "status": "CONFIRMED",
      "grand_total": 1004.98,
      "items_count": 1,
      "created_at": "2026-05-09T15:30:00Z",
      "shipment_status": "PENDING"
    }
  ],
  "pagination": { "total": 250, "limit": 20, "offset": 0 }
}
```

#### PATCH /api/v1/admin/orders/{order_id}/status
Update order status (admin only).

**Request**:
```json
{
  "status": "SHIPPED",
  "notes": "Ready for carrier pickup"
}
```

**Response** (200 OK): Updated order object

#### POST /api/v1/admin/orders/{order_id}/shipment
Create shipment manually (admin only).

**Request**:
```json
{
  "carrier": "FEDEX",
  "tracking_number": "794629991174",
  "estimated_delivery": "2026-05-12T23:59:59Z"
}
```

**Response** (201 Created): Shipment object

---

## Domain Events

### OrderConfirmed
Emitted when order transitions to CONFIRMED (payment successful).

```json
{
  "event_type": "OrderConfirmed",
  "event_id": "uuid",
  "aggregate_id": "order-uuid",
  "timestamp": "2026-05-09T15:30:00Z",
  "data": {
    "order_id": "uuid",
    "order_number": "ORD-20260509-ABC123",
    "user_id": "uuid",
    "grand_total": 1004.98,
    "items": [...]
  }
}
```

### OrderShipped
Emitted when order transitions to SHIPPED.

```json
{
  "event_type": "OrderShipped",
  "event_id": "uuid",
  "aggregate_id": "order-uuid",
  "timestamp": "2026-05-10T08:00:00Z",
  "data": {
    "order_id": "uuid",
    "order_number": "ORD-20260509-ABC123",
    "user_id": "uuid",
    "tracking_number": "794629991174",
    "carrier": "FEDEX",
    "estimated_delivery": "2026-05-12T23:59:59Z"
  }
}
```

### OrderDelivered
Emitted when order transitions to DELIVERED.

```json
{
  "event_type": "OrderDelivered",
  "event_id": "uuid",
  "aggregate_id": "order-uuid",
  "timestamp": "2026-05-12T18:45:00Z",
  "data": {
    "order_id": "uuid",
    "order_number": "ORD-20260509-ABC123",
    "user_id": "uuid",
    "delivered_at": "2026-05-12T18:45:00Z"
  }
}
```

### OrderRefunded
Emitted when order is refunded.

```json
{
  "event_type": "OrderRefunded",
  "event_id": "uuid",
  "aggregate_id": "order-uuid",
  "timestamp": "2026-05-16T10:00:00Z",
  "data": {
    "order_id": "uuid",
    "refund_amount": 1004.98,
    "reason": "Customer return"
  }
}
```

---

## Error Responses

### 409 Conflict - Invalid Status Transition
```json
{
  "error_code": "INVALID_STATUS_TRANSITION",
  "message": "Cannot cancel order in DELIVERED status",
  "details": {
    "current_status": "DELIVERED",
    "requested_status": "CANCELLED"
  }
}
```

### 404 Not Found - Order Not Found
```json
{
  "error_code": "NOT_FOUND",
  "message": "Order not found",
  "details": {
    "resource": "Order",
    "order_id": "uuid"
  }
}
```

---

## Authentication & Authorization

- **Customer endpoints**: Requires CUSTOMER role; can only access own orders
- **Admin endpoints**: Requires ADMIN role
- **Webhook endpoints**: Requires carrier API key authentication (Bearer token)

---

## Performance SLAs

- GET /api/v1/orders: < 200ms
- GET /api/v1/orders/{id}: < 150ms
- POST /api/v1/orders: < 1s (includes payment, order creation, inventory finalization)

---

## Cross-Module Dependencies

- **Cart Module**: Creates order from confirmed cart
- **Users Module**: User verification, address validation
- **Email Service**: Sends order confirmations, shipment updates, refund notices
