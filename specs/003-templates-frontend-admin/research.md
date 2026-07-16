# Research: Templates Frontend Rendering & Admin Editor

## Decision: Reuse existing backend Templates contract as-is

**Decision**: Consume `specs/002-backend-ecommerce/contracts/templates-contract.md` verbatim; no new backend endpoints, no changes requested to that contract.

**Rationale**: The contract already covers everything both `front` (public `GET /api/templates/{slug}`, `GET /api/templates/preview/{template_id}`) and `admin` (full CRUD, blocks, meta, publish/unpublish/versions/revert, entity mapping) need. Introducing a second contract or altering the existing one would violate the constitution's API-First Contracts principle for no benefit.

**Alternatives considered**: Defining a frontend-specific BFF (backend-for-frontend) layer to reshape responses — rejected as unnecessary complexity; the existing response shapes are already frontend-friendly (flat JSON, block arrays in order).

---

## Decision: Server-rendered Astro pages with islands only for the admin block editor

**Decision**: `front` template rendering is 100% server-rendered `.astro` (no client JS needed to display blocks). `admin`'s block editor uses two small interactive islands (`BlockEditor.tsx` for add/reorder/hide, `BlockForm.tsx` for per-block-type content editing); everything else in `admin` (list, SEO form submission, version history) uses standard Astro form/page patterns already used elsewhere in the app.

**Rationale**: Matches the existing `front`/`admin` architecture (Astro 4.x, React islands optional per `tasks-frontend.md`). Public template pages benefit from SSR for SEO and fast first paint — no interactivity is required to view a landing/category/product page. The admin editor's reordering and live-editing UX is the one place that genuinely needs client-side state, so it's isolated rather than making the whole editor a SPA.

**Alternatives considered**:
- Full SPA admin editor (e.g., a single large React app mounted in Astro) — rejected: heavier bundle, inconsistent with the rest of `admin`'s server-rendered shell (`MainLayout.astro`, `Sidebar.astro`).
- Fully non-interactive (page-reload-per-change) block editor — rejected: reordering and content editing would feel unacceptably slow (spec SC-002 targets under-10-minutes template authoring).

---

## Decision: One Astro component per template block type, shared "shape" between front and admin preview

**Decision**: Create one renderer component per block type (HERO, PRODUCT_GRID, CATEGORY_LIST, FEATURED_PRODUCTS, RICH_TEXT, IMAGE_BANNER, TESTIMONIALS, CTA, HEADER, FOOTER) in `front/src/components/templates/`. The `admin` preview reuses the same rendering logic (either by importing from a shared location or by duplicating the same block→markup mapping, consistent with the current no-shared-package convention between `front` and `admin`).

**Rationale**: The contract defines exactly these 10 block content shapes; a 1:1 component mapping keeps rendering logic simple and independently testable per block type, and matches FR-002's requirement to support every documented block type.

**Alternatives considered**: A single generic "Block" component with a big switch statement — rejected: harder to test in isolation, harder to keep each block type's markup/accessibility concerns separate.

---

## Decision: Fallback-to-default-layout strategy for unmapped/unpublished entities

**Decision**: `front` pages (`catalog/[slug].astro`, `product/[id].astro`) first attempt `GET /api/templates/{slug}` (public endpoint, only returns published templates); on 404 or "no mapping" response, render the existing default layout/content for that entity type unchanged. The new `landing/[slug].astro` route is the one exception: since landing pages have no pre-existing default layout, a 404 response there renders a standard "page not found" state instead of a fallback layout.

**Rationale**: Directly satisfies FR-004 and Edge Case "no template mapped → fallback, not error" for categories/products. Landing pages are net-new routes with no prior content to fall back to, so "not found" is the only meaningful non-error response. Since the public endpoint per the contract only ever returns published content, no extra draft-filtering logic is needed on the frontend.

