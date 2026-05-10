-- Flyway Migration V6: Create Promotions Module Tables
-- This migration creates all tables for the promotions module

-- Promotion table
CREATE TABLE IF NOT EXISTS promotions.promotion (
    id BIGSERIAL PRIMARY KEY,
    promotion_name VARCHAR(255) NOT NULL,
    description TEXT,
    promotion_type VARCHAR(50) NOT NULL,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(10, 2),
    discount_percentage DECIMAL(5, 2),
    is_active BOOLEAN DEFAULT true,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    max_uses INTEGER,
    usage_count INTEGER DEFAULT 0,
    is_stackable BOOLEAN DEFAULT false,
    requires_code BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_promo_active ON promotions.promotion(is_active);
CREATE INDEX idx_promo_start_end ON promotions.promotion(start_date, end_date);
COMMENT ON TABLE promotions.promotion IS 'Promotional campaigns and discounts';

-- Promotion Rule table
CREATE TABLE IF NOT EXISTS promotions.promotion_rule (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL REFERENCES promotions.promotion(id) ON DELETE CASCADE,
    rule_type VARCHAR(100) NOT NULL,
    condition_data JSONB NOT NULL,
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_rule_promo ON promotions.promotion_rule(promotion_id);
COMMENT ON TABLE promotions.promotion_rule IS 'Rules determining when promotions apply (min purchase, category, etc.)';

-- Coupon Code table
CREATE TABLE IF NOT EXISTS promotions.coupon_code (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    promotion_id BIGINT REFERENCES promotions.promotion(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT true,
    usage_limit INTEGER,
    usage_count INTEGER DEFAULT 0,
    per_customer_limit INTEGER DEFAULT 1,
    discount_type VARCHAR(50),
    discount_value DECIMAL(10, 2),
    discount_percentage DECIMAL(5, 2),
    min_order_amount DECIMAL(12, 2),
    max_discount_amount DECIMAL(12, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255)
);
CREATE INDEX idx_coupon_code ON promotions.coupon_code(code);
CREATE INDEX idx_coupon_active ON promotions.coupon_code(is_active);
COMMENT ON TABLE promotions.coupon_code IS 'Discount coupon codes with usage tracking';

-- Coupon Usage table
CREATE TABLE IF NOT EXISTS promotions.coupon_usage (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES promotions.coupon_code(id) ON DELETE CASCADE,
    order_id BIGINT,
    user_id BIGINT NOT NULL,
    discount_applied DECIMAL(12, 2),
    used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    canceled_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_usage_coupon ON promotions.coupon_usage(coupon_id);
CREATE INDEX idx_usage_user ON promotions.coupon_usage(user_id);
CREATE INDEX idx_usage_order ON promotions.coupon_usage(order_id);
COMMENT ON TABLE promotions.coupon_usage IS 'Tracking of coupon code usage by customers';

-- Promotion Usage by User table
CREATE TABLE IF NOT EXISTS promotions.promotion_usage (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL REFERENCES promotions.promotion(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    discount_applied DECIMAL(12, 2),
    used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_promo_usage_promo ON promotions.promotion_usage(promotion_id);
CREATE INDEX idx_promo_usage_user ON promotions.promotion_usage(user_id);
COMMENT ON TABLE promotions.promotion_usage IS 'Tracking of promotion usage by customers';

-- Product Promotion table (applies promotion to specific products)
CREATE TABLE IF NOT EXISTS promotions.product_promotion (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL REFERENCES promotions.promotion(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_prod_promo ON promotions.product_promotion(product_id);
COMMENT ON TABLE promotions.product_promotion IS 'Links promotions to specific products';

-- Category Promotion table (applies promotion to product categories)
CREATE TABLE IF NOT EXISTS promotions.category_promotion (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL REFERENCES promotions.promotion(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cat_promo ON promotions.category_promotion(category_id);
COMMENT ON TABLE promotions.category_promotion IS 'Links promotions to product categories';

-- Promotion Exclusion table
CREATE TABLE IF NOT EXISTS promotions.promotion_exclusion (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL REFERENCES promotions.promotion(id) ON DELETE CASCADE,
    product_id BIGINT,
    category_id BIGINT,
    user_id BIGINT,
    exclusion_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_exclusion_promo ON promotions.promotion_exclusion(promotion_id);
COMMENT ON TABLE promotions.promotion_exclusion IS 'Products, categories or users excluded from promotions';
