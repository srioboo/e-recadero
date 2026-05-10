-- Flyway Migration V5: Create Orders Module Tables
-- This migration creates all tables for the orders module

-- Order Status table
CREATE TABLE IF NOT EXISTS orders.order_status (
    id SERIAL PRIMARY KEY,
    status_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_terminal_state BOOLEAN DEFAULT false
);
INSERT INTO orders.order_status (status_name, description, is_terminal_state) VALUES
    ('PENDING', 'Order created, awaiting confirmation', false),
    ('CONFIRMED', 'Order confirmed by customer', false),
    ('PROCESSING', 'Order being prepared for shipment', false),
    ('SHIPPED', 'Order shipped to customer', false),
    ('DELIVERED', 'Order delivered', true),
    ('CANCELLED', 'Order cancelled', true),
    ('RETURNED', 'Order returned by customer', true),
    ('REFUNDED', 'Order refunded', true)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS orders."order" (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status_id INTEGER NOT NULL REFERENCES orders.order_status(id),
    billing_address_id BIGINT,
    shipping_address_id BIGINT,
    subtotal_amount DECIMAL(12, 2) NOT NULL,
    tax_amount DECIMAL(12, 2) NOT NULL,
    shipping_amount DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(1000),
    confirmation_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_order_user ON orders."order"(user_id);
CREATE INDEX idx_order_number ON orders."order"(order_number);
CREATE INDEX idx_order_status ON orders."order"(status_id);
CREATE INDEX idx_order_created ON orders."order"(created_at);
COMMENT ON TABLE orders."order" IS 'Customer orders with pricing and status tracking';

-- Order Item table
CREATE TABLE IF NOT EXISTS orders.order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    sku VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_item_order ON orders.order_item(order_id);
CREATE INDEX idx_order_item_product ON orders.order_item(product_id);
COMMENT ON TABLE orders.order_item IS 'Individual line items in orders';

-- Order Shipment table
CREATE TABLE IF NOT EXISTS orders.order_shipment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    tracking_number VARCHAR(100) UNIQUE,
    carrier_name VARCHAR(100),
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_shipment_order ON orders.order_shipment(order_id);
CREATE INDEX idx_shipment_tracking ON orders.order_shipment(tracking_number);
COMMENT ON TABLE orders.order_shipment IS 'Shipment tracking information';

-- Order Payment table
CREATE TABLE IF NOT EXISTS orders.order_payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    payment_method VARCHAR(100) NOT NULL,
    payment_gateway_id VARCHAR(255),
    transaction_id VARCHAR(255),
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'COP',
    payment_status VARCHAR(50) NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_payment_order ON orders.order_payment(order_id);
CREATE INDEX idx_payment_status ON orders.order_payment(payment_status);
COMMENT ON TABLE orders.order_payment IS 'Payment records for orders';

-- Money table for detailed payment transactions
CREATE TABLE IF NOT EXISTS orders.order_transaction (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    payment_id BIGINT REFERENCES orders.order_payment(id) ON DELETE SET NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'COP',
    external_transaction_id VARCHAR(255),
    response_code VARCHAR(50),
    response_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_transaction_order ON orders.order_transaction(order_id);
COMMENT ON TABLE orders.order_transaction IS 'Payment transaction details and history';

-- Order Return table
CREATE TABLE IF NOT EXISTS orders.order_return (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    reason VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING',
    refund_amount DECIMAL(12, 2),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_return_order ON orders.order_return(order_id);
COMMENT ON TABLE orders.order_return IS 'Return/RMA tracking for orders';

-- Order Event Log table
CREATE TABLE IF NOT EXISTS orders.order_event_log (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders."order"(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    event_description VARCHAR(500),
    event_data JSONB,
    actor_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_event_order ON orders.order_event_log(order_id);
COMMENT ON TABLE orders.order_event_log IS 'Audit trail of events on orders (status changes, payments, etc.)';
