# Catalog Module Contract

**Module**: Catalog (Products & Categories)  
**Status**: API Contract  
**Version**: 1.0.0  

## Overview

The Catalog Module manages product hierarchies, product information, variants, and inventory. It exposes REST APIs for product discovery, search, filtering, and category navigation. The module is autonomous; external modules query via REST only.

### Responsibilities
- Category CRUD and hierarchy management
- Product information (names, descriptions, pricing)
- Product variants (sizes, colors, etc.)
- Inventory level tracking

---

## REST API Endpoints

### Categories

#### GET /api/v1/categories
Fetch list of categories with optional filtering.

**Query Parameters**:
```
- parent_id: UUID (optional) - filter by parent category
- status: ACTIVE|INACTIVE|ARCHIVED (optional, default ACTIVE)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": "uuid-1",
      "name": "Electronics",
      "slug": "electronics",
      "description": "Electronic devices and accessories",
      "parent_id": null,
      "image_url": "http://cdn.example.com/electronics.jpg",
      "sort_order": 1,
      "status": "ACTIVE",
      "created_at": "2026-05-01T10:00:00Z"
    }
  ],
  "pagination": {
    "total": 45,
    "limit": 20,
    "offset": 0,
    "has_more": true
  }
}
```

#### GET /api/v1/categories/{category_id}
Fetch single category with children.

**Response** (200 OK):
```json
{
  "id": "uuid-1",
  "name": "Electronics",
  "slug": "electronics",
  "description": "...",
  "parent_id": null,
  "image_url": "...",
  "sort_order": 1,
  "status": "ACTIVE",
  "children": [
    {
      "id": "uuid-2",
      "name": "Smartphones",
      "slug": "smartphones",
      "parent_id": "uuid-1"
    }
  ],
  "created_at": "2026-05-01T10:00:00Z"
}
```

#### POST /api/v1/categories
Create new category (admin only).

**Request**:
```json
{
  "name": "Electronics",
  "slug": "electronics",
  "description": "Electronic devices",
  "parent_id": null,
  "image_url": "http://cdn.example.com/electronics.jpg",
  "sort_order": 1
}
```

**Response** (201 Created):
```json
{
  "id": "uuid-1",
  "name": "Electronics",
  "slug": "electronics",
  "status": "ACTIVE",
  "created_at": "2026-05-01T10:00:00Z"
}
```

#### PUT /api/v1/categories/{category_id}
Update category (admin only).

**Response** (200 OK): Updated category object

#### DELETE /api/v1/categories/{category_id}
Archive category (soft delete; prevents new products added).

**Response** (204 No Content)

---

### Products

#### GET /api/v1/products
Fetch products with filtering, sorting, and search.

**Query Parameters**:
```
- category_id: UUID (optional)
- query: string (optional, search product name/description)
- sort: string (optional, default "created_at DESC")
  Valid: price_asc, price_desc, name_asc, created_at_desc, popularity
- min_price: decimal (optional)
- max_price: decimal (optional)
- in_stock_only: boolean (optional, default false)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": "uuid-1",
      "sku": "PHONE-001",
      "name": "Smartphone X",
      "description": "Latest smartphone",
      "category_id": "uuid-cat-1",
      "base_price": 999.99,
      "status": "PUBLISHED",
      "featured": true,
      "available_quantity": 42,
      "created_at": "2026-05-01T10:00:00Z"
    }
  ],
  "pagination": { "total": 150, "limit": 20, "offset": 0 }
}
```

#### GET /api/v1/products/{product_id}
Fetch product details with variants and pricing.

**Response** (200 OK):
```json
{
  "id": "uuid-1",
  "sku": "PHONE-001",
  "name": "Smartphone X",
  "description": "Latest smartphone",
  "long_description": "Detailed description...",
  "category_id": "uuid-cat-1",
  "base_price": 999.99,
  "cost_price": 500.00,
  "status": "PUBLISHED",
  "featured": true,
  "attributes": [
    {
      "name": "Color",
      "type": "SELECT",
      "values": ["Black", "White", "Blue"]
    },
    {
      "name": "Storage",
      "type": "SELECT",
      "values": ["64GB", "128GB", "256GB"]
    }
  ],
  "variants": [
    {
      "id": "uuid-var-1",
      "sku": "PHONE-001-BLK-64GB",
      "variant_attributes": {"color": "Black", "storage": "64GB"},
      "price": 999.99,
      "available_quantity": 25
    },
    {
      "id": "uuid-var-2",
      "sku": "PHONE-001-BLK-128GB",
      "variant_attributes": {"color": "Black", "storage": "128GB"},
      "price": 1099.99,
      "available_quantity": 17
    }
  ],
  "images": [
    {
      "url": "http://cdn.example.com/phone-001-main.jpg",
      "alt": "Product main image",
      "is_primary": true
    }
  ],
  "created_at": "2026-05-01T10:00:00Z",
  "updated_at": "2026-05-08T14:30:00Z"
}
```

#### GET /api/v1/products/{product_id}/availability
Check real-time product availability (for cart operations).

**Response** (200 OK):
```json
{
  "product_id": "uuid-1",
  "variants": [
    {
      "variant_id": "uuid-var-1",
      "available_quantity": 25,
      "is_available": true,
      "reorder_status": "IN_STOCK"
    }
  ],
  "timestamp": "2026-05-09T15:00:00Z"
}
```

