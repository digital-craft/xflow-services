-- Toutes les tables dans le schéma isolé 'auth'
SET search_path = auth;

CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','OPERATOR','PARTICIPANT')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login_at   TIMESTAMPTZ
);

CREATE TABLE anonymous_tokens (
    uuid        UUID        PRIMARY KEY,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     UUID        REFERENCES users(id),
    event       VARCHAR(50) NOT NULL,  -- LOGIN_SUCCESS, LOGIN_FAILED, LOCKED
    client_ip   VARCHAR(45),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index sur les colonnes les plus requêtées
CREATE INDEX idx_users_email         ON users (email);
CREATE INDEX idx_anon_expires        ON anonymous_tokens (expires_at);
CREATE INDEX idx_audit_user_occurred ON audit_logs (user_id, occurred_at DESC);

-- Seed admin de développement (mot de passe : changeme → bcrypt)
INSERT INTO users (email, password_hash, role)
VALUES ('admin@xflow.io',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeAHQsONQT9FV.kiy',
        'ADMIN');