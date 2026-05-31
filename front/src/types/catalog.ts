// Catalog TypeScript Interfaces
// Based on backend catalog contract

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parent_id?: string | null;
  image_url?: string;
  sort_order?: number;
  status?: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  children?: Category[];
  created_at?: string;
}

export interface ProductAttribute {
  name: string;
  type: 'SELECT' | 'TEXT' | 'NUMBER';
  values: string[];
}

export interface ProductVariant {
  id: string;
  sku: string;
  variant_attributes: Record<string, string>;
  price: number;
  available_quantity: number;
  weight?: number;
  dimensions_json?: {
    length_cm?: number;
    width_cm?: number;
    height_cm?: number;
  };
}

export interface ProductImage {
  url: string;
  alt?: string;
  is_primary?: boolean;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  description?: string;
  long_description?: string;
  category_id: string;
  base_price: number;
  cost_price?: number;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  featured?: boolean;
  available_quantity?: number;
  attributes?: ProductAttribute[];
  variants?: ProductVariant[];
  images?: ProductImage[];
  created_at?: string;
  updated_at?: string;
}

export interface ProductVariantAvailability {
  variant_id: string;
  available_quantity: number;
  is_available: boolean;
  reorder_status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
}

export interface ProductAvailability {
  product_id: string;
  variants: ProductVariantAvailability[];
  timestamp: string;
}

export interface InventoryLevel {
  product_variant_id: string;
  quantity_on_hand: number;
  reserved_quantity: number;
  available_quantity: number;
  reorder_level: number;
  last_counted_at: string;
}

export interface SearchFilters {
  query?: string;
  categoryId?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'price_asc' | 'price_desc' | 'name_asc' | 'created_at_desc' | 'popularity';
  page?: number;
  limit?: number;
  inStockOnly?: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    total: number;
    limit: number;
    offset: number;
    has_more?: boolean;
  };
}
