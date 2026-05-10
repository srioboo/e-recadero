-- Flyway Migration V3: Create Users Module Tables
-- This migration creates all tables for the users module

-- User Role table
CREATE TABLE IF NOT EXISTS users.user_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE users.user_role IS 'User roles for authorization (ADMIN, CUSTOMER, VENDOR, SUPPORT)';

INSERT INTO users.user_role (role_name, description) VALUES
    ('ADMIN', 'Administrator with full system access'),
    ('CUSTOMER', 'Regular customer'),
    ('VENDOR', 'Product vendor/seller'),
    ('SUPPORT', 'Customer support staff')
ON CONFLICT DO NOTHING;

-- User table
CREATE TABLE IF NOT EXISTS users."user" (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_user_email ON users."user"(email);
CREATE INDEX idx_user_username ON users."user"(username);
CREATE INDEX idx_user_active ON users."user"(is_active);
COMMENT ON TABLE users."user" IS 'User accounts with authentication credentials and verification status';

-- User Role Assignment table
CREATE TABLE IF NOT EXISTS users.user_role_assignment (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users."user"(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES users.user_role(id),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(255),
    UNIQUE(user_id, role_id)
);
CREATE INDEX idx_role_assignment_user ON users.user_role_assignment(user_id);
CREATE INDEX idx_role_assignment_role ON users.user_role_assignment(role_id);
COMMENT ON TABLE users.user_role_assignment IS 'Many-to-many relationship between users and roles';

-- User Profile table
CREATE TABLE IF NOT EXISTS users.user_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users."user"(id) ON DELETE CASCADE,
    profile_picture_url VARCHAR(1000),
    bio TEXT,
    date_of_birth DATE,
    gender VARCHAR(50),
    preferred_language VARCHAR(10) DEFAULT 'es',
    preferred_currency VARCHAR(10) DEFAULT 'COP',
    notification_preferences JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE users.user_profile IS 'Extended user profile information and preferences';

-- Address table
CREATE TABLE IF NOT EXISTS users.address (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users."user"(id) ON DELETE CASCADE,
    address_type VARCHAR(50),
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    street_address VARCHAR(255) NOT NULL,
    apartment_suite VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) DEFAULT 'CO',
    is_default BOOLEAN DEFAULT false,
    is_billing_address BOOLEAN DEFAULT false,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_address_user ON users.address(user_id);
CREATE INDEX idx_address_default ON users.address(user_id, is_default);
COMMENT ON TABLE users.address IS 'User addresses for shipping and billing';

-- User Authentication History table
CREATE TABLE IF NOT EXISTS users.auth_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users."user"(id) ON DELETE CASCADE,
    login_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    logout_timestamp TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    authentication_method VARCHAR(100),
    success BOOLEAN DEFAULT true,
    failure_reason VARCHAR(255)
);
CREATE INDEX idx_auth_user ON users.auth_history(user_id);
CREATE INDEX idx_auth_timestamp ON users.auth_history(login_timestamp);
COMMENT ON TABLE users.auth_history IS 'Login history and authentication tracking for security';

-- Password Reset Token table
CREATE TABLE IF NOT EXISTS users.password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users."user"(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reset_token ON users.password_reset_token(token);
CREATE INDEX idx_reset_user ON users.password_reset_token(user_id);
COMMENT ON TABLE users.password_reset_token IS 'Password reset tokens with expiration';

-- Email Verification Token table
CREATE TABLE IF NOT EXISTS users.email_verification_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users."user"(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_verify_token ON users.email_verification_token(token);
CREATE INDEX idx_verify_user ON users.email_verification_token(user_id);
COMMENT ON TABLE users.email_verification_token IS 'Email verification tokens for new registrations';
