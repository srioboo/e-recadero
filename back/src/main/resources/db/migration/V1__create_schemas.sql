-- Flyway Migration V1: Create Schemas
-- This migration creates the module-isolated schemas for the e-recadero backend

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
