---

description: "Task list for Templates Frontend Rendering & Admin Editor"
---

# Tasks: Templates Frontend Rendering & Admin Editor

**Input**: Design documents from `/specs/003-templates-frontend-admin/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md (all present)

**Tests**: Not explicitly requested in spec.md; dedicated test tasks are limited to Polish phase (unit/e2e), consistent with `specs/001-migrate-svelte-astro/tasks-frontend.md` convention.

**Organization**: Tasks are grouped by user story (US1-US4, per spec.md priorities P1-P4) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)
- Paths are relative to repository root (`front/`, `admin/`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Directory scaffolding and shared type definitions for both apps

- [X] T001 Create directory scaffolding: `front/src/components/templates/`, `front/src/pages/landing/`, `admin/src/pages/templates/`, `admin/src/components/templates/`
- [X] T002 [P] Create `front/src/types/templates.ts` with `Template`, `TemplateBlock`, `TemplateMeta`, `BlockType`, and all 10 `BlockContent` variants per `data-model.md`
- [X] T003 [P] Extend `admin/src/lib/types.ts` with `Template`, `TemplateBlock`, `TemplateMeta`, `TemplateVersion`, `PageContentMapping`, `BlockType`, and all 10 `BlockContent` variants per `data-model.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core API clients and tooling that user stories depend on

**⚠️ CRITICAL**: No user story work can begin until its own prerequisites below are complete

### Core (blocks ALL user stories, including US1/MVP)

- [X] T004 [P] Implement `fetchTemplateBySlug(slug)` and `fetchTemplatePreview(templateId, version?)` in `front/src/lib/api.ts` per `contracts/front-api-client.md` (404 → `null`, non-2xx → throw)

### Admin-specific (blocks US2, US3, US4 only — NOT required for US1/MVP)

- [X] T005 [P] Implement full templates API client in `admin/src/lib/api.ts` per `contracts/admin-api-client.md`: `listTemplates`, `createTemplate`, `getTemplate`, `updateTemplate`, `addBlock`, `updateBlock`, `setBlockVisibility`, `removeBlock`, `reorderBlocks`, `updateTemplateMeta`, `publishTemplate`, `unpublishTemplate`, `listTemplateVersions`, `getTemplateVersion`, `revertTemplate`, `previewTemplate`, `mapTemplateToEntity`, `getTemplateEntities`, `unmapTemplateEntity`
- [X] T006 [P] Implement typed error classes `DuplicateSlugError` (409/`DUPLICATE_SLUG`) and `InvalidBlockContentError` (400/`INVALID_BLOCK_CONTENT`, carrying per-field `details.errors[]`) in `admin/src/lib/api.ts`, thrown by `createTemplate`/`updateTemplate` and `addBlock`/`updateBlock` respectively
- [X] T007 Add React (or Preact) island support to `admin`: install `@astrojs/react` (or `@astrojs/preact`), register the integration in `admin/astro.config.mjs`, and add the corresponding dependency to `admin/package.json` — required for the interactive block editor islands in US2

**Checkpoint**: Core foundation ready — US1 can begin immediately after T004; US2-US4 can begin once T005-T007 are also complete

---

## Phase 3: User Story 1 - Visitor sees a page built from a published template (Priority: P1) 🎯 MVP

**Goal**: Storefront pages render published templates' blocks in order with SEO metadata, and fall back to the default layout when no template applies.

**Independent Test**: Publish one template mapped to a known slug/entity (via direct API calls or a temporary admin script), visit its public URL, and confirm all blocks render correctly in order with correct SEO meta tags; visit an unmapped entity and confirm the existing default layout still renders.

### Implementation for User Story 1

