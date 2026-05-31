# Frontend Tasks: Product Catalog Module (Astro)

**Input**: Design documents from `/specs/001-migrate-svelte-astro/`  
**Tech Stack**: Astro 4.x + TypeScript + Tailwind CSS + React islands (optional)  
**Dependency**: Backend Catalog APIs (T034-T060) — REST endpoints must be available  
**Related Backend**: `/specs/002-backend-ecommerce/tasks.md` T034-T060

---

## Catalog Frontend Prerequisites

**Backend Blocking Dependencies:**
- ✅ T034-T050: Catalog entities, repos, services, controllers (product search, detail, availability)
- ✅ T051-T055: Domain events (ProductPublishedEvent, ProductPriceChangedEvent, etc.)
- ✅ T056-T058: Unit tests (CategoryService, ProductService, InventoryService)
- ⏳ T059-T060: Contract & integration tests (unblocks production-ready APIs)

**Frontend can start once T056-T058 complete** (unit tests pass = APIs are stable)

---

## Phase 1: Catalog Product Components (Depends: Backend T056-T058 ✅)

**Purpose**: Build reusable Astro components for product browsing & display

### UI Components (No Backend Data Required)

- [x] F001 Create ProductCard.astro component:
  - Image, name, price, rating (stars), "Add to Cart" button
  - Responsive grid layout (mobile-first)
  - Hover effects and loading state
  - Location: `src/components/catalog/ProductCard.astro`

- [x] F002 Create ProductGrid.astro layout:
  - Grid container with configurable columns (1 mobile, 2 tablet, 3-4 desktop)
  - Pagination controls (prev/next + page numbers)
  - Location: `src/components/catalog/ProductGrid.astro`

- [x] F003 Create PriceDisplay.astro component:
  - Format currency (EUR) with thousands separator
  - Show sale price vs base price with discount %
  - Low stock warning badge (qty < 5)
  - Location: `src/components/shared/PriceDisplay.astro`

- [x] F004 Create RatingStars.astro component:
  - Display 1-5 star rating (filled/empty)
  - Show review count tooltip
  - Location: `src/components/shared/RatingStars.astro`

### Fetch Utilities & Types

- [x] F005 Create `src/lib/api.ts` with Astro fetch helpers:
  - `fetchCategories()`: List all categories (paginated, GET /api/v1/categories)
  - `fetchProducts(query, categoryId, minPrice, maxPrice)`: Search products with filters
  - `fetchProductDetail(productId)`: Get single product + variants
  - `fetchProductAvailability(productId)`: Check in-stock status
  - Error handling (404, 500, timeout)
  - Caching strategy (HTTP headers, optional Redis in middleware)

- [x] F006 Create `src/types/catalog.ts` TypeScript interfaces:
  - `Category`: id, name, slug, description, parentId, imageUrl, children[]
  - `Product`: id, sku, name, description, price, salePrice, categoryId, images[], variants[]
  - `ProductVariant`: id, sku, attributes, price (override), weight
  - `Availability`: variantId, quantityAvailable, reorderStatus (OUT_OF_STOCK/LOW_STOCK/IN_STOCK)
  - `SearchFilters`: query, categoryId, minPrice, maxPrice, sortBy, page

---

## Phase 2: Catalog Pages (Depends: F001-F006 complete)

**Purpose**: Server-rendered Astro pages for product browsing

### Product Listing Page

- [x] F007 Create `src/pages/catalog/index.astro`:
  - Server: Fetch categories + paginate products (query params: category, q, page)
  - UI: Category sidebar, product grid, search input, filter panel
  - On mount: Show loading skeleton, fetch real data client-side (optional with View Transitions)
  - Location: `/catalog` route

- [x] F008 Create `src/pages/catalog/[slug].astro` (Category detail):
  - Server: Fetch category + children + products in category
  - UI: Category title, breadcrumb, product grid filtered by category
  - Pagination: 12 products per page
  - Sort: By name, price, newest

