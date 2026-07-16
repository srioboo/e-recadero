import type {
  Category,
  Product,
  ProductAvailability,
  PaginatedResponse,
  SearchFilters,
} from '../types/catalog';
import type { Template } from '../types/templates';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';
// Public Templates endpoints live under /api (no /v1 segment) per templates-contract.md
const PUBLIC_API_BASE_URL = process.env.PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

async function fetchAPI<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;

  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`Not found: ${endpoint}`);
      }
      throw new Error(`API error: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error(`Failed to fetch ${endpoint}:`, error);
    throw error;
  }
}

export async function fetchCategories(
  parentId?: string,
  limit: number = 20,
  offset: number = 0
): Promise<PaginatedResponse<Category>> {
  const params = new URLSearchParams({
    limit: limit.toString(),
    offset: offset.toString(),
  });

  if (parentId) {
    params.append('parent_id', parentId);
  }

  const response = await fetchAPI<any>(`/categories?${params.toString()}`);
  return {
    data: response.data || [],
    pagination: response.pagination || {
      total: 0,
      limit,
      offset,
      has_more: false,
    },
  };
}

export async function fetchCategoryDetail(categoryId: string): Promise<Category> {
  return fetchAPI<Category>(`/categories/${categoryId}`);
}

export async function fetchProducts(
  filters: SearchFilters = {}
): Promise<PaginatedResponse<Product>> {
  const params = new URLSearchParams();

  if (filters.query) params.append('query', filters.query);
  if (filters.categoryId) params.append('category_id', filters.categoryId);
  if (filters.minPrice !== undefined) params.append('min_price', filters.minPrice.toString());
  if (filters.maxPrice !== undefined) params.append('max_price', filters.maxPrice.toString());
  if (filters.sortBy) params.append('sort', filters.sortBy);
  if (filters.inStockOnly) params.append('in_stock_only', 'true');

  params.append('limit', (filters.limit || 20).toString());
  params.append('offset', (((filters.page || 1) - 1) * (filters.limit || 20)).toString());

  const response = await fetchAPI<any>(`/products?${params.toString()}`);
  return {
    data: response.data || [],
    pagination: response.pagination || {
      total: 0,
      limit: filters.limit || 20,
      offset: ((filters.page || 1) - 1) * (filters.limit || 20),
      has_more: false,
    },
  };
}

export async function fetchProductDetail(productId: string): Promise<Product> {
  return fetchAPI<Product>(`/products/${productId}`);
}

export async function fetchProductAvailability(
  productId: string
): Promise<ProductAvailability> {
  return fetchAPI<ProductAvailability>(`/products/${productId}/availability`);
}

export async function searchProducts(
  query: string,
  categoryId?: string,
  limit: number = 20,
  offset: number = 0
): Promise<PaginatedResponse<Product>> {
  return fetchProducts({
    query,
    categoryId,
    limit,
    page: Math.floor(offset / limit) + 1,
  });
}

// ---------------------------------------------------------------------------
// Templates module (public rendering)
// See specs/003-templates-frontend-admin/contracts/front-api-client.md
// ---------------------------------------------------------------------------

/**
 * Fetch a published template by slug (public endpoint, no auth).
 * Per the entity/template slug convention (spec.md Assumptions), category/product
 * pages call this with their own routing slug/ID.
 * Returns `null` on 404 so callers can fall back to their default layout;
 * throws on any other non-2xx response.
 */
export async function fetchTemplateBySlug(slug: string): Promise<Template | null> {
  const url = `${PUBLIC_API_BASE_URL}/templates/${slug}`;

  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new Error(`Templates API error: ${response.status}`);
  }

  return response.json();
}

/**
 * Fetch an admin preview of a template (may include unpublished changes).
 * Requires an authenticated admin session; throws on 401/403 so callers can
 * redirect to login or show an access-denied state.
 */
export async function fetchTemplatePreview(
  templateId: string,
  version?: number,
  accessToken?: string
): Promise<Template> {
  const params = new URLSearchParams();
  if (version !== undefined) params.append('version', version.toString());
  const query = params.toString() ? `?${params.toString()}` : '';
  const url = `${PUBLIC_API_BASE_URL}/templates/preview/${templateId}${query}`;

  // Auth: this backend uses stateless JWT bearer tokens (not session cookies) —
  // callers with an authenticated admin session must pass their access token.
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
  });

  if (response.status === 401 || response.status === 403) {
    throw new Error(`Unauthorized (${response.status})`);
  }

  if (!response.ok) {
    throw new Error(`Template preview error: ${response.status}`);
  }

  return response.json();
}
