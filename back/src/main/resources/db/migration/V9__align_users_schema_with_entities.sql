-- Align users schema with the current JPA entity model

ALTER TABLE IF EXISTS users."user"
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_user_status ON users."user"(status);

-- first_name/last_name/phone_number moved conceptually to user_profile (kept
-- on "user" too as unused legacy columns rather than a destructive drop).
ALTER TABLE IF EXISTS users.user_profile
  ADD COLUMN IF NOT EXISTS first_name VARCHAR(100),
  ADD COLUMN IF NOT EXISTS last_name VARCHAR(100),
  ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
  ADD COLUMN IF NOT EXISTS newsletter_subscribed BOOLEAN NOT NULL DEFAULT false;

-- Denormalize role_name onto the assignment row (simpler read path than joining
-- the user_role lookup table for every authorization check); role_id is kept
-- for backward compatibility but is no longer required.
ALTER TABLE IF EXISTS users.user_role_assignment
  ADD COLUMN IF NOT EXISTS role_name VARCHAR(20),
  ALTER COLUMN role_id DROP NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_role_assignment_user_role_name
  ON users.user_role_assignment(user_id, role_name);

INSERT INTO users.user_role (role_name, description) VALUES
    ('ANALYST', 'Business intelligence analyst with read-only access')
ON CONFLICT DO NOTHING;
