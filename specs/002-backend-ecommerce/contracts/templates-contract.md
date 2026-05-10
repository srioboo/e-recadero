# Templates Module Contract

**Module**: Templates (Page Template Management)  
**Status**: API Contract  
**Version**: 1.0.0

## Overview

The Templates Module provides a dynamic page template builder for landing pages, category pages, and product pages. It supports drag-and-drop block composition, SEO metadata management, version control, and A/B testing setup.

### Responsibilities
- Template CRUD and lifecycle management
- Template block composition (reusable components)
- SEO metadata management
- Version control and publishing workflow
- Template preview and draft management
- Entity-template mapping (landing pages, category pages, product pages)

---

## REST API Endpoints

### Template Management (Admin)

#### GET /api/v1/templates
List templates with filtering (admin only).

**Query Parameters**:
```
- type: LANDING_PAGE|CATEGORY_PAGE|PRODUCT_PAGE|CUSTOM (optional)
- status: DRAFT|PUBLISHED|ARCHIVED (optional)
- created_by: UUID (optional, filter by creator)
- limit: int (1-100, default 20)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "template_id": "uuid-tmpl-1",
      "name": "Summer Sale Landing Page",
      "type": "LANDING_PAGE",
      "slug": "summer-sale-2026",
      "status": "PUBLISHED",
      "version": 3,
      "published_version": 3,
      "blocks_count": 8,
      "created_by": "uuid-admin-1",
      "created_at": "2026-05-15T10:00:00Z",
      "published_at": "2026-05-20T14:00:00Z"
    }
  ],
  "pagination": { "total": 45, "limit": 20, "offset": 0 }
}
```

#### POST /api/v1/templates
Create new template (admin only).

**Request**:
```json
{
  "name": "Summer Sale Landing Page",
  "type": "LANDING_PAGE",
  "slug": "summer-sale-2026"
}
```

**Response** (201 Created):
```json
{
  "template_id": "uuid-tmpl-1",
  "name": "Summer Sale Landing Page",
  "type": "LANDING_PAGE",
  "slug": "summer-sale-2026",
  "status": "DRAFT",
  "version": 1,
  "blocks": [],
  "created_at": "2026-05-20T10:00:00Z"
}
```

#### GET /api/v1/templates/{template_id}
Get template details with all blocks (admin only).

**Response** (200 OK):
```json
{
  "template_id": "uuid-tmpl-1",
  "name": "Summer Sale Landing Page",
  "type": "LANDING_PAGE",
  "slug": "summer-sale-2026",
  "status": "DRAFT",
  "version": 1,
  "published_version": null,
  "created_by": "uuid-admin-1",
  "created_at": "2026-05-20T10:00:00Z",
  "blocks": [
    {
      "block_id": "uuid-block-1",
      "block_type": "HERO",
      "block_name": "Main Hero Banner",
      "block_order": 1,
      "is_visible": true,
      "content": {
        "title": "Summer Sale 2026",
        "subtitle": "Up to 70% Off Everything",
        "background_image_url": "http://cdn.example.com/hero-bg.jpg",
        "cta_text": "Shop Now",
        "cta_link": "/summer-sale"
      }
    },
    {
      "block_id": "uuid-block-2",
      "block_type": "PRODUCT_GRID",
      "block_name": "Featured Products",
      "block_order": 2,
      "content": {
        "products": ["uuid-prod-1", "uuid-prod-2", "uuid-prod-3", "uuid-prod-4"],
        "layout": "4-columns",
        "show_prices": true,
        "show_ratings": true
      }
    }
  ],
  "meta": {
    "page_title": "Summer Sale 2026 - Up to 70% Off | E-Recadero",
    "page_description": "Enjoy our biggest summer sale with discounts up to 70% on electronics, clothing, and more.",
    "og_title": "Summer Sale 2026",
    "og_description": "Up to 70% off on thousands of products",
    "og_image_url": "http://cdn.example.com/og-image.jpg",
    "keywords": "summer sale, discounts, electronics, fashion, deals"
  }
}
```

#### PUT /api/v1/templates/{template_id}
Update template metadata (admin only).

