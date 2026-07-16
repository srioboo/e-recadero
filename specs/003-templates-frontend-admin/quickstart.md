# Quickstart: Templates Frontend Rendering & Admin Editor

## Prerequisites

- Backend Templates module (`specs/002-backend-ecommerce/tasks.md` Phase 8, T0xx) implemented and running, exposing the endpoints documented in `specs/002-backend-ecommerce/contracts/templates-contract.md`.
- `back` running locally (`./gradlew bootRun`, `localhost:8080/api/v1`) with PostgreSQL + Redis up (`back/compose.yaml`).
- At least one seeded/created template, published and mapped to a category or product, to verify the `front` fallback-vs-rendered behavior end to end.
- Note the entity→template lookup convention: for category/product templates, the template's `slug` MUST equal that category's slug or that product's ID (see `spec.md` Assumptions and `contracts/front-api-client.md`). Landing page templates use their own arbitrary slug as the URL path segment.

## Running the apps

```bash
# Backend
cd back && ./gradlew bootRun

# Public storefront (consumes published templates)
cd front && npm run dev     # http://localhost:3001

# Admin dashboard (template authoring)
cd admin && npm run dev     # http://localhost:3000
```

## Manual verification flow

Start a stopwatch before step 1 and stop it after step 5 to validate **SC-002** (create + arrange + publish a 3-block template in under 10 minutes).

1. **Admin: create a template** — open `admin` → Templates → New, choose type `CATEGORY_PAGE`, and set the slug to **exactly match** an existing category's own slug (per the lookup convention above), save. Confirm it appears in the template list as `DRAFT`.
2. **Admin: add blocks** — open the new template's editor, add a `HERO` block and a `PRODUCT_GRID` block (referencing real product IDs from the Catalog module), reorder them, and toggle one block's visibility off then on. Confirm the in-editor preview reflects each change.
3. **Admin: set SEO metadata** — fill the SEO form (page title, description, OG image) and save.
4. **Admin: confirm entity mapping** — assign the template to that same category via the entity mapping panel. Confirm the mapping appears under "entities using this template." (This registers the mapping for admin bookkeeping/reporting; `front`'s actual lookup uses the slug convention from step 1, not this mapping record.)
5. **Admin: publish** — publish the template with a change note. Confirm status flips to `PUBLISHED`. Stop the stopwatch here (SC-002 check).
6. **Front: verify rendering** — visit that category's page in `front`. Confirm the hero and product grid render in the correct order, the hidden-then-shown block is visible, and the page `<head>` contains the configured SEO metadata.
7. **Front: verify fallback** — visit a different category with no template mapped. Confirm the existing default category layout renders (no error, no blank page).
8. **Front: verify landing-page 404** — visit `/landing/some-unpublished-slug` for a slug with no published template. Confirm a standard "page not found" state renders (not the category/product fallback layout, since landing pages have no pre-existing default content).
9. **Admin: unpublish** — unpublish the template. Reload the `front` category page and confirm it now shows the fallback layout again.
10. **Admin: version history & revert** — make another change and republish, then open version history and revert to the first version. Confirm a new draft is created (version number increments) rather than the live page changing immediately.
11. **Degradation check** — delete (or otherwise make unavailable) one of the products referenced in the `PRODUCT_GRID` block via the Catalog module, republish, and confirm the `front` page still renders (omitting only the missing product) instead of failing.
12. **Empty-template publish warning** — create a second template with zero blocks and attempt to publish it. Confirm the admin UI shows a confirmation warning before proceeding (Edge Case: publishing with no blocks).
13. **Unauthorized preview check** — attempt to open a template's preview URL from a non-admin/unauthenticated session. Confirm access is denied (redirect to login or an access-denied state), not a silent fallback to the public template.
14. **Locate-template timing (SC-004)** — with several templates created, time how long it takes to find a specific one using the list's type/status filters; confirm it takes under 15 seconds.

## Test commands

```bash
# front
cd front && npm run check && npm test && npx vitest run tests/unit/templates.test.ts
cd front && npm run test:e2e -- template-rendering

# admin
cd admin && npm run check && npm test && npx vitest run tests/unit/templates-api.test.ts
cd admin && npm run test:e2e -- template-editor
```
