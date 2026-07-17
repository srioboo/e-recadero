-- Align promotions schema with the current JPA entity model.
-- Denormalizes status onto the promotion row directly (same simplification
-- made for users.user_role_assignment in V9 and orders."order" in V12).

ALTER TABLE IF EXISTS promotions.promotion
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  ADD COLUMN IF NOT EXISTS max_discount_amount DECIMAL(12, 2),
  ADD COLUMN IF NOT EXISTS minimum_order_amount DECIMAL(12, 2),
  ADD COLUMN IF NOT EXISTS priority INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS rule_match_mode VARCHAR(10) NOT NULL DEFAULT 'ALL',
  ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_promo_status ON promotions.promotion(status);

ALTER TABLE IF EXISTS promotions.promotion_usage
  ADD COLUMN IF NOT EXISTS coupon_code_id BIGINT REFERENCES promotions.coupon_code(id);
