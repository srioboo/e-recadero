-- Adds indexes documented in specs/002-backend-ecommerce/data-model.md that
-- were never actually created (found during the T185 static index review).
-- Additive only; no existing index/column is touched.
--
-- CAUTION: the two UNIQUE indexes below (uq_inventory_variant_warehouse,
-- uq_cart_item_cart_variant) will fail to create if any environment this
-- runs against already has duplicate rows for those column pairs. Not
-- verified against live data (no running database in the environment this
-- migration was authored in) — dedupe first if that's a possibility before
-- running this against a populated database.

-- Catalog
CREATE INDEX IF NOT EXISTS idx_product_status ON catalog.product(status);
CREATE INDEX IF NOT EXISTS idx_product_category_status ON catalog.product(category_id, status);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON catalog.inventory(warehouse_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_variant_warehouse
  ON catalog.inventory(variant_id, warehouse_id);

-- Users
CREATE INDEX IF NOT EXISTS idx_user_status_created ON users."user"(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_address_user_type ON users.address(user_id, address_type);
CREATE INDEX IF NOT EXISTS idx_role_assignment_role_name ON users.user_role_assignment(role_name);

-- Cart
CREATE INDEX IF NOT EXISTS idx_cart_expires ON cart.shopping_cart(expires_at);
CREATE INDEX IF NOT EXISTS idx_cart_session ON cart.shopping_cart(session_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_item_cart_variant ON cart.cart_item(cart_id, variant_id);
CREATE INDEX IF NOT EXISTS idx_cart_promo_promotion ON cart.cart_promotion(promotion_id);
CREATE INDEX IF NOT EXISTS idx_cart_promo_coupon_code ON cart.cart_promotion(coupon_code);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_promo_cart_promotion
  ON cart.cart_promotion(cart_id, promotion_id);

-- Orders
CREATE INDEX IF NOT EXISTS idx_order_confirmed_date ON orders."order"(confirmed_date);
CREATE INDEX IF NOT EXISTS idx_order_user_status ON orders."order"(user_id, status);
CREATE INDEX IF NOT EXISTS idx_order_status_created ON orders."order"(status, created_at DESC);

-- Promotions
CREATE INDEX IF NOT EXISTS idx_promotion_priority ON promotions.promotion(priority DESC);
CREATE INDEX IF NOT EXISTS idx_promo_rule_promo_type ON promotions.promotion_rule(promotion_id, rule_type);
CREATE INDEX IF NOT EXISTS idx_promo_usage_order ON promotions.promotion_usage(order_id);
CREATE INDEX IF NOT EXISTS idx_promo_usage_coupon_code ON promotions.promotion_usage(coupon_code_id);
CREATE INDEX IF NOT EXISTS idx_promo_usage_promo_used_at
  ON promotions.promotion_usage(promotion_id, used_at DESC);
CREATE INDEX IF NOT EXISTS idx_promo_usage_user_promo
  ON promotions.promotion_usage(user_id, promotion_id);

-- Templates
CREATE INDEX IF NOT EXISTS idx_template_created_by ON templates.template(created_by);
CREATE INDEX IF NOT EXISTS idx_template_status_type ON templates.template(status, type);
