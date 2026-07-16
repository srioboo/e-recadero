import type {
  Template,
  TemplateType,
  TemplateStatus,
  TemplateBlock,
  BlockType,
  BlockContent,
  TemplateMeta,
  TemplateVersion,
  PageContentMapping,
  EntityType,
  MappingStatus,
  PaginatedResponse,
} from './types';
import { getAccessToken } from './storage';

// import.meta.env (not process.env) so this resolves identically whether the
// call runs server-side (Astro frontmatter) or client-side (React islands).
const API_BASE_URL = import.meta.env.PUBLIC_API_V1_BASE_URL || 'http://localhost:8080/api/v1';
// Preview is served from the public /api root (no /v1 segment), per templates-contract.md
const PUBLIC_API_BASE_URL = import.meta.env.PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

// ---------------------------------------------------------------------------
// Typed errors
// See specs/003-templates-frontend-admin/contracts/admin-api-client.md
// ---------------------------------------------------------------------------

export class DuplicateSlugError extends Error {
  readonly field: string;
  readonly providedSlug: string;

  constructor(message: string, field: string, providedSlug: string) {
    super(message);
    this.name = 'DuplicateSlugError';
    this.field = field;
    this.providedSlug = providedSlug;
  }
}

export interface BlockContentFieldError {
  field: string;
  issue: string;
}

export class InvalidBlockContentError extends Error {
  readonly blockType?: string;
  readonly errors: BlockContentFieldError[];

  constructor(message: string, blockType: string | undefined, errors: BlockContentFieldError[]) {
    super(message);
    this.name = 'InvalidBlockContentError';
    this.blockType = blockType;
    this.errors = errors;
  }
}

export class UnauthorizedError extends Error {
  readonly status: number;

  constructor(status: number) {
    super(`Unauthorized (${status})`);
    this.name = 'UnauthorizedError';
    this.status = status;
  }
}

async function parseErrorBody(response: Response): Promise<any> {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

async function templatesFetch<T>(
  baseUrl: string,
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${baseUrl}${endpoint}`;
  // Auth: this backend uses stateless JWT bearer tokens (not session cookies).
  // Token is only available client-side (post-login), via the Users/Auth
  // module — not yet implemented. Requests made without one will be
  // rejected by the backend as 401, surfaced below as UnauthorizedError.
  const accessToken = getAccessToken();

  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
    ...options,
  });

  if (response.status === 401 || response.status === 403) {
    throw new UnauthorizedError(response.status);
  }

  if (!response.ok) {
    const body = await parseErrorBody(response);

    if (body?.error_code === 'DUPLICATE_SLUG') {
      throw new DuplicateSlugError(
        body.message || 'Template slug must be unique',
        body.details?.field || 'slug',
        body.details?.provided_slug || ''
      );
    }

    if (body?.error_code === 'INVALID_BLOCK_CONTENT') {
      throw new InvalidBlockContentError(
        body.message || 'Block content validation failed',
        body.details?.block_type,
        body.details?.errors || []
      );
    }

    throw new Error(body?.message || `Templates API error: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as unknown as T;
  }

  return response.json();
}

function adminFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  return templatesFetch<T>(API_BASE_URL, endpoint, options);
}

// ---------------------------------------------------------------------------
// Template management
// ---------------------------------------------------------------------------

export interface ListTemplatesFilters {
  type?: TemplateType;
  status?: TemplateStatus;
  createdBy?: string;
  limit?: number;
  offset?: number;
}

export async function listTemplates(
  filters: ListTemplatesFilters = {}
): Promise<PaginatedResponse<Template>> {
  const params = new URLSearchParams();
  if (filters.type) params.append('type', filters.type);
  if (filters.status) params.append('status', filters.status);
  if (filters.createdBy) params.append('created_by', filters.createdBy);
  params.append('limit', (filters.limit ?? 20).toString());
  params.append('offset', (filters.offset ?? 0).toString());

  return adminFetch<PaginatedResponse<Template>>(`/templates?${params.toString()}`);
}

export interface CreateTemplateInput {
  name: string;
  type: TemplateType;
  slug: string;
}

