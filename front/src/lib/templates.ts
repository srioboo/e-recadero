import { fetchProductDetail, fetchCategoryDetail } from './api';
import type { Product, Category } from '../types/catalog';

/**
 * Resolve a list of product IDs against the catalog API, silently omitting
 * any that no longer exist (FR-006 / SC-006 graceful degradation) instead of
 * throwing and breaking the whole page render.
 */
export async function resolveProducts(productIds: string[]): Promise<Product[]> {
  const results = await Promise.all(
    productIds.map(async (id) => {
      try {
        return await fetchProductDetail(id);
      } catch {
        return null;
      }
    })
  );
  return results.filter((product): product is Product => product !== null);
}

/**
 * Resolve a list of category IDs against the catalog API, silently omitting
 * any that no longer exist.
 */
export async function resolveCategories(categoryIds: string[]): Promise<Category[]> {
  const results = await Promise.all(
    categoryIds.map(async (id) => {
      try {
        return await fetchCategoryDetail(id);
      } catch {
        return null;
      }
    })
  );
  return results.filter((category): category is Category => category !== null);
}
