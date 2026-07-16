import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { Product, Category } from '../../src/types/catalog';

vi.mock('../../src/lib/api', () => ({
  fetchProductDetail: vi.fn(),
  fetchCategoryDetail: vi.fn(),
}));

import { fetchProductDetail, fetchCategoryDetail } from '../../src/lib/api';
import { resolveProducts, resolveCategories } from '../../src/lib/templates';

const mockedFetchProductDetail = vi.mocked(fetchProductDetail);
const mockedFetchCategoryDetail = vi.mocked(fetchCategoryDetail);

function makeProduct(id: string): Product {
  return {
    id,
    sku: `sku-${id}`,
    name: `Product ${id}`,
    category_id: 'cat-1',
    base_price: 10,
  };
}

function makeCategory(id: string): Category {
  return {
    id,
    name: `Category ${id}`,
    slug: `category-${id}`,
  };
}

describe('resolveProducts', () => {
  beforeEach(() => {
    mockedFetchProductDetail.mockReset();
  });

  it('returns all products when every ID resolves', async () => {
    mockedFetchProductDetail.mockImplementation(async (id: string) => makeProduct(id));

    const result = await resolveProducts(['p1', 'p2']);

    expect(result).toEqual([makeProduct('p1'), makeProduct('p2')]);
  });

  it('silently omits IDs that fail to resolve (FR-006)', async () => {
    mockedFetchProductDetail.mockImplementation(async (id: string) => {
      if (id === 'missing') {
        throw new Error('Not found');
      }
      return makeProduct(id);
    });

    const result = await resolveProducts(['p1', 'missing', 'p2']);

    expect(result).toEqual([makeProduct('p1'), makeProduct('p2')]);
  });

  it('returns an empty array without throwing when every ID fails to resolve', async () => {
    mockedFetchProductDetail.mockRejectedValue(new Error('Not found'));

    const result = await resolveProducts(['gone1', 'gone2']);

    expect(result).toEqual([]);
  });

  it('returns an empty array for an empty input list', async () => {
    const result = await resolveProducts([]);

    expect(result).toEqual([]);
    expect(mockedFetchProductDetail).not.toHaveBeenCalled();
  });
});

describe('resolveCategories', () => {
  beforeEach(() => {
    mockedFetchCategoryDetail.mockReset();
  });

  it('returns all categories when every ID resolves', async () => {
    mockedFetchCategoryDetail.mockImplementation(async (id: string) => makeCategory(id));

    const result = await resolveCategories(['c1', 'c2']);

    expect(result).toEqual([makeCategory('c1'), makeCategory('c2')]);
  });

  it('silently omits IDs that fail to resolve', async () => {
    mockedFetchCategoryDetail.mockImplementation(async (id: string) => {
      if (id === 'missing') {
        throw new Error('Not found');
      }
      return makeCategory(id);
    });

    const result = await resolveCategories(['c1', 'missing', 'c2']);

    expect(result).toEqual([makeCategory('c1'), makeCategory('c2')]);
  });
});
