-- Align orders schema with the current JPA entity model.
-- Denormalizes status onto the order row directly (matching the same
-- role_name simplification made for users.user_role_assignment in V9)
-- rather than joining through order_status for every read.

ALTER TABLE IF EXISTS orders."order"
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ALTER COLUMN status_id DROP NOT NULL,
  ADD COLUMN IF NOT EXISTS confirmed_date TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS shipped_date TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS delivered_date TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS shipping_method_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_order_status_denorm ON orders."order"(status);

ALTER TABLE IF EXISTS orders.order_item
  ALTER COLUMN product_id DROP NOT NULL,
  ADD COLUMN IF NOT EXISTS line_discount DECIMAL(10, 2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);

ALTER TABLE IF EXISTS orders.order_shipment
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Tracking event history for a shipment (carrier webhook updates append here).
CREATE TABLE IF NOT EXISTS orders.order_shipment_event (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES orders.order_shipment(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    location VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_shipment_event_shipment ON orders.order_shipment_event(shipment_id);

ALTER TABLE IF EXISTS orders.order_return
  ADD COLUMN IF NOT EXISTS order_item_id BIGINT REFERENCES orders.order_item(id),
  ADD COLUMN IF NOT EXISTS description VARCHAR(1000);