- [x] F009 Create `src/pages/product/[id].astro` (Product detail):
  - Server: Fetch product detail + variants + availability
  - UI: Large image gallery, title, price, description, specs table
  - Variant selector: Dropdown/buttons for size/color/etc
  - Availability indicator: "In Stock" / "Low Stock" / "Out of Stock"
  - CTA: "Add to Cart" button (disabled if OOS)
  - Related products: Fetch 4 related items from same category
  - Location: `/product/[id]` route

### Search & Filter

- [x] F010 Create `src/components/catalog/SearchInput.astro`:
  - Text input with debounced search (500ms)
  - Clear button, search icon
  - Focus state: Show recent searches or category suggestions

- [x] F011 Create `src/components/catalog/FilterPanel.astro`:
  - Checkbox filters: Category, Brand, Price range slider, Availability (In stock/Low stock)
  - Apply/Reset buttons
  - Show active filter count badge
  - Collapse/expand on mobile

---

## Phase 3: Integration with Cart & Checkout (Depends: Cart module backend T061+, F007-F011)

**Purpose**: Connect catalog to shopping cart

### Add-to-Cart Integration

- [ ] F012 Update `src/components/catalog/ProductCard.astro`:
  - Add "Add to Cart" button with quantity selector (spinner +/-)
  - On click: POST to `/api/v1/cart/items` with variantId + quantity
  - Toast notification: "Added 3x Product Name" / "Out of stock"
  - Disable button if inventory.quantityAvailable < 1

- [ ] F013 Update `src/pages/product/[id].astro`:
  - Quantity selector: 1-100 (max: inventory.available)
  - "Add to Cart" button calls cart API
  - "Add to Wishlist" button (optional, stores in localStorage)

### Cart Updates

- [ ] F014 Create `src/lib/cart.ts` client utility:
  - `addToCart(variantId, quantity)`: POST request
  - `removeFromCart(cartItemId)`: DELETE request
  - `updateCartQuantity(cartItemId, newQty)`: PUT request
  - Client-side optimistic UI updates (show spinner until API responds)

---

## Phase 4: Performance & Polish (Depends: F007-F014 complete)

**Purpose**: Optimization, SEO, accessibility

### Image Optimization

- [ ] F015 Setup image optimization:
  - Use Astro's `<Image>` component with cloudinary/CDN
  - Generate responsive srcset (320w, 640w, 1280w)
  - Lazy load below-fold images
  - Placeholder: LQIP (low-quality image placeholder)

### SEO & Meta Tags

- [ ] F016 Add SEO for all pages:
  - Product listing: Meta description, Open Graph image, JSON-LD schema (Product collection)
  - Product detail: Title (Product Name + "| Recadero"), description, OG image, breadcrumb JSON-LD
  - Use Astro's `<SEO>` or manual meta tags

### Accessibility

- [ ] F017 Audit accessibility (WCAG 2.1 AA):
  - Image alt text on all ProductCard images
  - ARIA labels on buttons (e.g., `aria-label="Add to cart"`)
  - Color contrast on price text (min 4.5:1 for normal text)
  - Keyboard navigation: Tab through filters, search, Add to Cart

### Performance

- [ ] F018 Measure & optimize Core Web Vitals:
  - LCP (Largest Contentful Paint) < 2.5s
  - FID (First Input Delay) < 100ms
  - CLS (Cumulative Layout Shift) < 0.1
  - Use Astro Integrations: `astro-compress`, minify CSS/JS

---

## Phase 5: Localization & Regional Support (Depends: F001-F018 complete)

**Purpose**: Support multiple languages and regional preferences

### i18n Setup

- [ ] F019 Configure `astro-i18n` or `astro-i18next`:
  - Locales: `es`, `en`, `fr` (configurable in frontmatter)
  - Currency: EUR (Spain), USD (US), GBP (UK)
  - Routing: `/es/catalogo`, `/en/catalog`, `/fr/catalogue`
  - Fallback: ES → EN

