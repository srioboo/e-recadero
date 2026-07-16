# Contract: `admin` Templates API Client

**Location**: `admin/src/lib/api.ts` (new templates section)
**Backing REST contract**: `specs/002-backend-ecommerce/contracts/templates-contract.md` — Template Management, Blocks, Metadata, Publishing & Versioning, Page Content Mapping sections (all admin-only endpoints)

All functions below require an authenticated ADMIN-role session; every function throws on `401`/`403` and callers must redirect to login / show an access-denied state.

## Template management

- `listTemplates(filters: { type?: TemplateType; status?: TemplateStatus; createdBy?: string; limit?: number; offset?: number }): Promise<{ data: Template[]; pagination: { total: number; limit: number; offset: number } }>` → `GET /api/v1/templates`
- `createTemplate(input: { name: string; type: TemplateType; slug: string }): Promise<Template>` → `POST /api/v1/templates`; throws typed `DuplicateSlugError` on `409` so the create form can show a field-level error.
- `getTemplate(templateId: string): Promise<Template>` → `GET /api/v1/templates/{template_id}` (full detail incl. blocks + meta)
- `updateTemplate(templateId: string, input: { name?: string; meta?: Partial<TemplateMeta> }): Promise<Template>` → `PUT /api/v1/templates/{template_id}`

## Block management

- `addBlock(templateId: string, input: { blockType: BlockType; blockName: string; blockOrder: number; content: BlockContent }): Promise<TemplateBlock>` → `POST /api/v1/templates/{template_id}/blocks`; throws typed `InvalidBlockContentError` with per-field issues on `400` so `BlockForm.tsx` can render inline validation.
- `updateBlock(templateId: string, blockId: string, input: { content?: Partial<BlockContent>; blockOrder?: number }): Promise<TemplateBlock>` → `PUT /api/v1/templates/{template_id}/blocks/{block_id}`
- `setBlockVisibility(templateId: string, blockId: string, isVisible: boolean): Promise<{ blockId: string; isVisible: boolean }>` → `PATCH /api/v1/templates/{template_id}/blocks/{block_id}/visibility`
- `removeBlock(templateId: string, blockId: string): Promise<void>` → `DELETE /api/v1/templates/{template_id}/blocks/{block_id}` (`204`)
- `reorderBlocks(templateId: string, order: { blockId: string; blockOrder: number }[]): Promise<void>` → `POST /api/v1/templates/{template_id}/blocks/reorder`

## Metadata

- `updateTemplateMeta(templateId: string, meta: TemplateMeta): Promise<{ templateId: string; meta: TemplateMeta; updatedAt: string }>` → `PUT /api/v1/templates/{template_id}/meta`

## Publishing & versioning

- `publishTemplate(templateId: string, changeNote?: string): Promise<{ status: 'PUBLISHED'; version: number; publishedVersion: number; publishedAt: string }>` → `POST /api/v1/templates/{template_id}/publish`
- `unpublishTemplate(templateId: string): Promise<{ status: 'DRAFT' }>` → `POST /api/v1/templates/{template_id}/unpublish`
- `listTemplateVersions(templateId: string, pagination?: { limit?: number; offset?: number }): Promise<{ data: TemplateVersion[]; pagination: { total: number; limit: number; offset: number } }>` → `GET /api/v1/templates/{template_id}/versions`
- `getTemplateVersion(templateId: string, versionNumber: number): Promise<TemplateVersion>` → `GET /api/v1/templates/{template_id}/versions/{version_number}`
- `revertTemplate(templateId: string, versionNumber: number, changeNote?: string): Promise<{ status: 'DRAFT'; version: number; blocks: TemplateBlock[] }>` → `POST /api/v1/templates/{template_id}/revert/{version_number}`

## Preview

- `previewTemplate(templateId: string, version?: number): Promise<Template>` → `GET /api/templates/preview/{template_id}` (shared with `front`'s equivalent function; see `front-api-client.md`)

## Entity mapping

- `mapTemplateToEntity(templateId: string, input: { entityId: string; entityType: EntityType; status?: MappingStatus }): Promise<PageContentMapping>` → `POST /api/v1/templates/{template_id}/map-entity`; UI must confirm with the user before calling this if the entity already has a different mapping (fetch current mapping first via `getTemplateEntities` on the *other* template, or surface the contract's own conflict signal if the backend returns one).
- `getTemplateEntities(templateId: string, filters?: { entityType?: EntityType; status?: MappingStatus }): Promise<{ data: PageContentMapping[]; pagination: { total: number; limit: number; offset: number } }>` → `GET /api/v1/templates/{template_id}/entities`
- `unmapTemplateEntity(templateId: string, entityId: string): Promise<void>` → `DELETE /api/v1/templates/{template_id}/map-entity/{entity_id}` (`204`)

## Error handling conventions (all functions)

- `DuplicateSlugError` (`409`, `error_code: DUPLICATE_SLUG`) — surfaced as a field-level error on the `slug` input (FR-014).
- `InvalidBlockContentError` (`400`, `error_code: INVALID_BLOCK_CONTENT`) — surfaced as field-level errors per `details.errors[]` (FR-013).
- Any other non-2xx — surfaced as a generic toast/banner error; caller does not attempt automatic retry.
- All mutating calls (`add/update/remove/reorder Block`, `publish/unpublish/revert`, `map/unmap entity`) should optimistically disable the relevant UI control until the request resolves, to avoid duplicate submissions — this is a UI-level concern, not part of the request/response contract itself.
