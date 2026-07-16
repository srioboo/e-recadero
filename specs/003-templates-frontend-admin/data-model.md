# Data Model: Templates Frontend Rendering & Admin Editor

These are the **frontend-facing TypeScript types** consumed/produced by `front` and `admin`. They mirror the shapes already defined by the backend contract (`specs/002-backend-ecommerce/contracts/templates-contract.md`) — no new persistence or backend entities are introduced by this feature.

## Template

Represents a page template as returned by both admin and public endpoints.

| Field | Type | Notes |
|---|---|---|
| `templateId` | `string` (uuid) | |
| `name` | `string` | |
| `type` | `'LANDING_PAGE' \| 'CATEGORY_PAGE' \| 'PRODUCT_PAGE' \| 'CUSTOM'` | |
| `slug` | `string` | Unique; used for public routing (`front`) |
| `status` | `'DRAFT' \| 'PUBLISHED' \| 'ARCHIVED'` | Admin-only field; public endpoint only ever returns published content |
| `version` | `number` | Current draft version number |
| `publishedVersion` | `number \| null` | |
| `blocksCount` | `number` | Present on list responses only |
| `blocks` | `TemplateBlock[]` | Present on detail/public responses |
| `meta` | `TemplateMeta` | Present on detail/public responses |
| `createdBy` | `string` (uuid) | Admin-only |
| `createdAt` | `string` (ISO datetime) | |
| `publishedAt` | `string \| null` (ISO datetime) | |

**Validation rules** (enforced by backend, surfaced by frontend forms):
- `slug` must be unique — admin create/edit form must show a conflict error (`DUPLICATE_SLUG`) inline on the slug field.
- `type` is required and fixed at creation (not editable afterward, per contract's PUT payload never including `type`).

## TemplateBlock

| Field | Type | Notes |
|---|---|---|
| `blockId` | `string` (uuid) | |
| `templateId` | `string` (uuid) | |
| `blockType` | `BlockType` (see below) | |
| `blockName` | `string` | Admin-facing label |
| `blockOrder` | `number` | 1-based position |
| `isVisible` | `boolean` | |
| `content` | `BlockContent` (union, keyed by `blockType`) | |
| `createdAt` | `string` (ISO datetime) | |

**BlockType**: `'HERO' | 'PRODUCT_GRID' | 'CATEGORY_LIST' | 'FEATURED_PRODUCTS' | 'RICH_TEXT' | 'IMAGE_BANNER' | 'TESTIMONIALS' | 'CTA' | 'HEADER' | 'FOOTER'`

**BlockContent variants** (one interface per `blockType`, matching contract's documented shapes):
- `HeroContent`: `title`, `subtitle`, `backgroundImageUrl`, `ctaText`, `ctaLink`
- `ProductGridContent`: `products: string[]` (product IDs), `layout: '2-columns'|'3-columns'|'4-columns'`, `showPrices: boolean`, `showRatings: boolean`
- `CategoryListContent`: `categoryIds: string[]`, `itemsPerRow: number`, `showSubcategories: boolean`
- `FeaturedProductsContent`: `products: string[]`, `title: string`, `layout: 'carousel'|'grid'`
- `RichTextContent`: `htmlContent: string`, `textAlignment: 'left'|'center'|'right'`, `backgroundColor: string`
- `ImageBannerContent`: `imageUrl`, `altText`, `linkUrl`, `overlayColor`, `overlayOpacity: number`
- `TestimonialsContent`: `testimonials: { author: string; content: string; rating: number }[]`, `layout: 'carousel'|'grid'`
- `CtaContent`: `text`, `link`, `buttonStyle: 'primary'|'secondary'`, `buttonSize: 'small'|'medium'|'large'`
- `HeaderContent`: `logoUrl`, `navigationLinks: { label: string; link: string }[]`
- `FooterContent`: `companyInfo: string`, `links: { label: string; link: string }[]`, `socialLinks: { platform: string; url: string }[]`

**Validation rules**:
- `ProductGridContent.products` and `FeaturedProductsContent.products` entries must reference existing catalog products; backend rejects unknown IDs with `INVALID_BLOCK_CONTENT` — admin form surfaces this per-field.
- `CategoryListContent.categoryIds` entries must reference existing categories, same error handling.
- Frontend rendering must treat any ID not resolvable via the catalog API as **absent** (skip it) rather than erroring — this is a read-time safeguard independent of the write-time validation above (covers deletion-after-save).

## TemplateMeta

| Field | Type | Notes |
|---|---|---|
| `pageTitle` | `string` | |
| `pageDescription` | `string` | |
| `ogTitle` | `string` | |
| `ogDescription` | `string` | |
| `ogImageUrl` | `string` | |
| `keywords` | `string` | Comma-separated |
| `canonicalUrl` | `string` | |
| `robotsDirective` | `string` | e.g. `index,follow` |
| `structuredDataJson` | `Record<string, unknown>` | Raw JSON-LD object |

## TemplateVersion

| Field | Type | Notes |
|---|---|---|
| `versionId` | `string` (uuid) | |
| `templateId` | `string` (uuid) | |
| `versionNumber` | `number` | |
| `publishedAt` | `string` (ISO datetime) | |
| `createdBy` | `string` (uuid) | |
| `changeNote` | `string \| null` | |
| `blocks` | `TemplateBlock[]` | Only present when fetching a single version snapshot |
| `meta` | `TemplateMeta` | Only present when fetching a single version snapshot |

**State transitions** (admin-driven, backend-enforced, frontend must reflect):
```
DRAFT --publish--> PUBLISHED --unpublish--> DRAFT
PUBLISHED --revert(to older version)--> DRAFT (new version N+1 created from snapshot)
DRAFT/PUBLISHED --archive--> ARCHIVED   (not directly exposed by contract's documented endpoints; list filter only)
```

## PageContentMapping

| Field | Type | Notes |
|---|---|---|
| `pageContentId` | `string` (uuid) | |
| `templateId` | `string` (uuid) | |
| `entityId` | `string` (uuid) | |
| `entityType` | `'PRODUCT' \| 'CATEGORY' \| 'LANDING_PAGE' \| 'USER'` | |
| `entityName` | `string` | Present on list responses for admin display |
| `status` | `'DRAFT' \| 'PUBLISHED' \| 'ARCHIVED'` | Mapping status, independent of template status |
| `publishedAt` | `string \| null` (ISO datetime) | |

**Validation rules**:
- One entity may have at most one active mapping per the contract's `POST /map-entity` (reassignment replaces, does not duplicate) — admin UI must confirm before overwriting an existing mapping (User Story 4, acceptance scenario 2).

## Relationships

```
Template 1───* TemplateBlock        (ordered by blockOrder)
Template 1───1 TemplateMeta
Template 1───* TemplateVersion      (history, immutable snapshots)
Template 1───* PageContentMapping   (an entity's page rendering source)
PageContentMapping *───1 Entity     (Product | Category | LandingPage — owned by Catalog/other modules, referenced by ID only)
```

No frontend-local persistence is introduced; all of the above are transient client-side representations of API responses/requests.
