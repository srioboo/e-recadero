# Backend-Frontend Delivery Timeline

## Current Status

**Backend Catalog Module**: ✅ **v0.1.0-catalog-services** complete (T034-T058)

### What's Ready Now
- ✅ 21 REST API endpoints (search, detail, availability, inventory)
- ✅ 41 unit tests (all passing)
- ✅ Domain events system (async notifications)
- ✅ Full CRUD for products, categories, inventory
- ❌ Contract & Integration tests (T059-T060 pending)

---

## Frontend Parallel Track

### Can Start NOW (F001-F006)
**Dependencies**: None (component library, type definitions, API utilities)

| Phase | Tasks | Duration | Backend Dependency |
|-------|-------|----------|---|
| Components & API | F001-F006 | 2-3 days | ✅ None needed |

**What to build**:
- ProductCard, ProductGrid components (Astro)
- TypeScript types (Category, Product, Availability)
- API fetch utilities (with error handling)

---

### Can Start AFTER T056-T058 ✅ (F007-F011)
**Dependency**: Backend unit tests passing = APIs are stable

| Phase | Tasks | Duration | Backend Dependency |
|-------|-------|----------|---|
| Catalog Pages | F007-F011 | 3-4 days | ✅ **T056-T058** |
| - Listing page (`/catalog`) | F007 | | |
| - Category pages (`/catalog/[slug]`) | F008 | | |
| - Product detail (`/product/[id]`) | F009 | | |
| - Search & filtering | F010-F011 | | |

**What to build**:
- Server-rendered Astro pages
- Connect to real backend APIs
- Search functionality with filters
- Responsive layouts

---

### BLOCKED: Waiting for Cart Backend (T061+) 🔒
**Dependency**: Cart module (T061+) must complete first

| Phase | Tasks | Duration | Backend Dependency |
|-------|-------|----------|---|
| Cart Integration | F012-F014 | 2-3 days | 🔒 T061+ (Cart backend) |
| - Add-to-cart button | F012-F013 | | |
| - Cart API utilities | F014 | | |

**Why blocked**: Need `/api/v1/cart/items` endpoints to test Add-to-Cart

---

### Lower Priority: Polish & i18n (F015-F020)
**Dependency**: F012-F014 complete, Cart/Orders modules stable

| Phase | Tasks | Duration | Backend Dependency |
|-------|-------|----------|---|
| Performance | F015-F018 | 2-3 days | 🔄 F012-F014 |
| - Image optimization | F015 | | |
| - SEO metadata | F016 | | |
| - Accessibility (WCAG AA) | F017 | | |
| - Core Web Vitals | F018 | | |
| Localization | F019-F020 | 1-2 days | 🔄 All phases |
| - Multi-language support | F019 | | |
| - Currency/regional formatting | F020 | | |

---

## Recommended Team Allocation

### Team A: Frontend Components (Start NOW)
**Duration**: 3-4 days
**Tasks**: F001-F006
**Outcome**: Reusable component library + API client ready
```
Day 1-2: Create ProductCard, ProductGrid, PriceDisplay components
Day 2-3: API utilities + TypeScript types
Day 3-4: Visual testing (Chromatic), Storybook stories (optional)
```

### Team B: Catalog Pages (Start after Team A finishes + T056-T058)
**Duration**: 3-4 days
**Tasks**: F007-F011
**Blocks on**: Team A (F001-F006) + T056-T058 passing
```
Day 1: Product listing + search page
Day 2: Category detail page
Day 3: Product detail page + availability
Day 4: Filter panel + visual polish
```

### Team C: Cart Integration (Start when T061+ ready)
**Duration**: 2-3 days
**Tasks**: F012-F014
**Blocks on**: T061+ (Cart backend)
```
Day 1: Add-to-cart button integration
Day 2: Cart utilities + error handling
Day 3: Testing + toast notifications
```

### Team D: Polish (Start after F012-F014)
**Duration**: 2-3 days
**Tasks**: F015-F020
**Blocks on**: F012-F014 complete
```
Day 1-2: Performance optimization, image optimization
Day 2-3: i18n setup + localization
Day 3: SEO audit + accessibility fixes
```