**Alternatives considered**: Always requiring a template (no fallback) — rejected, spec explicitly calls out fallback behavior as a functional requirement and edge case. Designing a generic default landing-page layout so FR-004 has zero exceptions — rejected as unnecessary added scope; a 404 is the standard, expected behavior for a URL with no corresponding published content anywhere on the web.

---

## Decision: Entity-to-template lookup via slug/ID convention (no new backend contract)

**Decision**: `front` resolves a category/product's mapped template by calling `fetchTemplateBySlug` with that entity's own existing routing slug/ID (category slug or product ID) — relying on a documented convention that a template mapped to an entity must share that entity's slug/ID as its own `slug` field. No Catalog contract change and no new Templates endpoint are introduced.

**Rationale**: The public Templates endpoint is keyed by `template_slug`; the only entity→template reverse lookup in the contract (`GET /api/v1/templates/{template_id}/entities`) is admin-only and requires already knowing the `template_id`, so it cannot be used by the public storefront. Extending Catalog's category/product responses with a `templateSlug` field, or adding a new public Templates lookup endpoint, would both require a backend contract change outside this feature's stated constraint ("no new backend contract or endpoint"). The slug/ID convention achieves the same outcome with zero backend changes, at the cost of requiring admin discipline when mapping templates to entities.

**Alternatives considered**:
- Extend Catalog's entity detail response with a `templateSlug` field — rejected for this release: requires a Catalog module contract amendment and backend work outside this feature's scope.
- Add a new public Templates endpoint for entity-based lookup (e.g., `GET /api/templates/by-entity/{type}/{id}`) — rejected for this release: also a new backend contract surface, and duplicates information already expressible via the slug convention.

---

## Decision: Graceful degradation for blocks referencing deleted catalog data

**Decision**: Block renderer components (`ProductGridBlock.astro`, `FeaturedProductsBlock.astro`, `CategoryListBlock.astro`) filter out referenced product/category IDs that the catalog API reports as not found, rendering only the items that still resolve; if zero items resolve, the block renders an empty/placeholder state rather than throwing.

**Rationale**: Satisfies FR-006 and SC-006 (0% hard failures from stale references). This is a frontend-only safeguard — the backend contract already validates references at write-time (`INVALID_BLOCK_CONTENT` error), but catalog items can still be deleted after a block was saved.

**Alternatives considered**: Failing the whole page render on any unresolved reference — rejected, directly violates the "no error page" edge case in the spec.

---

## Decision: Drag-and-drop with ordinal-control fallback for block reordering

**Decision**: Implement reordering in `BlockEditor.tsx` using a lightweight drag-and-drop approach; if implementation cost/complexity proves too high during `/speckit-tasks` breakdown, fall back to explicit "move up"/"move down" buttons per block, which map to the same `POST /api/v1/templates/{id}/blocks/reorder` call.

**Rationale**: Spec Assumptions explicitly allow this fallback. Both approaches use the same backend reorder endpoint, so the decision only affects `admin` UI implementation, not the contract or data model.

**Alternatives considered**: No reordering UI (blocks always append) — rejected, spec FR-009 and User Story 2 acceptance scenario 2 require reordering.

---

## Decision: Last-write-wins concurrency handling for concurrent template edits

**Decision**: No optimistic locking / merge UI in this release. When saving, if the backend returns a conflict/stale-version error (or simply overwrites), the admin UI shows a non-blocking warning banner suggesting the user reload to see the latest state before continuing.

**Rationale**: Spec Assumptions explicitly scope out real-time collaborative editing; the edge case only requires that a later save "should not silently overwrite... without warning" — a warning banner satisfies this without needing operational-transform or locking infrastructure.

**Alternatives considered**: Pessimistic locking (lock template while one admin edits) — rejected as out of scope per spec Assumptions and requires new backend behavior not in the existing contract.

---

## Outstanding NEEDS CLARIFICATION

None. All Technical Context fields were resolved using the existing `front`/`admin` stack (Astro 4.x/TypeScript/Vitest/Playwright) and the existing backend contract; no unresolved unknowns remain.