**Request**:
```json
{
  "name": "Summer Sale Landing Page 2026",
  "meta": {
    "page_title": "Summer Sale 2026 - Updated Title",
    "page_description": "Updated description..."
  }
}
```

**Response** (200 OK): Updated template object

---

### Template Blocks

#### POST /api/v1/templates/{template_id}/blocks
Add block to template (admin only).

**Request**:
```json
{
  "block_type": "HERO",
  "block_name": "Hero Section",
  "block_order": 1,
  "content": {
    "title": "Summer Sale 2026",
    "subtitle": "Up to 70% Off",
    "background_image_url": "http://cdn.example.com/hero-bg.jpg",
    "cta_text": "Shop Now",
    "cta_link": "/summer-sale"
  }
}
```

**Response** (201 Created):
```json
{
  "block_id": "uuid-block-1",
  "template_id": "uuid-tmpl-1",
  "block_type": "HERO",
  "block_name": "Hero Section",
  "block_order": 1,
  "content": { ... },
  "is_visible": true,
  "created_at": "2026-05-20T10:00:00Z"
}
```

#### PUT /api/v1/templates/{template_id}/blocks/{block_id}
Update block content (admin only).

**Request**:
```json
{
  "content": {
    "title": "Updated Title",
    "subtitle": "Updated subtitle"
  },
  "block_order": 2
}
```

**Response** (200 OK): Updated block object

#### PATCH /api/v1/templates/{template_id}/blocks/{block_id}/visibility
Toggle block visibility.

**Request**:
```json
{
  "is_visible": false
}
```

**Response** (200 OK):
```json
{
  "block_id": "uuid-block-1",
  "is_visible": false
}
```

#### DELETE /api/v1/templates/{template_id}/blocks/{block_id}
Remove block from template (admin only).

**Response** (204 No Content)

#### POST /api/v1/templates/{template_id}/blocks/reorder
Reorder blocks (admin only).

**Request**:
```json
{
  "blocks": [
    { "block_id": "uuid-block-3", "block_order": 1 },
    { "block_id": "uuid-block-1", "block_order": 2 },
    { "block_id": "uuid-block-2", "block_order": 3 }
  ]
}
```

**Response** (200 OK):
```json
{
  "message": "Blocks reordered successfully"
}
```

---

### Template Metadata

#### PUT /api/v1/templates/{template_id}/meta
Update SEO and social metadata (admin only).

**Request**:
```json
{
  "page_title": "Summer Sale 2026 - E-Recadero",
  "page_description": "Enjoy our biggest summer sale with discounts...",
  "og_title": "Summer Sale 2026",
  "og_description": "Up to 70% off",
  "og_image_url": "http://cdn.example.com/og-image.jpg",
  "keywords": "summer sale, discounts, electronics",
  "canonical_url": "https://www.example.com/summer-sale-2026",
  "robots_directive": "index,follow",
  "structured_data_json": {
    "@context": "https://schema.org",
    "@type": "Event",
    "name": "Summer Sale 2026",
    "url": "https://www.example.com/summer-sale-2026"
  }
}
```

**Response** (200 OK):
```json
{
  "template_id": "uuid-tmpl-1",
  "meta": { ... },
  "updated_at": "2026-05-20T15:00:00Z"
}
```

---

### Template Publishing & Versioning

#### POST /api/v1/templates/{template_id}/publish
Publish template (transitions DRAFT → PUBLISHED).

**Request**:
```json
{
  "change_note": "Initial launch of summer sale page"
}
```

**Response** (200 OK):
```json
{
  "template_id": "uuid-tmpl-1",
  "status": "PUBLISHED",
  "version": 1,
  "published_version": 1,
  "published_at": "2026-05-20T15:00:00Z",
  "message": "Template published successfully"
}
```

#### POST /api/v1/templates/{template_id}/unpublish
Unpublish template (transitions PUBLISHED → DRAFT).

**Response** (200 OK):
```json
{
  "template_id": "uuid-tmpl-1",
  "status": "DRAFT",
  "message": "Template unpublished. Previous version remains live."
}
```

