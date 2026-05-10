-- Initialize Recadero Database Schemas
-- This script is automatically executed on PostgreSQL startup
-- It creates isolated schemas for each module per Modulith architecture

-- Create schemas for each module
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS cart;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS promotions;
CREATE SCHEMA IF NOT EXISTS templates;
CREATE SCHEMA IF NOT EXISTS shared;

-- Grant permissions to recadero user on all schemas
GRANT ALL ON SCHEMA catalog, users, cart, orders, promotions, templates, shared TO recadero;

-- Set default search path for connections
ALTER ROLE recadero SET search_path = public, catalog, users, cart, orders, promotions, templates, shared;

-- Create shared audit schema for logging
CREATE TABLE IF NOT EXISTS shared.audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    operation VARCHAR(50) NOT NULL,  -- CREATE, UPDATE, DELETE
    old_value JSONB,
    new_value JSONB,
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address INET
);

-- Create indexes for audit log
CREATE INDEX idx_audit_log_entity ON shared.audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_changed_at ON shared.audit_log(changed_at DESC);
CREATE INDEX idx_audit_log_operation ON shared.audit_log(operation);

-- Create shared sequences for UUID generation (if needed as fallback)
CREATE SEQUENCE IF NOT EXISTS shared.seq_entity_id
    START WITH 1
    INCREMENT BY 1
    CACHE 20;

-- Grant audit log permissions
GRANT ALL ON shared.audit_log TO recadero;
GRANT USAGE, SELECT ON SEQUENCE shared.seq_entity_id TO recadero;

-- Create performance monitoring table
CREATE TABLE IF NOT EXISTS shared.performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,      -- GET, POST, PUT, DELETE, PATCH
    response_time_ms INT NOT NULL,
    status_code INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID
);

CREATE INDEX idx_performance_metrics_endpoint ON shared.performance_metrics(endpoint, method);
CREATE INDEX idx_performance_metrics_created_at ON shared.performance_metrics(created_at DESC);
CREATE INDEX idx_performance_metrics_status_code ON shared.performance_metrics(status_code);

GRANT ALL ON shared.performance_metrics TO recadero;

-- Output confirmation
\echo 'Recadero database initialized successfully!'
\echo 'Schemas created: catalog, users, cart, orders, promotions, templates, shared'
\echo 'Audit logging and performance metrics tables created in shared schema'
