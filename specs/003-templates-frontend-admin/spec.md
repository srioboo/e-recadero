# Feature Specification: Templates Frontend Rendering & Admin Editor

**Feature Branch**: `003-templates-frontend-admin`
**Created**: 2026-07-14
**Status**: Draft
**Input**: User description: "Quiero implementar la parte de templates, pero no hay tareas de frontend para mostrar las plantillas ni para editarlas. Quiero crear las tareas adicionales para el módulo de templates, tanto en front como en admin"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Visitor sees a page built from a published template (Priority: P1)

A storefront visitor navigates to a landing page, category page, or product page whose content has been assembled from reusable content blocks (hero banners, product grids, rich text, testimonials, etc.). The page renders using the currently published version of the template, including its SEO metadata (title, description, social preview image).

**Why this priority**: This is the entire reason the Templates module exists — without frontend rendering, no template ever reaches a real customer. It delivers value the moment a single published template can be displayed.

**Independent Test**: Publish one template mapped to a known slug/entity, visit its public URL, and confirm all blocks render in the correct order with correct content and that the page's SEO meta tags match the template's configured metadata.

**Acceptance Scenarios**:

1. **Given** a template is published and mapped to a category, **When** a visitor opens that category's page, **Then** the page displays the template's blocks in their configured order with correct content and visibility.
2. **Given** a template has SEO metadata configured, **When** a visitor's browser loads the page, **Then** the page title, meta description, and social preview tags match the template's metadata.
3. **Given** a template block is marked hidden, **When** the page renders, **Then** that block does not appear on the page.
4. **Given** no template is mapped to a requested category or product, **When** a visitor opens that entity's page, **Then** the system falls back to that entity's existing default page layout instead of an error.
5. **Given** no published template exists at a requested landing page slug, **When** a visitor opens that URL, **Then** the system shows a standard "page not found" state (landing pages have no pre-existing default content to fall back to).

---

### User Story 2 - Admin builds and edits a template's content (Priority: P2)

An administrator opens the template editor, adds and arranges content blocks (hero, product grid, rich text, images, testimonials, etc.), edits each block's content, toggles block visibility, and reorders blocks — seeing an in-editor preview of the result before saving.

**Why this priority**: Without an editing UI, templates can only be built by calling the API directly, which defeats the purpose of giving non-technical staff control over page content. This is the second most valuable capability after rendering, since it is what produces the templates User Story 1 renders.

**Independent Test**: Create a new template, add three blocks of different types, edit their content, reorder them, hide one, and confirm the saved template reflects all changes when reloaded.

**Acceptance Scenarios**:

1. **Given** an admin is editing a draft template, **When** they add a new block and fill in its content fields, **Then** the block is saved and appears in the template's block list in the chosen position.
2. **Given** a template has multiple blocks, **When** the admin reorders them (e.g., drag-and-drop or move up/down controls), **Then** the new order is persisted and reflected on next load.
3. **Given** a block is visible, **When** the admin toggles it to hidden, **Then** the block is excluded from the live/public rendering but remains editable in the admin view.
4. **Given** an admin enters invalid block content (e.g., a product ID that does not exist), **When** they attempt to save, **Then** the editor shows a clear validation error identifying the offending field and does not save the block.
5. **Given** an admin is editing a template, **When** they open the preview, **Then** they see an accurate representation of how the page will look, including unpublished changes.

---

### User Story 3 - Admin manages the template list and lifecycle (Priority: P3)

An administrator browses all templates (filterable by type and status), creates new templates, publishes a draft to make it live, unpublishes a live template, and reviews/reverts to a previous version if a change needs to be undone.

**Why this priority**: Once templates and blocks can be authored (P2), staff need a way to organize, find, and safely promote/roll back templates without needing developer help — this is what makes ongoing content operations self-service.

**Independent Test**: Create a template, publish it, confirm it becomes visible on the front end, unpublish it, confirm the front end reverts to the fallback, then revert to a prior version and confirm the reverted content is restored as a new draft.

