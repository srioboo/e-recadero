-- Flyway Migration V1: Create Root Schemas
-- This migration creates all module-isolated schemas for the e-recadero backend

-- Create catalog schema for product data
CREATE SCHEMA IF NOT EXISTS catalog;
COMMENT ON SCHEMA catalog IS 'Product catalog module schema - manages products, categories, variants, inventory';

-- Create users schema for user management
CREATE SCHEMA IF NOT EXISTS users;
COMMENT ON SCHEMA users IS 'Users module schema - manages user profiles, addresses, roles, authentication';

-- Create cart schema for shopping cart
CREATE SCHEMA IF NOT EXISTS cart;
COMMENT ON SCHEMA cart IS 'Cart module schema - manages shopping carts, items, promotions applied to cart';

-- Create orders schema for order management
CREATE SCHEMA IF NOT EXISTS orders;
COMMENT ON SCHEMA orders IS 'Orders module schema - manages orders, order items, shipments, payments';

-- Create promotions schema for promotion rules
CREATE SCHEMA IF NOT EXISTS promotions;
COMMENT ON SCHEMA promotions IS 'Promotions module schema - manages promotions, rules, coupon codes, usage tracking';

-- Create templates schema for template management
CREATE SCHEMA IF NOT EXISTS templates;
COMMENT ON SCHEMA templates IS 'Templates module schema - manages templates, blocks, versions, metadata';

-- Create shared schema for cross-module utilities
CREATE SCHEMA IF NOT EXISTS shared;
COMMENT ON SCHEMA shared IS 'Shared module schema - audit logs, performance metrics, common utilities';

-- Grant privileges to recadero user on all schemas
GRANT ALL PRIVILEGES ON SCHEMA catalog TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA users TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA cart TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA orders TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA promotions TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA templates TO recadero;
GRANT ALL PRIVILEGES ON SCHEMA shared TO recadero;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA users GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA cart GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA orders GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA promotions GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA templates GRANT ALL ON TABLES TO recadero;
ALTER DEFAULT PRIVILEGES IN SCHEMA shared GRANT ALL ON TABLES TO recadero;

-- Create shared audit log table
CREATE TABLE IF NOT EXISTS shared.audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id BIGINT NOT NULL,
    operation VARCHAR(50) NOT NULL,
    changes JSONB,
    actor_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at)
);
COMMENT ON TABLE shared.audit_log IS 'Audit trail for sensitive operations across all modules';

-- Create shared performance metrics table
CREATE TABLE IF NOT EXISTS shared.performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(255) NOT NULL,
    value DECIMAL(10, 2),
    unit VARCHAR(50),
    measured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    context JSONB,
    INDEX idx_metric_name (metric_name),
    INDEX idx_measured_at (measured_at)
);
COMMENT ON TABLE shared.performance_metrics IS 'Performance tracking across all modules';

-- Create Flyway schema history table version
CREATE TABLE IF NOT EXISTS shared.flyway_schema_history (
    installed_rank SERIAL NOT NULL,
    version VARCHAR(50),
    description VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);
COMMENT ON TABLE shared.flyway_schema_history IS 'Flyway migration history tracking';
