# Contract: `front` Templates API Client

**Location**: `front/src/lib/api.ts` (extends the existing catalog client with template functions)
**Backing REST contract**: `specs/002-backend-ecommerce/contracts/templates-contract.md` — Template Preview & Public APIs section

This is a frontend consumption contract, not a new REST contract. It documents the function signatures `front` pages call and the response shapes they receive, so implementation and tests stay aligned with the backend without needing to change the backend contract.

## `fetchTemplateBySlug(slug: string): Promise<Template | null>`

- Calls `GET /api/templates/{template_slug}`.
- Returns the parsed `Template` (see `data-model.md`) on `200 OK`.
- Returns `null` on `404 Not Found` (no published template at that slug) — callers MUST treat `null` as "use fallback layout," never as an error.
- Any other non-2xx status (5xx, network failure) throws, to be caught by the calling page and rendered as a generic error state (distinct from the "no template" fallback).

## `fetchTemplatePreview(templateId: string, version?: number): Promise<Template>`

- Calls `GET /api/templates/preview/{template_id}` (optionally with `?version=`).
- Requires the caller to have forwarded an authenticated admin session/token (used only if `front` ever needs to render an admin-triggered preview page; primarily used by `admin`'s `TemplatePreview.astro`, but the function lives in the shared block-rendering path so both apps can call it consistently).
- Throws on `401`/`403` — caller must redirect to login or show an access-denied state, never fall back silently to the public template.

## Consumption rules for page components

- `catalog/[slug].astro`, `product/[id].astro`, and the new `landing/[slug].astro` MUST call `fetchTemplateBySlug` (or an entity-to-slug/id resolution step, if the entity's template is looked up by entity ID rather than slug — see Open Question below) before falling back to each page's existing default rendering logic.
- Block rendering components (one per `blockType`) MUST accept a `content` prop typed per `data-model.md`'s `BlockContent` variants and MUST NOT throw if a referenced product/category ID does not resolve via the catalog API — they render only the resolvable items (or an empty state if none resolve).
- `TemplateMeta` fields MUST be mapped onto the page's `<head>` (title, meta description, Open Graph tags, canonical link, robots directive, JSON-LD script tag for `structuredDataJson`).

## Entity-to-template resolution (resolved)

The contract's public endpoint is keyed by `template_slug`, while `PageContentMapping` links a template to an `entityId`+`entityType`. For category/product pages, the frontend has an entity slug/ID (from the URL) but needs the *template's* slug, and the only reverse-lookup endpoint (`GET /api/v1/templates/{template_id}/entities`) is admin-only per contract — unusable by `front`.

**Resolved approach**: no new backend contract or endpoint. `front` calls `fetchTemplateBySlug(categorySlug)` / `fetchTemplateBySlug(productId)` directly, using the entity's own existing routing slug/ID as the template lookup key. This relies on a documented convention (see `spec.md` Assumptions): admins mapping a template to a category or product MUST set the template's `slug` field equal to that entity's own slug/ID. No entity-resolution helper or extra Catalog field is required — `catalog/[slug].astro` and `product/[id].astro` pass their existing route param straight into `fetchTemplateBySlug`.