### Localized Components

- [ ] F020 Create localized product display:
  - Translate category names, product descriptions
  - Format price by locale currency
  - Format dates (product added, reviews) by locale
  - Right-to-left support (if needed for future Arabic locale)

---

## Integration Timeline

| Backend Phase | Frontend Phase | Status | Notes |
|---|---|---|---|
| T034-T050 (Entities-Controllers) | F001-F006 (Components+API) | ✅ Ready | Can build in parallel after T050 |
| T051-T055 (Events) | F001-F011 (Pages+Search) | ✅ Ready | Events inform cart notifications |
| T056-T058 (Unit Tests) | F007-F011 (Catalog Pages) | ✅ **GO** | **Start here** |
| T059-T060 (Integration Tests) | F012-F014 (Cart Integration) | 🔒 Blocked | Wait for cart backend (T061+) |
| T061+ (Users, Cart, Orders) | F015-F020 (Polish, i18n) | 🔒 Blocked | Cart module required |

---

## Dependency Graph (Frontend)

```
F001-F004 (Components)
    ↓
F005-F006 (API Utils + Types)
    ↓
F007-F011 (Pages + Search)  ← T056-T058 (Backend tests) required
    ↓
F012-F014 (Cart Integration) ← T061+ (Cart backend) required
    ↓
F015-F018 (Perf + SEO + A11y)
    ↓
F019-F020 (i18n)
```

---

## Backend-to-Frontend API Contract

**Endpoints Required (from backend T049-T050):**

```bash
# Category List
GET /api/v1/categories
Response: { items: Category[], total, page, pageSize }

# Product Search
GET /api/v1/products?query=iphone&categoryId=1&minPrice=100&maxPrice=1000&page=1&size=12
Response: { items: Product[], total, page, pageSize }

# Product Detail
GET /api/v1/products/{id}
Response: Product { id, sku, name, price, description, variants[], images[] }

# Product Availability
GET /api/v1/products/{id}/availability
Response: ProductAvailabilityResponse { productId, variants[{ variantId, available, reorderStatus }] }

# Inventory for Variant
GET /api/v1/inventory/{variantId}
Response: InventoryResponse { quantityOnHand, availableQuantity, reorderLevel, lastCountedAt }

# Low Stock Alert (optional, admin only)
GET /api/v1/inventory/low-stock
Response: InventoryResponse[] (with auth header)
```

---

## Quality Checklist (Before Production)

**For each Page (F007-F009):**
- [ ] Renders without JavaScript (SSR works)
- [ ] Images load with fallback
- [ ] Error state displayed (404, 500 handling)
- [ ] Loading state with skeleton
- [ ] Mobile responsive (tested on 375px, 768px, 1920px)
- [ ] Lighthouse score ≥ 90 (Performance, Accessibility, Best Practices)
- [ ] No console errors/warnings

**For each Component (F001-F006):**
- [ ] Props typed with TypeScript
- [ ] Handles missing/null data gracefully
- [ ] Accessible (ARIA labels, keyboard nav)
- [ ] Tested with visual regression (e.g., Chromatic)

**API Integration (F005):**
- [ ] Error responses logged
- [ ] Timeout handling (>5s → fallback/retry)
- [ ] Cache headers honored (if backend sends Cache-Control)
- [ ] Rate limiting considered (if backend has quotas)

---

## Notes

- **Start Date**: After T056-T058 pass (backend unit tests stable)
- **Timeline**: F001-F011 = ~1 week (2 devs), F012-F020 = ~1 week (depends on cart backend)
- **Tech Debt**: Cypress E2E tests for critical user flows (add to cart, search, detail view)
- **Future**: Analytics (product views, search queries), A/B testing (price display, CTA text)