export async function createTemplate(input: CreateTemplateInput): Promise<Template> {
  return adminFetch<Template>('/templates', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export async function getTemplate(templateId: string): Promise<Template> {
  return adminFetch<Template>(`/templates/${templateId}`);
}

export interface UpdateTemplateInput {
  name?: string;
  meta?: Partial<TemplateMeta>;
}

export async function updateTemplate(
  templateId: string,
  input: UpdateTemplateInput
): Promise<Template> {
  return adminFetch<Template>(`/templates/${templateId}`, {
    method: 'PUT',
    body: JSON.stringify(input),
  });
}

// ---------------------------------------------------------------------------
// Block management
// ---------------------------------------------------------------------------

export interface AddBlockInput {
  blockType: BlockType;
  blockName: string;
  blockOrder: number;
  content: BlockContent;
}

export async function addBlock(
  templateId: string,
  input: AddBlockInput
): Promise<TemplateBlock> {
  return adminFetch<TemplateBlock>(`/templates/${templateId}/blocks`, {
    method: 'POST',
    body: JSON.stringify({
      block_type: input.blockType,
      block_name: input.blockName,
      block_order: input.blockOrder,
      content: input.content,
    }),
  });
}

export interface UpdateBlockInput {
  content?: Partial<BlockContent>;
  blockOrder?: number;
}

export async function updateBlock(
  templateId: string,
  blockId: string,
  input: UpdateBlockInput
): Promise<TemplateBlock> {
  return adminFetch<TemplateBlock>(`/templates/${templateId}/blocks/${blockId}`, {
    method: 'PUT',
    body: JSON.stringify({
      content: input.content,
      block_order: input.blockOrder,
    }),
  });
}

export async function setBlockVisibility(
  templateId: string,
  blockId: string,
  isVisible: boolean
): Promise<{ block_id: string; is_visible: boolean }> {
  return adminFetch(`/templates/${templateId}/blocks/${blockId}/visibility`, {
    method: 'PATCH',
    body: JSON.stringify({ is_visible: isVisible }),
  });
}

export async function removeBlock(templateId: string, blockId: string): Promise<void> {
  await adminFetch<void>(`/templates/${templateId}/blocks/${blockId}`, {
    method: 'DELETE',
  });
}

export interface BlockOrderEntry {
  blockId: string;
  blockOrder: number;
}

export async function reorderBlocks(
  templateId: string,
  order: BlockOrderEntry[]
): Promise<void> {
  await adminFetch<{ message: string }>(`/templates/${templateId}/blocks/reorder`, {
    method: 'POST',
    body: JSON.stringify({
      blocks: order.map((entry) => ({
        block_id: entry.blockId,
        block_order: entry.blockOrder,
      })),
    }),
  });
}

// ---------------------------------------------------------------------------
// Metadata
// ---------------------------------------------------------------------------

export async function updateTemplateMeta(
  templateId: string,
  meta: TemplateMeta
): Promise<{ template_id: string; meta: TemplateMeta; updated_at: string }> {
  return adminFetch(`/templates/${templateId}/meta`, {
    method: 'PUT',
    body: JSON.stringify(meta),
  });
}

// ---------------------------------------------------------------------------
// Publishing & versioning
// ---------------------------------------------------------------------------

export async function publishTemplate(
  templateId: string,
  changeNote?: string
): Promise<{ status: 'PUBLISHED'; version: number; published_version: number; published_at: string }> {
  return adminFetch(`/templates/${templateId}/publish`, {
    method: 'POST',
    body: JSON.stringify({ change_note: changeNote }),
  });
}

export async function unpublishTemplate(
  templateId: string
): Promise<{ status: 'DRAFT'; message: string }> {
  return adminFetch(`/templates/${templateId}/unpublish`, {
    method: 'POST',
  });
}

export async function listTemplateVersions(
  templateId: string,
  pagination: { limit?: number; offset?: number } = {}
): Promise<PaginatedResponse<TemplateVersion>> {
  const params = new URLSearchParams();
  params.append('limit', (pagination.limit ?? 10).toString());
  params.append('offset', (pagination.offset ?? 0).toString());

  return adminFetch<PaginatedResponse<TemplateVersion>>(
    `/templates/${templateId}/versions?${params.toString()}`
  );
}

export async function getTemplateVersion(
  templateId: string,
  versionNumber: number
): Promise<TemplateVersion> {
  return adminFetch<TemplateVersion>(`/templates/${templateId}/versions/${versionNumber}`);
}

export async function revertTemplate(
  templateId: string,
  versionNumber: number,
  changeNote?: string
): Promise<{ status: 'DRAFT'; version: number; blocks: TemplateBlock[] }> {
  return adminFetch(`/templates/${templateId}/revert/${versionNumber}`, {
    method: 'POST',
    body: JSON.stringify({ change_note: changeNote }),
  });
}

// ---------------------------------------------------------------------------
// Preview (shared behavior with front's fetchTemplatePreview)
// ---------------------------------------------------------------------------

export async function previewTemplate(templateId: string, version?: number): Promise<Template> {
  const params = new URLSearchParams();
  if (version !== undefined) params.append('version', version.toString());
  const query = params.toString() ? `?${params.toString()}` : '';

  return templatesFetch<Template>(
    PUBLIC_API_BASE_URL,
    `/templates/preview/${templateId}${query}`
  );
}

// ---------------------------------------------------------------------------
// Entity mapping
// ---------------------------------------------------------------------------

export interface MapEntityInput {
  entityId: string;
  entityType: EntityType;
  status?: MappingStatus;
}

export async function mapTemplateToEntity(
  templateId: string,
  input: MapEntityInput
): Promise<PageContentMapping> {
  return adminFetch<PageContentMapping>(`/templates/${templateId}/map-entity`, {
    method: 'POST',
    body: JSON.stringify({
      entity_id: input.entityId,
      entity_type: input.entityType,
      status: input.status,
    }),
  });
}

export async function getTemplateEntities(
  templateId: string,
  filters: { entityType?: EntityType; status?: MappingStatus } = {}
): Promise<PaginatedResponse<PageContentMapping>> {
  const params = new URLSearchParams();
  if (filters.entityType) params.append('entity_type', filters.entityType);
  if (filters.status) params.append('status', filters.status);
  const query = params.toString() ? `?${params.toString()}` : '';

  return adminFetch<PaginatedResponse<PageContentMapping>>(
    `/templates/${templateId}/entities${query}`
  );
}

export async function unmapTemplateEntity(templateId: string, entityId: string): Promise<void> {
  await adminFetch<void>(`/templates/${templateId}/map-entity/${entityId}`, {
    method: 'DELETE',
  });
}
