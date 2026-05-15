CREATE TABLE IF NOT EXISTS refresh_token (
    id              UUID  PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(255) NOT NULL UNIQUE,
    user_id         UUID NOT NULL,
    expiry_at       TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_refresh_token_on_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_value ON refresh_token (token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_token (expiry_at);