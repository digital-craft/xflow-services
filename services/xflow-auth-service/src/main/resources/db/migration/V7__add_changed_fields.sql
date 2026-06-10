ALTER TABLE users
    ADD COLUMN is_password_changed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_pin_changed BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_users_is_password_changed ON users (is_password_changed);
CREATE INDEX IF NOT EXISTS idx_users_is_pin_changed ON users (is_pin_changed);