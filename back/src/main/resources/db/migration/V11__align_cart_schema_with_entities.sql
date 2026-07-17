-- Align cart schema with the current JPA entity model

ALTER TABLE IF EXISTS cart.shopping_cart
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS checkout_token VARCHAR(255),
  ADD COLUMN IF NOT EXISTS billing_address_id BIGINT,
  ADD COLUMN IF NOT EXISTS shipping_address_id BIGINT,
  ADD COLUMN IF NOT EXISTS shipping_method_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_cart_status ON cart.shopping_cart(status);

-- One ACTIVE cart per user (partial unique index, since status varies).
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_active_per_user
  ON cart.shopping_cart(user_id) WHERE status = 'ACTIVE';

-- product_id is superseded by variant_id (the actual sellable unit); kept
-- nullable rather than dropped to avoid a destructive change.
ALTER TABLE IF EXISTS cart.cart_item
  ALTER COLUMN product_id DROP NOT NULL,
  ADD COLUMN IF NOT EXISTS discount_applied DECIMAL(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS cart.cart_promotion
  ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0;

-- Inventory reservations tied to a cart item, released on removal/expiry.
CREATE TABLE IF NOT EXISTS cart.reservation (
    id BIGSERIAL PRIMARY KEY,
    cart_item_id BIGINT NOT NULL REFERENCES cart.cart_item(id) ON DELETE CASCADE,
    product_variant_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_reservation_cart_item ON cart.reservation(cart_item_id);
CREATE INDEX IF NOT EXISTS idx_reservation_variant ON cart.reservation(product_variant_id);
CREATE INDEX IF NOT EXISTS idx_reservation_expires ON cart.reservation(expires_at);
COMMENT ON TABLE cart.reservation IS 'Inventory reservations held against a cart item until checkout or expiry';