**Acceptance Scenarios**:

1. **Given** templates of different types and statuses exist, **When** the admin filters the template list by type or status, **Then** only matching templates are shown.
2. **Given** a draft template is ready, **When** the admin publishes it, **Then** its status changes to published and it becomes immediately visible to storefront visitors.
3. **Given** a published template, **When** the admin unpublishes it, **Then** the storefront reverts to the fallback page and the template returns to draft status without losing its content.
4. **Given** a template has version history, **When** the admin views past versions and reverts to one, **Then** a new draft is created from that version's snapshot for review before publishing again.

---

### User Story 4 - Admin configures SEO metadata and entity mapping (Priority: P4)

An administrator edits a template's SEO fields (page title, description, social image, canonical URL) and assigns/removes which category, product, or landing page uses the template.

**Why this priority**: This refines and connects templates to real pages but is not required for a minimally usable editor — it can follow once core authoring and publishing work.

**Independent Test**: Edit a template's SEO fields and save; assign the template to a category; confirm the category's page now uses the template and reflects the SEO fields in its page head.

**Acceptance Scenarios**:

1. **Given** an admin edits a template's SEO fields, **When** they save, **Then** the updated fields are persisted and reflected the next time the template is fetched.
2. **Given** an admin assigns a template to an entity (category, product, or landing page) that already has a different template assigned, **When** they confirm the reassignment, **Then** the entity now uses the newly assigned template.
3. **Given** an admin removes a template's mapping to an entity, **When** the mapping is removed, **Then** that entity's page falls back to the default layout.

---

### Edge Cases

- What happens when two admins edit the same template's blocks concurrently? The later save should not silently overwrite the other's changes without warning.
- How does the front end handle a template that references a product or category that has since been deleted? The block should degrade gracefully (e.g., skip the missing item) rather than breaking the page.
- What happens when an admin tries to publish a template with no blocks? The system should warn but not necessarily block publishing.
- How is a slug conflict handled when creating or renaming a template? The admin must be shown the conflict and asked to choose a different slug.
- What happens when a visitor requests a template preview link without admin permissions? Access must be denied and the visitor redirected to the public (published) version or an error page.

## Requirements *(mandatory)*

### Functional Requirements

**Frontend (public rendering)**

- **FR-001**: The storefront MUST render a page's content from its mapped, published template, displaying all visible blocks in their configured order.
- **FR-002**: The storefront MUST support rendering every supported block type (hero, product grid, category list, featured products, rich text, image banner, testimonials, call-to-action, header, footer).
- **FR-003**: The storefront MUST apply a template's SEO metadata (page title, description, social preview fields) to the rendered page.
- **FR-004**: The storefront MUST fall back to that entity's existing default page layout when a category or product has no published template mapped to it. Landing pages have no pre-existing default layout, so a landing page URL with no published template at that slug MUST show a standard "page not found" state instead (see Assumptions).
- **FR-005**: The storefront MUST exclude blocks marked as hidden from the rendered output.
- **FR-006**: The storefront MUST degrade gracefully (omit or placeholder) when a block references catalog data (product/category) that no longer exists, rather than failing to render the page.

**Admin (template & block editor)**

- **FR-007**: Admin users MUST be able to view a paginated, filterable (by type, status, creator) list of all templates.
- **FR-008**: Admin users MUST be able to create a new template by choosing a name, type, and unique slug.
- **FR-009**: Admin users MUST be able to add, edit, remove, and reorder content blocks within a template.
- **FR-010**: Admin users MUST be able to toggle the visibility of an individual block without deleting it.
- **FR-011**: Admin users MUST be able to preview a template (including unpublished changes) before making it live.
- **FR-012**: Admin users MUST be able to edit a template's SEO/social metadata fields.
- **FR-013**: The editor MUST validate block content (e.g., referenced product/category IDs must exist) and surface field-level errors before saving.
- **FR-014**: The editor MUST prevent creating or renaming a template to a slug already used by another template, surfacing a clear conflict message.

