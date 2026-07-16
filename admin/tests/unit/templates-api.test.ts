import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  createTemplate,
  updateTemplate,
  addBlock,
  updateBlock,
  getTemplate,
  DuplicateSlugError,
  InvalidBlockContentError,
  UnauthorizedError,
} from '../../src/lib/api';

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('admin templates API error mapping', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('maps a 409/DUPLICATE_SLUG response to DuplicateSlugError on createTemplate', async () => {
    vi.mocked(fetch).mockImplementation(async () =>
      jsonResponse(409, {
        error_code: 'DUPLICATE_SLUG',
        message: 'Template slug must be unique',
        details: { field: 'slug', provided_slug: 'summer-sale' },
      })
    );

    try {
      await createTemplate({ name: 'Summer Sale', type: 'LANDING_PAGE', slug: 'summer-sale' });
      expect.unreachable();
    } catch (error) {
      expect(error).toBeInstanceOf(DuplicateSlugError);
      const duplicateError = error as DuplicateSlugError;
      expect(duplicateError.field).toBe('slug');
      expect(duplicateError.providedSlug).toBe('summer-sale');
    }
  });

  it('maps a 409/DUPLICATE_SLUG response to DuplicateSlugError on updateTemplate', async () => {
    vi.mocked(fetch).mockImplementation(async () => 
      jsonResponse(409, {
        error_code: 'DUPLICATE_SLUG',
        message: 'Template slug must be unique',
        details: { field: 'slug', provided_slug: 'existing-slug' },
      })
    );

    await expect(updateTemplate('tmpl-1', { name: 'Renamed' })).rejects.toBeInstanceOf(
      DuplicateSlugError
    );
  });

  it('maps a 400/INVALID_BLOCK_CONTENT response to InvalidBlockContentError on addBlock', async () => {
    vi.mocked(fetch).mockImplementation(async () => 
      jsonResponse(400, {
        error_code: 'INVALID_BLOCK_CONTENT',
        message: 'Block content validation failed',
        details: {
          block_type: 'HERO',
          errors: [{ field: 'title', issue: 'must not be blank' }],
        },
      })
    );

    try {
      await addBlock('tmpl-1', {
        blockType: 'HERO',
        blockName: 'Main hero',
        blockOrder: 1,
        content: { title: '' },
      });
      expect.unreachable();
    } catch (error) {
      expect(error).toBeInstanceOf(InvalidBlockContentError);
      const invalidContentError = error as InvalidBlockContentError;
      expect(invalidContentError.blockType).toBe('HERO');
      expect(invalidContentError.errors).toEqual([{ field: 'title', issue: 'must not be blank' }]);
    }
  });

  it('maps a 400/INVALID_BLOCK_CONTENT response to InvalidBlockContentError on updateBlock', async () => {
    vi.mocked(fetch).mockImplementation(async () => 
      jsonResponse(400, {
        error_code: 'INVALID_BLOCK_CONTENT',
        message: 'Block content validation failed',
        details: {
          block_type: 'CTA',
          errors: [{ field: 'link', issue: 'must be a valid URL' }],
        },
      })
    );

    await expect(
      updateBlock('tmpl-1', 'block-1', { content: { text: 'Buy now', link: 'not-a-url' } })
    ).rejects.toBeInstanceOf(InvalidBlockContentError);
  });

  it('maps a 401 response to UnauthorizedError', async () => {
    vi.mocked(fetch).mockImplementation(async () => jsonResponse(401, { message: 'Unauthorized' }));

    await expect(getTemplate('tmpl-1')).rejects.toBeInstanceOf(UnauthorizedError);
  });

  it('maps a 403 response to UnauthorizedError', async () => {
    vi.mocked(fetch).mockImplementation(async () => jsonResponse(403, { message: 'Forbidden' }));

    await expect(getTemplate('tmpl-1')).rejects.toBeInstanceOf(UnauthorizedError);
  });

  it('throws a generic Error for other non-2xx responses', async () => {
    vi.mocked(fetch).mockImplementation(async () => jsonResponse(500, { message: 'Internal error' }));

    await expect(getTemplate('tmpl-1')).rejects.toThrow('Internal error');
  });

  it('resolves normally on a 2xx response', async () => {
    vi.mocked(fetch).mockImplementation(async () => 
      jsonResponse(200, { template_id: 'tmpl-1', name: 'Home', type: 'LANDING_PAGE', slug: 'home', status: 'DRAFT', version: 1, published_version: null })
    );

    const result = await getTemplate('tmpl-1');

    expect(result.template_id).toBe('tmpl-1');
  });
});