#### GET /api/v1/templates/{template_id}/versions
Get template version history (admin only).

**Query Parameters**:
```
- limit: int (1-50, default 10)
- offset: int (default 0)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "version_id": "uuid-ver-3",
      "template_id": "uuid-tmpl-1",
      "version_number": 3,
      "published_at": "2026-05-22T14:00:00Z",
      "created_by": "uuid-admin-1",
      "change_note": "Updated hero image and CTA link"
    },
    {
      "version_id": "uuid-ver-2",
      "version_number": 2,
      "published_at": "2026-05-20T15:00:00Z",
      "created_by": "uuid-admin-1",
      "change_note": "Added testimonials section"
    }
  ],
  "pagination": { "total": 3, "limit": 10, "offset": 0 }
}
```

#### GET /api/v1/templates/{template_id}/versions/{version_number}
Get template snapshot for specific version (admin only).

**Response** (200 OK):
```json
{
  "version_number": 2,
  "template_id": "uuid-tmpl-1",
  "blocks": [ ... ],
  "meta": { ... },
  "created_at": "2026-05-20T15:00:00Z",
  "created_by": "uuid-admin-1"
}
```

#### POST /api/v1/templates/{template_id}/revert/{version_number}
Revert to previous version (creates new draft from snapshot).

**Request**:
```json
{
  "change_note": "Reverting to version 2 - old design performed better"
}
```

**Response** (201 Created):
```json
{
  "template_id": "uuid-tmpl-1",
  "status": "DRAFT",
  "version": 4,
  "message": "Template reverted to version 2. New draft version 4 created for review.",
  "blocks": [ ... ]
}
```

---

### Page Content Mapping

#### POST /api/v1/templates/{template_id}/map-entity
Assign template to an entity (category, product, or landing page).

**Request**:
```json
{
  "entity_id": "uuid-cat-1",
  "entity_type": "CATEGORY",
  "status": "PUBLISHED"
}
```

**Response** (201 Created):
```json
{
  "page_content_id": "uuid-pc-1",
  "template_id": "uuid-tmpl-1",
  "entity_id": "uuid-cat-1",
  "entity_type": "CATEGORY",
  "status": "PUBLISHED",
  "published_at": "2026-05-20T15:00:00Z"
}
```

#### GET /api/v1/templates/{template_id}/entities
Get entities using this template (admin only).

**Query Parameters**:
```
- entity_type: PRODUCT|CATEGORY|LANDING_PAGE|USER (optional)
- status: DRAFT|PUBLISHED|ARCHIVED (optional)
```

**Response** (200 OK):
```json
{
  "data": [
    {
      "page_content_id": "uuid-pc-1",
      "entity_id": "uuid-cat-1",
      "entity_name": "Electronics Category",
      "entity_type": "CATEGORY",
      "status": "PUBLISHED",
      "published_at": "2026-05-20T15:00:00Z"
    }
  ],
  "pagination": { "total": 1, "limit": 20, "offset": 0 }
}
```

#### DELETE /api/v1/templates/{template_id}/map-entity/{entity_id}
Remove template from entity.

**Response** (204 No Content)

---

### Template Preview & Public APIs

#### GET /api/templates/{template_slug}
Fetch published template for frontend rendering (public, no auth required).

**Response** (200 OK):
```json
{
  "template_id": "uuid-tmpl-1",
  "name": "Summer Sale Landing Page",
  "slug": "summer-sale-2026",
  "blocks": [
    {
      "block_type": "HERO",
      "content": { ... }
    },
    {
      "block_type": "PRODUCT_GRID",
      "content": { ... }
    }
  ],
  "meta": {
    "page_title": "Summer Sale 2026 - E-Recadero",
    "page_description": "Enjoy our biggest summer sale...",
    "og_title": "Summer Sale 2026",
    "og_image_url": "http://cdn.example.com/og-image.jpg"
  }
}
```

#### GET /api/templates/preview/{template_id}
Preview template version (admin only, can preview unpublished).

**Query Parameters**:
```
- version: int (optional, defaults to current version)
```

**Response** (200 OK): Template object (same as public endpoint but always returns requested version)

---