---

## Git Workflow

### Version Tags by Milestone

```bash
# Catalog Backend
v0.1.0-catalog-services   (T034-T058) ✅ CURRENT
v0.2.0-catalog-complete   (T059-T060) - after contract/integration tests

# Catalog Frontend
v0.1.1-catalog-components (F001-F006) - component library
v0.1.2-catalog-pages      (F007-F011) - product browsing
v0.2.0-catalog-cart       (F012-F014) - add-to-cart flow
v0.2.1-catalog-polish     (F015-F020) - perf, SEO, i18n
```

### Branching Strategy

```
main (stable)
  ├─ feature/backend-catalog ─── v0.1.0 ✅ merged
  ├─ feature/frontend-components (in progress)
  └─ feature/frontend-catalog-pages (waiting on T056-T058)
```

---

## API Contract (What Frontend Expects)

### Required Endpoints (already implemented ✅)

```
GET  /api/v1/categories
GET  /api/v1/categories/{id}
GET  /api/v1/products?query=&categoryId=&page=
GET  /api/v1/products/{id}
GET  /api/v1/products/{id}/availability
GET  /api/v1/inventory/{variantId}
```

### Future Endpoints (blocked on backend T061+)

```
POST /api/v1/cart/items          (Cart module)
PUT  /api/v1/cart/items/{id}     (Cart module)
DELETE /api/v1/cart/items/{id}   (Cart module)
GET  /api/v1/orders              (Orders module)
POST /api/v1/orders              (Orders module)
```

---

## Success Criteria

### Frontend Phase 1 (F001-F006)
- [ ] ProductCard renders correctly
- [ ] API fetch utilities have error handling
- [ ] TypeScript types cover all backend DTOs
- [ ] Components are responsive (mobile-first)

### Frontend Phase 2 (F007-F011)
- [ ] `/catalog` page loads products from API
- [ ] Search + filters work end-to-end
- [ ] Product detail page shows variants + availability
- [ ] Lighthouse score ≥ 85 (Performance)

### Frontend Phase 3 (F012-F014)
- [ ] Add-to-cart works with real cart backend
- [ ] Cart quantity updates optimistically
- [ ] Toast notifications show success/error
- [ ] Out-of-stock items disable the button

### Frontend Phase 4 (F015-F020)
- [ ] Core Web Vitals pass (LCP < 2.5s, CLS < 0.1)
- [ ] WCAG 2.1 AA accessibility audit passes
- [ ] Multi-language support works (ES/EN)
- [ ] Images lazy-load with LQIP placeholders

---

## Risk Mitigation

**Risk**: Backend APIs change after frontend ships
**Mitigation**: 
- Contract tests (T059) ensure API stability
- OpenAPI spec generated from code
- Semantic versioning for breaking changes

**Risk**: Cart backend delays frontend
**Mitigation**:
- Mock cart API locally for F012-F014 development
- Frontend can ship without cart, add later
- Use feature flags to toggle cart UI

**Risk**: Performance regression from adding real data
**Mitigation**:
- Profile with real backend data early (F007-F008)
- Use pagination (12 items/page)
- Lazy-load images + code splitting

---

## Communication Plan

| Phase | Dependency | Check-In |
|-------|---|---|
| Now | - | ✅ Catalog backend T056-T058 passing |
| +1 week | F001-F006 ready | Frontend team can start F007-F011 |
| +2 weeks | F007-F011 deployed | Can demo product browsing |
| +3 weeks | T061+ (Cart backend) ready | F012-F014 can start |
| +4 weeks | F012-F014 deployed | Full e-commerce flow demo |

---

## Next Actions

1. ✅ **Backend**: Commit T056-T058 (unit tests) & create v0.1.0 tag
2. ✅ **Frontend**: Start F001-F006 component library (no backend needed)
3. 🔄 **Backend**: Complete T059-T060 (contract/integration tests) → v0.2.0
4. 🔄 **Frontend**: Start F007-F011 pages (ready when T056-T058 stable)
5. 🔒 **Backend**: Start T061+ (Cart module)
6. 🔒 **Frontend**: Start F012-F014 (when T061+ endpoints ready)
