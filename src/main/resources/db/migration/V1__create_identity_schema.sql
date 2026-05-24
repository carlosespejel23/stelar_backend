-- =============================================================
-- V1 — Identity Module Schema
-- Tablas: tenants, roles, permissions, users, tokens
-- =============================================================

-- -----------------------------------------------------------------
-- TENANTS (raíz — sin tenant_id)
-- -----------------------------------------------------------------
CREATE TABLE tenants (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(50)  NOT NULL,   -- identificador legible, ej: "colegio-juarez"
    description VARCHAR(500),
    logo_url    VARCHAR(500),
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  UUID,   -- userId que creó el tenant (null = sistema)

    CONSTRAINT uq_tenants_slug UNIQUE (slug)
);

CREATE INDEX idx_tenants_slug ON tenants (slug);

-- -----------------------------------------------------------------
-- ROLES (por tenant)
-- -----------------------------------------------------------------
CREATE TABLE roles (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(300),
    system_role BOOLEAN      NOT NULL DEFAULT false,  -- true = rol creado por Stellar al crear el tenant
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  UUID,   -- userId que creó el rol (null = sistema)

    CONSTRAINT uq_roles_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_roles_tenant_id ON roles (tenant_id);

-- -----------------------------------------------------------------
-- PERMISSIONS (catálogo global — sin tenant_id, seed en V2)
-- -----------------------------------------------------------------
CREATE TABLE permissions (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(150) NOT NULL,
    description VARCHAR(300),
    category    VARCHAR(50)  NOT NULL,
    action      VARCHAR(50),
    resource    VARCHAR(100),
    type        VARCHAR(20)  NOT NULL DEFAULT 'BACKEND',  -- BACKEND | FRONTEND | FULL_STACK
    active      BOOLEAN      NOT NULL DEFAULT true,

    CONSTRAINT uq_permissions_code UNIQUE (code),
    CONSTRAINT chk_permissions_type CHECK (type IN ('BACKEND', 'FRONTEND', 'FULL_STACK'))
);

-- -----------------------------------------------------------------
-- ROLE_PERMISSIONS (join: rol ↔ permiso)
-- -----------------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,

    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);

-- -----------------------------------------------------------------
-- USERS (por tenant)
-- -----------------------------------------------------------------
CREATE TABLE users (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    role_id             UUID                 REFERENCES roles (id) ON DELETE SET NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(200) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(500),
    email_verified      BOOLEAN      NOT NULL DEFAULT false,
    active              BOOLEAN      NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          UUID,   -- userId que creó este registro (null = auto-registro)

    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant_id  ON users (tenant_id);
CREATE INDEX idx_users_email      ON users (email);
CREATE INDEX idx_users_role_id    ON users (role_id);

-- Enlace bidireccional: tenant conoce a su owner
ALTER TABLE tenants ADD COLUMN owner_id UUID REFERENCES users (id) ON DELETE SET NULL;

-- -----------------------------------------------------------------
-- REFRESH_TOKENS
-- -----------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500) NOT NULL,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    tenant_id   UUID         NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    revoked     BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user_id   ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token     ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_expires   ON refresh_tokens (expires_at) WHERE revoked = false;

-- -----------------------------------------------------------------
-- EMAIL_VERIFICATION_TOKENS
-- -----------------------------------------------------------------
CREATE TABLE email_verification_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500) NOT NULL,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    email       VARCHAR(200) NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_email_verification_token UNIQUE (token)
);

CREATE INDEX idx_email_verification_user_id ON email_verification_tokens (user_id);
CREATE INDEX idx_email_verification_token   ON email_verification_tokens (token);

-- -----------------------------------------------------------------
-- PASSWORD_RESET_TOKENS
-- -----------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500) NOT NULL,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    email       VARCHAR(200) NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_password_reset_token UNIQUE (token)
);

CREATE INDEX idx_password_reset_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_token   ON password_reset_tokens (token);