## Template Block Types

### Supported Block Types

**HERO**
```json
{
  "title": "Main heading",
  "subtitle": "Subheading",
  "background_image_url": "...",
  "cta_text": "Call to action",
  "cta_link": "/path"
}
```

**PRODUCT_GRID**
```json
{
  "products": ["uuid-prod-1", "uuid-prod-2", ...],
  "layout": "2-columns|3-columns|4-columns",
  "show_prices": true,
  "show_ratings": true
}
```

**CATEGORY_LIST**
```json
{
  "category_ids": ["uuid-cat-1", "uuid-cat-2", ...],
  "items_per_row": 3,
  "show_subcategories": true
}
```

**FEATURED_PRODUCTS**
```json
{
  "products": ["uuid-prod-1", "uuid-prod-2", ...],
  "title": "Featured Items",
  "layout": "carousel|grid"
}
```

**RICH_TEXT**
```json
{
  "html_content": "<p>Custom HTML content...</p>",
  "text_alignment": "left|center|right",
  "background_color": "#ffffff"
}
```

**IMAGE_BANNER**
```json
{
  "image_url": "...",
  "alt_text": "Banner alt text",
  "link_url": "/path",
  "overlay_color": "#000000",
  "overlay_opacity": 0.3
}
```

**TESTIMONIALS**
```json
{
  "testimonials": [
    {
      "author": "John Doe",
      "content": "Great products!",
      "rating": 5
    }
  ],
  "layout": "carousel|grid"
}
```

**CTA (Call To Action)**
```json
{
  "text": "Shop Now",
  "link": "/path",
  "button_style": "primary|secondary",
  "button_size": "small|medium|large"
}
```

**HEADER**
```json
{
  "logo_url": "...",
  "navigation_links": [
    {
      "label": "Home",
      "link": "/"
    }
  ]
}
```

**FOOTER**
```json
{
  "company_info": "About text",
  "links": [ ... ],
  "social_links": [ ... ]
}
```

---

## Domain Events

### TemplatePublished
Emitted when template is published.

```json
{
  "event_type": "TemplatePublished",
  "event_id": "uuid",
  "aggregate_id": "template-uuid",
  "timestamp": "2026-05-20T15:00:00Z",
  "data": {
    "template_id": "uuid",
    "template_name": "Summer Sale Landing Page",
    "version": 1,
    "slug": "summer-sale-2026"
  }
}
```

### TemplateArchived
Emitted when template is archived.

```json
{
  "event_type": "TemplateArchived",
  "event_id": "uuid",
  "aggregate_id": "template-uuid",
  "timestamp": "2026-05-25T10:00:00Z",
  "data": {
    "template_id": "uuid",
    "template_name": "Summer Sale Landing Page"
  }
}
```

---

## Error Responses

### 409 Conflict - Slug Already Exists
```json
{
  "error_code": "DUPLICATE_SLUG",
  "message": "Template slug must be unique",
  "details": {
    "field": "slug",
    "provided_slug": "summer-sale-2026"
  }
}
```

### 400 Bad Request - Invalid Block Content
```json
{
  "error_code": "INVALID_BLOCK_CONTENT",
  "message": "Block content validation failed",
  "details": {
    "block_type": "PRODUCT_GRID",
    "errors": [
      {
        "field": "products",
        "issue": "Product uuid-invalid-id does not exist"
      }
    ]
  }
}
```

---

## Authentication & Authorization

- **Admin endpoints** (POST, PUT, DELETE, PREVIEW): Requires ADMIN role
- **Public endpoints** (GET /api/templates/{slug}): No auth required
- **Analytics endpoints**: Requires ADMIN role

---

## Performance SLAs

- GET /api/v1/templates: < 200ms
- GET /api/templates/{slug}: < 100ms (cached)
- POST /api/v1/templates/{id}/publish: < 500ms (includes cache invalidation)

---

## Cross-Module Dependencies

- **Catalog Module**: Validates product_ids and category_ids in block content
- **Frontend/Client**: Consumes GET /api/templates/{slug} for page rendering
- **CDN Cache**: Template publish event triggers cache invalidation