- [X] T008 [P] [US1] Create `front/src/components/templates/HeroBlock.astro` rendering `HeroContent` (title, subtitle, background image, CTA)
- [X] T009 [P] [US1] Create `front/src/components/templates/ProductGridBlock.astro` rendering `ProductGridContent`, resolving each product ID via the existing catalog API and silently omitting any that don't resolve (FR-006)
- [X] T010 [P] [US1] Create `front/src/components/templates/CategoryListBlock.astro` rendering `CategoryListContent` with the same graceful-omission behavior for missing category IDs
- [X] T011 [P] [US1] Create `front/src/components/templates/FeaturedProductsBlock.astro` rendering `FeaturedProductsContent` (carousel/grid layout) with graceful omission for missing product IDs
- [X] T012 [P] [US1] Create `front/src/components/templates/RichTextBlock.astro` rendering `RichTextContent` (sanitized HTML, alignment, background color)
- [X] T013 [P] [US1] Create `front/src/components/templates/ImageBannerBlock.astro` rendering `ImageBannerContent` (image, alt text, link, overlay)
- [X] T014 [P] [US1] Create `front/src/components/templates/TestimonialsBlock.astro` rendering `TestimonialsContent` (carousel/grid of author/content/rating)
- [X] T015 [P] [US1] Create `front/src/components/templates/CtaBlock.astro` rendering `CtaContent` (text, link, button style/size)
- [X] T016 [P] [US1] Create `front/src/components/templates/HeaderBlock.astro` rendering `HeaderContent` (logo, navigation links)
- [X] T017 [P] [US1] Create `front/src/components/templates/FooterBlock.astro` rendering `FooterContent` (company info, links, social links)
- [X] T018 [US1] Create `front/src/components/templates/TemplateRenderer.astro` that iterates a `Template`'s blocks in `blockOrder`, skips blocks where `isVisible` is false, dispatches each visible block to its matching component (T008-T017), and injects `TemplateMeta` fields into the page `<head>` (title, description, OG tags, canonical, robots, JSON-LD) — depends on T008-T017
- [X] T019 [US1] Create `front/src/lib/templates.ts` with the shared "resolve or omit" catalog-reference helper used by T009-T011 (looks up a product/category ID via the existing catalog API and returns `null` if not found, rather than throwing)
- [X] T020 [US1] Update `front/src/pages/catalog/[slug].astro` (keep existing `export const prerender = false`) to call `fetchTemplateBySlug(slug)` (T004) using the category's own route `slug` directly — per the slug/ID convention in `spec.md` Assumptions, no separate entity-resolution step is needed — render via `TemplateRenderer` (T018) when a template exists, and keep the existing category layout as the fallback when it returns `null`
- [X] T021 [US1] Update `front/src/pages/product/[id].astro` (keep existing `export const prerender = false`) with the same pattern as T020, calling `fetchTemplateBySlug(id)` using the product's own route `id` directly
- [X] T022 [US1] Create `front/src/pages/landing/[slug].astro` with `export const prerender = false` — a new route type for `LANDING_PAGE` templates; render via `TemplateRenderer` (T018) when `fetchTemplateBySlug(slug)` (T004) resolves, otherwise render a standard "page not found" (404) state per `spec.md` FR-004/Assumptions (landing pages have no pre-existing default layout to fall back to)

**Checkpoint**: At this point, User Story 1 is fully functional and testable independently — published templates render on the storefront, unmapped/unpublished entities show the existing fallback.

---

## Phase 4: User Story 2 - Admin builds and edits a template's content (Priority: P2)

**Goal**: Admins can create a template, add/edit/reorder/hide blocks, and preview the result before saving further changes.

**Independent Test**: Create a new template, add three blocks of different types, edit their content, reorder them, hide one, and confirm the saved template reflects all changes when reloaded via `getTemplate`.

### Implementation for User Story 2

- [X] T023 [US2] Create `admin/src/pages/templates/new.astro` — create-template form (name, type, slug) calling `createTemplate` (T005), redirecting to the new template's edit page on success
- [X] T024 [US2] Implement slug-conflict handling in `admin/src/pages/templates/new.astro`: catch `DuplicateSlugError` (T006) and show an inline error on the slug field
- [X] T025 [US2] Create `admin/src/pages/templates/[templateId]/edit.astro` — shell page fetching the template via `getTemplate` (T005) and composing the block editor, SEO form, and preview panels
- [X] T026 [US2] Create `admin/src/components/templates/BlockEditor.tsx` (React/Preact island, requires T007) managing the current block list: add block (type + name), remove block, toggle visibility (`setBlockVisibility`), and reorder via drag-and-drop calling `reorderBlocks` (T005), with explicit move-up/move-down buttons as the accessible/fallback interaction per spec Assumptions
- [X] T027 [US2] Create `admin/src/components/templates/BlockForm.tsx` (React/Preact island) — per-`blockType` content form (one field set per `BlockContent` variant from `data-model.md`), calling `addBlock`/`updateBlock` (T005) on save
- [X] T028 [US2] Implement block content validation error surfacing in `BlockForm.tsx`: catch `InvalidBlockContentError` (T006) and render its `details.errors[]` as field-level messages instead of a generic failure
- [X] T029 [US2] Create `admin/src/components/templates/TemplatePreview.astro` reusing the same block→markup mapping as `front`'s block components (T008-T017), keeping per-block-type output structurally equivalent to its `front` counterpart (verified by T048) to render the current in-progress draft (including unpublished changes) for the admin's live preview, calling `previewTemplate` (T005) when previewing a saved version; on a `401`/`403` response (Edge Case: preview requested without admin permissions), redirect to login or show an access-denied state instead of falling back to the public template
- [X] T030 [US2] Wire `edit.astro` (T025) to pass the fetched template's blocks into `BlockEditor` (T026), `BlockForm` (T027), and `TemplatePreview` (T029), keeping local state in sync after each save
- [X] T031 [US2] Handle concurrent-edit conflicts (Edge Case: two admins editing the same template): when `updateBlock`/`addBlock`/`updateTemplate` (T005) returns a conflict or stale-version response, show a non-blocking warning banner in `edit.astro`/`BlockEditor.tsx` advising the admin to reload before continuing, per the last-write-wins decision in `research.md`

