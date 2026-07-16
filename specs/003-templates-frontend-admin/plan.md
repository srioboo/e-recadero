# Implementation Plan: Templates Frontend Rendering & Admin Editor

**Branch**: `003-templates-frontend-admin` | **Date**: 2026-07-14 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-templates-frontend-admin/spec.md`

## Summary

Add the missing frontend layer for the backend Templates module: (1) in `front`, server-rendered pages that fetch a published template by slug and render its ordered, visible blocks with SEO metadata, falling back to a default layout when no template is mapped; (2) in `admin`, a template management UI — list/filter, create, a block editor (add/edit/reorder/hide blocks) with live preview, SEO metadata form, publish/unpublish/version-history/revert actions, and entity-mapping management. Both apps consume the existing backend contract in `specs/002-backend-ecommerce/contracts/templates-contract.md` — no new backend contract is introduced by this feature.

## Technical Context

**Language/Version**: TypeScript 5.x on Astro 4.x (Node.js 18+), matching `front` and `admin`'s existing setup
**Primary Dependencies**: Astro 4.x, `@astrojs/node`; no new runtime dependency required for rendering; admin block editor uses a React (or Preact) island only for the interactive parts (drag-and-drop reorder, live-updating block forms) — everything else stays server-rendered `.astro`
**Storage**: N/A — both apps are stateless consumers of the backend Templates REST API; no local database or file storage
**Testing**: Vitest (unit/component), Playwright (`test:e2e`) — same as existing `front`/`admin` setup; contract-shape tests for the API client layer using mocked fetch responses
**Target Platform**: Server-rendered web (Node adapter), served behind the existing `front` (:3001) and `admin` (:3000) Astro apps
**Project Type**: Web application — two independent frontend apps (`front`, `admin`) consuming a separate backend (`back`) over REST
**Performance Goals**: Public template pages render within the backend's stated SLA (`GET /api/templates/{slug}` < 100ms cached) plus normal Astro SSR overhead; admin editor interactions (add/reorder/save block) feel immediate (< 300ms perceived latency for local state changes, network round-trip bound by backend SLAs otherwise)
**Constraints**: No new backend contract or endpoint may be introduced — only endpoints already defined in `templates-contract.md` are consumed; admin editor must degrade to non-drag-and-drop ordinal controls if drag-and-drop is descoped (see spec Assumptions); public rendering must never hard-fail when a block references deleted catalog data
**Scale/Scope**: Two Astro apps, ~10 supported block types, template CRUD + versioning + entity mapping UI in `admin`, template rendering + fallback in `front`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution (`.specify/memory/constitution.md`) governs the **backend** (API-first contracts, Modulith boundaries, Java/Spring stack). This feature is purely a frontend consumer of an already-approved contract (`templates-contract.md`) and introduces no backend code, no new endpoints, and no schema changes.

- **I. API-First Contracts**: Satisfied by inheritance — this feature implements against the existing, already-contracted Templates API and does not modify or bypass it. No new contract is authored as part of this plan.
- **II. Contract-Driven Testing**: N/A for backend contract tests (no backend change). Frontend equivalent: API client functions are tested against the documented request/response shapes from `templates-contract.md` (see Phase 1 `contracts/`).
- **III. Modulith Boundaries**: N/A — no backend module code touched.
- **IV. Versioned and Observable APIs**: Satisfied — this feature consumes the existing versioned `/api/v1/templates` and public `/api/templates/{slug}` paths as-is; no version bump needed since no breaking change is introduced.
- **V. Simplicity and Security**: Admin template/block endpoints require ADMIN auth (already enforced backend-side); the frontend must not duplicate authorization logic beyond passing the existing session/token and handling 401/403 responses. Editor keeps the smallest implementation that satisfies the spec (ordinal reorder is an acceptable fallback to drag-and-drop, per spec Assumptions).

**Result**: PASS — no violations, no Complexity Tracking entries needed.

## Project Structure

### Documentation (this feature)

```text
specs/003-templates-frontend-admin/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command) — frontend API client contracts, referencing the existing backend contract
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

This is a **web application** with two existing, independent Astro frontend apps (`front`, `admin`) consuming a separate Spring Boot backend (`back`, out of scope for this feature). Work is split across the two frontend apps:

```text
front/
├── src/
│   ├── lib/
│   │   ├── api.ts                      # extend: fetchTemplateBySlug(), fetchEntityTemplate()
│   │   └── templates.ts                # new: block-type renderers helpers, fallback resolution
│   ├── types/
│   │   └── templates.ts                # new: Template, TemplateBlock, TemplateMeta types
│   ├── components/
│   │   └── templates/                  # new: one Astro component per block type
│   │       ├── HeroBlock.astro
│   │       ├── ProductGridBlock.astro
│   │       ├── CategoryListBlock.astro
│   │       ├── FeaturedProductsBlock.astro
│   │       ├── RichTextBlock.astro
│   │       ├── ImageBannerBlock.astro
│   │       ├── TestimonialsBlock.astro
│   │       ├── CtaBlock.astro
│   │       ├── HeaderBlock.astro
│   │       └── FooterBlock.astro
│   └── pages/
│       ├── landing/[slug].astro        # new: landing pages rendered from templates
│       ├── catalog/[slug].astro        # existing category page: add template-aware rendering + fallback
│       └── product/[id].astro          # existing product page: add template-aware rendering + fallback
└── tests/
    ├── unit/templates.test.ts
    └── e2e/template-rendering.spec.ts

admin/
├── src/
│   ├── lib/
│   │   ├── api.ts                       # new: templates API client (list/get/create/update, blocks CRUD/reorder, meta, publish/unpublish/versions/revert, map-entity)
│   │   └── types.ts                     # extend: Template, TemplateBlock, TemplateVersion, PageContentMapping types
│   ├── components/
│   │   └── templates/
│   │       ├── TemplateList.astro       # new: filterable/paginated list
│   │       ├── TemplateEditor.astro     # new: shell page composing block list + SEO form + preview
│   │       ├── BlockEditor.tsx          # new: interactive island (add/edit/reorder/hide blocks)
│   │       ├── BlockForm.tsx            # new: per-block-type content form (island)
│   │       ├── SeoMetaForm.astro        # new: SEO/social metadata fields
│   │       ├── TemplatePreview.astro    # new: renders current draft using front's block components (or a shared preview renderer)
│   │       ├── VersionHistory.astro     # new: list versions + revert action
│   │       └── EntityMappingPanel.astro # new: assign/remove entity mapping
│   └── pages/
│       └── templates/
│           ├── index.astro              # new: template list page
│           ├── new.astro                # new: create template form
│           └── [templateId]/
│               ├── edit.astro           # new: block editor + SEO + mapping
│               └── versions.astro       # new: version history + revert
└── tests/
    ├── unit/templates-api.test.ts
    └── e2e/template-editor.spec.ts
```

**Structure Decision**: Reuses the existing two-app split (`front` for public rendering, `admin` for authoring) rather than introducing a shared package, consistent with `CLAUDE.md`'s note that `front`/`admin` currently duplicate logic in their own `src/lib/`. Block-type rendering components are added under `front/src/components/templates/`; the interactive parts of the admin block editor (reordering, per-block forms) are isolated into small React/Preact islands (`BlockEditor.tsx`, `BlockForm.tsx`) while list/shell/preview pages stay server-rendered `.astro`, minimizing client-side JavaScript.

## Complexity Tracking

*No Constitution Check violations — this section is intentionally empty.*
