import type {
  Category,
  Product,
  ProductAvailability,
  PaginatedResponse,
  SearchFilters,
} from '../types/catalog';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

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
  params.append('offset', ((filters.page || 1) - 1) * (filters.limit || 20));

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