**Checkpoint**: At this point, User Stories 1 AND 2 both work independently — templates can be authored end-to-end and rendered on the storefront once published (publishing itself lands in US3).

---

## Phase 5: User Story 3 - Admin manages the template list and lifecycle (Priority: P3)

**Goal**: Admins can browse/filter templates, publish/unpublish, and review/revert version history.

**Independent Test**: Create a template, publish it, confirm it becomes visible on the front end (via US1's rendering), unpublish it, confirm the front end reverts to the fallback, then revert to a prior version and confirm the reverted content is restored as a new draft.

### Implementation for User Story 3

- [X] T032 [P] [US3] Create `admin/src/components/templates/TemplateList.astro` — filterable (type/status/creator) paginated list component consuming `listTemplates` (T005)
- [X] T033 [US3] Create `admin/src/pages/templates/index.astro` using `TemplateList` (T032) as the template management landing page, linking to `new.astro` (T023) and each template's `edit.astro` (T025)
- [X] T034 [US3] Add publish/unpublish controls to `admin/src/pages/templates/[templateId]/edit.astro` (T025), calling `publishTemplate`/`unpublishTemplate` (T005) and reflecting the returned status immediately in the page state; if the template currently has zero blocks, show a non-blocking confirmation warning ("This template has no content blocks — publish anyway?") before calling `publishTemplate` (Edge Case: publishing an empty template)
- [X] T035 [US3] Create `admin/src/components/templates/VersionHistory.astro` listing a template's versions via `listTemplateVersions` (T005), with a "view version" action calling `getTemplateVersion`
- [X] T036 [US3] Create `admin/src/pages/templates/[templateId]/versions.astro` hosting `VersionHistory` (T035), linked from `edit.astro`
- [X] T037 [US3] Implement the revert action in `VersionHistory.astro` (T035): calling `revertTemplate` (T005) with a required change note, showing a confirmation before submitting, and redirecting to `edit.astro` afterward since revert creates a new draft rather than altering the live version in place

**Checkpoint**: At this point, User Stories 1, 2, AND 3 are all independently functional — templates can be found, authored, published, unpublished, and rolled back.

---

## Phase 6: User Story 4 - Admin configures SEO metadata and entity mapping (Priority: P4)

**Goal**: Admins can edit a template's SEO fields and control which entity (category/product/landing page) renders it.

**Independent Test**: Edit a template's SEO fields and save; assign the template to a category; confirm (via US1 rendering) the category's page now uses the template and reflects the SEO fields in its page head.

### Implementation for User Story 4

- [X] T038 [P] [US4] Create `admin/src/components/templates/SeoMetaForm.astro` — form for all `TemplateMeta` fields (page title/description, OG fields, canonical URL, robots directive, structured data JSON)
- [X] T039 [US4] Wire `SeoMetaForm` (T038) into `edit.astro` (T025), calling `updateTemplateMeta` (T005) on save and reflecting the response's `updatedAt` as a save confirmation
- [X] T040 [P] [US4] Create `admin/src/components/templates/EntityMappingPanel.astro` — shows current entities using this template via `getTemplateEntities` (T005), with a form to assign a new entity (`mapTemplateToEntity`) and a remove action per mapping (`unmapTemplateEntity`)
- [X] T041 [US4] Implement reassignment confirmation in `EntityMappingPanel.astro` (T040): before calling `mapTemplateToEntity` for an entity that already has a mapping elsewhere, show a confirmation dialog explaining the entity will be reassigned

**Checkpoint**: All four user stories are independently functional — the feature is complete per spec.md.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Test coverage, accessibility, and end-to-end validation across all stories

- [ ] T042 [P] Add unit tests for graceful catalog-reference degradation in `front/tests/unit/templates.test.ts` (covers T009-T011's "omit unresolved IDs" behavior)
- [ ] T043 [P] Add unit tests for the admin templates API client (error mapping for `DuplicateSlugError`/`InvalidBlockContentError`) in `admin/tests/unit/templates-api.test.ts`
- [ ] T044 [P] Add Playwright e2e test `front/tests/e2e/template-rendering.spec.ts` covering: published template renders, fallback renders for unmapped entity, hidden block excluded, and a 404 for an unpublished landing slug
- [ ] T045 [P] Add Playwright e2e test `admin/tests/e2e/template-editor.spec.ts` covering: create template → add/reorder/hide blocks → publish → revert
- [ ] T046 Accessibility pass on `BlockEditor.tsx`/`BlockForm.tsx` (T026, T027): ARIA labels on reorder/visibility controls, keyboard-operable move-up/down as alternative to drag-and-drop
- [ ] T047 Run the full manual verification flow in `quickstart.md` end-to-end and record any deviations, including timing the template-authoring flow against SC-002 (under 10 minutes) and the template-lookup flow against SC-004 (under 15 seconds)
- [ ] T048 [P] Add a preview/render parity test (unit or snapshot, e.g. `admin/tests/unit/template-preview-parity.test.ts`) that renders each of the 10 block types through both `admin`'s `TemplatePreview.astro` (T029) and `front`'s matching block component (T008-T017) with identical sample content, and asserts the resulting markup structure is equivalent (SC-003)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup (T002/T003 types). Only the Core sub-section (T004) blocks ALL user stories; the Admin-specific sub-section (T005-T007) blocks only US2-US4, not US1
- **User Story 1 (Phase 3)**: Depends on Foundational Core (T004) only — no dependency on T005-T007 or other stories
- **User Story 2 (Phase 4)**: Depends on Foundational (T005-T007) — reuses US1's block components (T008-T017) for preview (T029) but does not require US1's pages (T020-T022) to be done first
- **User Story 3 (Phase 5)**: Depends on Foundational (T005) and on US2's `edit.astro` shell (T025) existing to attach publish/version controls to
- **User Story 4 (Phase 6)**: Depends on Foundational (T005) and on US2's `edit.astro` shell (T025)
- **Polish (Phase 7)**: Depends on all desired user stories being complete (T042-T047)

### User Story Dependencies

- **US1 (P1)**: Independently testable once Foundational is done — this is the MVP slice
- **US2 (P2)**: Independently testable once Foundational is done; benefits from US1's block components existing (for preview) but can be built with placeholder preview markup if sequenced first
- **US3 (P3)**: Builds on US2's `edit.astro` page structurally, but is independently testable (publish/unpublish/revert can be verified via API responses even before US4 exists)
- **US4 (P4)**: Builds on US2's `edit.astro` page structurally, independently testable via API responses and, once US1 exists, via observing the rendered page's `<head>`

### Parallel Opportunities

- T002 and T003 (type definitions) in parallel
- T004, T005, T006 (API clients/errors) in parallel; T007 (React integration) can run alongside them
- T008-T017 (all 10 block components) fully in parallel — different files, no shared state
- T032 and T038/T040 (list component, SEO form, mapping panel) can run in parallel once their respective phases start
- T042-T045 and T048 (all Polish test tasks) in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all block components together (different files, no dependencies):
Task: "Create HeroBlock.astro in front/src/components/templates/HeroBlock.astro"
Task: "Create ProductGridBlock.astro in front/src/components/templates/ProductGridBlock.astro"
Task: "Create CategoryListBlock.astro in front/src/components/templates/CategoryListBlock.astro"
Task: "Create FeaturedProductsBlock.astro in front/src/components/templates/FeaturedProductsBlock.astro"
Task: "Create RichTextBlock.astro in front/src/components/templates/RichTextBlock.astro"
Task: "Create ImageBannerBlock.astro in front/src/components/templates/ImageBannerBlock.astro"
Task: "Create TestimonialsBlock.astro in front/src/components/templates/TestimonialsBlock.astro"
Task: "Create CtaBlock.astro in front/src/components/templates/CtaBlock.astro"
Task: "Create HeaderBlock.astro in front/src/components/templates/HeaderBlock.astro"
Task: "Create FooterBlock.astro in front/src/components/templates/FooterBlock.astro"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories; note T005-T007 are admin-only and can be deferred if the MVP is front-rendering-only, but T004 is required)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Publish a template directly via the backend API (curl/Postman) and confirm it renders correctly on `front` — the admin UI isn't needed yet to validate rendering
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. US1 → Test independently (templates render on storefront, given API-created templates) → Demo
3. US2 → Test independently (author templates end-to-end via UI) → Demo
4. US3 → Test independently (publish/unpublish/revert from the UI, observed via US1's rendering) → Demo
5. US4 → Test independently (SEO + mapping from the UI) → Demo (feature complete)

### Parallel Team Strategy

With multiple developers, after Foundational completes:

- Developer A: US1 (front block components + pages) — this is the critical path for demonstrable value
- Developer B: US2 (admin editor) — can start in parallel, using placeholder preview until US1's block components land
- Developer C: US3 + US4 (admin list/lifecycle/SEO/mapping) — can start on list/SEO/mapping forms in parallel, but publish/version controls need US2's `edit.astro` shell first

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- No new backend contract or endpoint is introduced — all tasks consume `specs/002-backend-ecommerce/contracts/templates-contract.md` as-is
- Commit after each task or logical group
- Stop at any checkpoint to validate a story independently before moving to the next