**Admin (lifecycle & mapping)**

- **FR-015**: Admin users MUST be able to publish a draft template, making its current content live immediately.
- **FR-016**: Admin users MUST be able to unpublish a live template, reverting the storefront to the fallback layout while preserving the draft content.
- **FR-017**: Admin users MUST be able to view a template's version history and revert to a previous version, which creates a new draft for review rather than overwriting live content directly.
- **FR-018**: Admin users MUST be able to assign a template to an entity (category, product, or landing page) and remove that assignment.
- **FR-019**: Admin users MUST be able to see which entities currently use a given template.

### Key Entities *(include if feature involves data)*

- **Template**: A named, typed (landing page/category page/product page/custom) page definition with a unique slug, lifecycle status (draft/published/archived), a version number, an ordered set of blocks, and SEO metadata. Rendered by the storefront when mapped to an entity and published.
- **Template Block**: A single content unit within a template (e.g., hero banner, product grid, rich text) with a type, display order, visibility flag, and type-specific content payload.
- **Template Version**: An immutable snapshot of a template's blocks and metadata captured at publish time, used for history review and revert.
- **Page Content Mapping**: The association between a template and a specific entity (a category, product, or landing page) that determines which template renders when that entity's page is requested.
- **SEO Metadata**: The set of page title, description, social preview, canonical URL, and structured data fields attached to a template, applied to the rendered page's head.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A published template's content is visible on its mapped storefront page within 1 second of an admin publishing it.
- **SC-002**: Non-technical admin staff can create, arrange, and publish a template with at least 3 blocks in under 10 minutes without developer assistance.
- **SC-003**: 100% of supported block types render correctly on the storefront with no visual or content discrepancy from what was configured in the editor preview.
- **SC-004**: Admins can locate any existing template in the management list (by name, type, or status filter) in under 15 seconds.
- **SC-005**: When a template is unpublished or an entity mapping is removed, the affected storefront page reflects the fallback layout on the very next page load with no stale cached content shown.
- **SC-006**: 0% of pages fail to render (error page shown) due to a missing/deleted catalog item referenced by a block — such blocks always degrade gracefully instead.

## Assumptions

- The backend Templates module REST API described in `specs/002-backend-ecommerce/contracts/templates-contract.md` is the system of record for template data; this feature only adds the consuming frontend (`front`) and authoring UI (`admin`) on top of it.
- "Front" (storefront) needs only read access to published templates via the public API; all authoring happens in `admin`.
- Drag-and-drop block reordering is the desired interaction, but simple ordinal controls (move up/down) are an acceptable first-release fallback if drag-and-drop proves too costly.
- Live/real-time collaborative editing (multiple admins editing simultaneously with conflict resolution) is out of scope for the first release; last-write-wins with a stale-data warning is sufficient.
- A single default/fallback page layout per entity type (landing/category/product) already exists or will be defined separately; this feature does not need to design that fallback's content, only trigger its use when no template is mapped or published.
- Only users with the ADMIN role can access the template editor and lifecycle actions, consistent with the existing backend contract's authorization rules.
- Template preview links are restricted to authenticated admin users; there is no shareable public preview link in this release.
- **Template-to-entity slug convention**: since the storefront cannot call admin-only reverse-lookup endpoints, a category or product's mapped template MUST be identified by using that same category/product's own routing slug (or ID) as the template's `slug`. Admins creating/mapping a template to an entity are responsible for setting the template's slug to match. This requires no backend contract change; it is a naming convention enforced by admin UI guidance, not by the backend.
- Landing pages have no pre-existing default content (unlike categories/products, which have an existing storefront layout independent of templates), so an unmapped/unpublished landing page slug shows a standard "page not found" state rather than a fallback layout (see FR-004, User Story 1 Acceptance Scenario 5).
