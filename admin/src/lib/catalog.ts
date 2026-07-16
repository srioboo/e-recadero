// Minimal read-only Catalog client for the template preview (T029), which
// needs to resolve product/category IDs referenced by PRODUCT_GRID,
// FEATURED_PRODUCTS, and CATEGORY_LIST blocks. Catalog GET endpoints are
// public (see specs/002-backend-ecommerce/contracts/catalog-contract.md),
// so no auth token is required here.

const API_BASE_URL = import.meta.env.PUBLIC_API_V1_BASE_URL || 'http://localhost:8080/api/v1';

export interface CatalogProduct {
  id: string;
  sku: string;
  name: string;
  base_price: number;
  images?: { url: string; alt?: string }[];
}

export interface CatalogCategory {
  id: string;
  name: string;
  slug: string;
  image_url?: string;
}

export async function fetchProductDetail(productId: string): Promise<CatalogProduct> {
  const response = await fetch(`${API_BASE_URL}/products/${productId}`);
  if (!response.ok) {
    throw new Error(`Not found: ${productId}`);
  }
  return response.json();
}

export async function fetchCategoryDetail(categoryId: string): Promise<CatalogCategory> {
  const response = await fetch(`${API_BASE_URL}/categories/${categoryId}`);
  if (!response.ok) {
    throw new Error(`Not found: ${categoryId}`);
  }
  return response.json();
}

export async function resolveProducts(productIds: string[]): Promise<CatalogProduct[]> {
  const results = await Promise.all(
    productIds.map(async (id) => {
      try {
        return await fetchProductDetail(id);
      } catch {
        return null;
      }
    })
  );
  return results.filter((product): product is CatalogProduct => product !== null);
}

export async function resolveCategories(categoryIds: string[]): Promise<CatalogCategory[]> {
  const results = await Promise.all(
    categoryIds.map(async (id) => {
      try {
        return await fetchCategoryDetail(id);
      } catch {
        return null;
      }
    })
  );
  return results.filter((category): category is CatalogCategory => category !== null);
}
