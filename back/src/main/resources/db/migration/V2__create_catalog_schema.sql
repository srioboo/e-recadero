-- Flyway Migration V2: Create Catalog Module Tables
-- This migration creates all tables for the catalog module

-- Category table
CREATE TABLE IF NOT EXISTS catalog.category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT REFERENCES catalog.category(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_category_parent ON catalog.category(parent_category_id);
CREATE INDEX idx_category_active ON catalog.category(is_active);
COMMENT ON TABLE catalog.category IS 'Product categories with hierarchical parent-child relationships';

-- Product table
CREATE TABLE IF NOT EXISTS catalog.product (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES catalog.category(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    sku VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    cost_price DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    rating DECIMAL(3, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_product_category ON catalog.product(category_id);
CREATE INDEX idx_product_sku ON catalog.product(sku);
CREATE INDEX idx_product_active ON catalog.product(is_active);
CREATE INDEX idx_product_featured ON catalog.product(is_featured);
COMMENT ON TABLE catalog.product IS 'Main product catalog with pricing and metadata';

-- Product Attribute table
CREATE TABLE IF NOT EXISTS catalog.product_attribute (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES catalog.product(id) ON DELETE CASCADE,
    attribute_name VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_product_attr ON catalog.product_attribute(product_id, attribute_name);
COMMENT ON TABLE catalog.product_attribute IS 'Dynamic attributes for products (color, size, material, etc.)';

-- Product Variant table
CREATE TABLE IF NOT EXISTS catalog.product_variant (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES catalog.product(id) ON DELETE CASCADE,
    variant_sku VARCHAR(100) NOT NULL UNIQUE,
    variant_name VARCHAR(255),
    price_modifier DECIMAL(10, 2) DEFAULT 0,
    cost_price_modifier DECIMAL(10, 2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_variant_product ON catalog.product_variant(product_id);
CREATE INDEX idx_variant_sku ON catalog.product_variant(variant_sku);
COMMENT ON TABLE catalog.product_variant IS 'Product variants (different sizes, colors, editions)';

-- Inventory table
CREATE TABLE IF NOT EXISTS catalog.inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES catalog.product(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES catalog.product_variant(id) ON DELETE CASCADE,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    quantity_damaged INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER DEFAULT 10,
    warehouse_location VARCHAR(255),
    last_restock_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_inventory_product ON catalog.inventory(product_id);
CREATE INDEX idx_inventory_variant ON catalog.inventory(variant_id);
COMMENT ON TABLE catalog.inventory IS 'Stock levels and warehouse tracking for products and variants';

-- Product Image table
CREATE TABLE IF NOT EXISTS catalog.product_image (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES catalog.product(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES catalog.product_variant(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_image_product ON catalog.product_image(product_id);
CREATE INDEX idx_image_variant ON catalog.product_image(variant_id);
COMMENT ON TABLE catalog.product_image IS 'Product images with ordering and alt text for accessibility';
