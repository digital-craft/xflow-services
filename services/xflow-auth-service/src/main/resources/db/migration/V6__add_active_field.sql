ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_users_is_active ON users (is_active);