#### POST /api/v1/products
Create product (admin only).

**Request**:
```json
{
  "sku": "PHONE-001",
  "name": "Smartphone X",
  "description": "Latest smartphone",
  "category_id": "uuid-cat-1",
  "base_price": 999.99,
  "cost_price": 500.00
}
```

**Response** (201 Created): Product object with id

#### PUT /api/v1/products/{product_id}
Update product.

**Response** (200 OK): Updated product object

#### PATCH /api/v1/products/{product_id}/status
Change product status (DRAFT, PUBLISHED, ARCHIVED).

**Request**:
```json
{
  "status": "PUBLISHED"
}
```

**Response** (200 OK): Updated product object

---

### Product Variants

#### POST /api/v1/products/{product_id}/variants
Create product variant.

**Request**:
```json
{
  "sku": "PHONE-001-BLK-64GB",
  "variant_attributes": {
    "color": "Black",
    "storage": "64GB"
  },
  "price": 999.99,
  "weight": 185.5,
  "dimensions_json": {
    "length_cm": 15.0,
    "width_cm": 7.5,
    "height_cm": 0.8
  }
}
```

**Response** (201 Created): Variant object

#### PUT /api/v1/products/{product_id}/variants/{variant_id}
Update variant.

**Response** (200 OK): Updated variant object

#### GET /api/v1/products/{product_id}/variants
List product variants.

**Response** (200 OK): Array of variant objects

---

### Inventory

#### GET /api/v1/inventory/{product_variant_id}
Check inventory level.

**Response** (200 OK):
```json
{
  "product_variant_id": "uuid-var-1",
  "quantity_on_hand": 100,
  "reserved_quantity": 25,
  "available_quantity": 75,
  "reorder_level": 10,
  "last_counted_at": "2026-05-08T10:00:00Z"
}
```

#### POST /api/v1/inventory/{product_variant_id}/adjust
Adjust inventory (admin/warehouse only).

**Request**:
```json
{
  "quantity_change": -10,
  "reason": "STOCK_ADJUSTMENT",
  "notes": "Damaged goods removal"
}
```

**Response** (200 OK): Updated inventory object

---

## Domain Events

### ProductPublished
Emitted when product transitions to PUBLISHED status.

```json
{
  "event_type": "ProductPublished",
  "event_id": "uuid",
  "aggregate_id": "product-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "product_id": "uuid",
    "sku": "PHONE-001",
    "name": "Smartphone X",
    "category_id": "uuid-cat-1",
    "base_price": 999.99
  }
}
```

### ProductPriceChanged
Emitted when product price is updated.

```json
{
  "event_type": "ProductPriceChanged",
  "event_id": "uuid",
  "aggregate_id": "product-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "product_id": "uuid",
    "old_price": 999.99,
    "new_price": 899.99,
    "effective_date": "2026-05-10T00:00:00Z"
  }
}
```

### InventoryUpdated
Emitted when inventory levels change.

```json
{
  "event_type": "InventoryUpdated",
  "event_id": "uuid",
  "aggregate_id": "product-variant-uuid",
  "timestamp": "2026-05-09T15:00:00Z",
  "data": {
    "product_variant_id": "uuid",
    "old_quantity": 100,
    "new_quantity": 90,
    "quantity_change": -10,
    "reason": "SALE_ORDER_CREATED",
    "reference_id": "order-uuid"
  }
}
```

---

## Error Responses

All errors follow standard format:

### 400 Bad Request
```json
{
  "error_code": "INVALID_REQUEST",
  "message": "SKU must be unique",
  "details": [
    {
      "field": "sku",
      "issue": "duplicate_key_violation"
    }
  ]
}
```

### 404 Not Found
```json
{
  "error_code": "NOT_FOUND",
  "message": "Product not found",
  "details": {
    "resource": "Product",
    "resource_id": "uuid"
  }
}
```

### 409 Conflict
```json
{
  "error_code": "CONFLICT",
  "message": "Product status cannot transition from PUBLISHED to DRAFT",
  "details": {
    "current_status": "PUBLISHED",
    "requested_status": "DRAFT"
  }
}
```

---

## Authentication & Authorization

- **Authentication**: Bearer JWT token in Authorization header
- **Roles**: 
  - CUSTOMER: Read products (published only), search
  - ADMIN: Full CRUD on categories, products, inventory

---

## Rate Limiting

- **Public Endpoints**: 100 requests/minute per IP
- **Authenticated Endpoints**: 1000 requests/minute per user
- **Admin Endpoints**: 500 requests/minute per admin user

---

## Performance SLAs

- GET /api/v1/products: < 500ms (100k+ products)
- GET /api/v1/products/{id}: < 200ms
- GET /api/v1/categories: < 100ms
- Search queries: < 500ms

---

## Versioning

- API Version: v1
- Future breaking changes: /api/v2 endpoint path

---

## Cross-Module Dependencies

- **Cart Module**: Calls GET /api/v1/products/{id}/availability to check stock
- **Orders Module**: Reads product snapshots for immutable order records
- **Promotions Module**: References product_id and category_id for rules (no direct queries; passed as parameters)
- **Templates Module**: References product_id for featured product blocks

