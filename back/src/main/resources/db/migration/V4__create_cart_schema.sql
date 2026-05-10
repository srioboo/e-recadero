-- Flyway Migration V4: Create Cart Module Tables
-- This migration creates all tables for the cart module

-- Shopping Cart table
CREATE TABLE IF NOT EXISTS cart.shopping_cart (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    total_items INTEGER DEFAULT 0,
    subtotal_amount DECIMAL(12, 2) DEFAULT 0,
    tax_amount DECIMAL(12, 2) DEFAULT 0,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0,
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cart_user ON cart.shopping_cart(user_id);
CREATE INDEX idx_cart_active ON cart.shopping_cart(is_active);
COMMENT ON TABLE cart.shopping_cart IS 'Shopping carts for customers';

-- Cart Item table
CREATE TABLE IF NOT EXISTS cart.cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGSERIAL NOT NULL REFERENCES cart.shopping_cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cart_item_cart ON cart.cart_item(cart_id);
CREATE INDEX idx_cart_item_product ON cart.cart_item(product_id);
CREATE INDEX idx_cart_item_variant ON cart.cart_item(variant_id);
COMMENT ON TABLE cart.cart_item IS 'Items in shopping carts with pricing';

-- Cart Promotion table
CREATE TABLE IF NOT EXISTS cart.cart_promotion (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart.shopping_cart(id) ON DELETE CASCADE,
    promotion_id BIGINT,
    coupon_code VARCHAR(100),
    discount_type VARCHAR(50),
    discount_value DECIMAL(10, 2),
    discount_percentage DECIMAL(5, 2),
    applied_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    applied_by VARCHAR(255)
);
CREATE INDEX idx_cart_promo_cart ON cart.cart_promotion(cart_id);
COMMENT ON TABLE cart.cart_promotion IS 'Promotions and coupons applied to carts';

-- Cart Saved Item table (wishlist functionality)
CREATE TABLE IF NOT EXISTS cart.cart_saved_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    notes VARCHAR(500),
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id, variant_id)
);
CREATE INDEX idx_saved_user ON cart.cart_saved_item(user_id);
CREATE INDEX idx_saved_product ON cart.cart_saved_item(product_id);
COMMENT ON TABLE cart.cart_saved_item IS 'Saved items/wishlist for users';

-- Cart Abandonment Tracking table
CREATE TABLE IF NOT EXISTS cart.cart_abandonment (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart.shopping_cart(id) ON DELETE CASCADE,
    abandoned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recovery_email_sent_at TIMESTAMP WITH TIME ZONE,
    cart_snapshot JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_abandoned_cart ON cart.cart_abandonment(abandoned_at);
COMMENT ON TABLE cart.cart_abandonment IS 'Abandoned cart tracking for recovery campaigns';
