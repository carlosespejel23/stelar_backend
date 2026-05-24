-- =============================================================
-- V4 — Restructure users for multi-tenancy
-- Creates user_tenants join table, migrates data from users,
-- removes tenant-specific columns from users, makes email global.
-- =============================================================

-- 1. Create user_tenants join table
CREATE TABLE user_tenants (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    role_id     UUID                 REFERENCES roles(id)  ON DELETE SET NULL,
    active      BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_user_tenant UNIQUE (user_id, tenant_id)
);

CREATE INDEX idx_user_tenants_tenant_id ON user_tenants(tenant_id);
CREATE INDEX idx_user_tenants_user_id   ON user_tenants(user_id);

-- 2. Migrate existing user data: copy per-tenant fields into user_tenants
INSERT INTO user_tenants (id, tenant_id, user_id, role_id, active, created_at, updated_at, created_by)
SELECT gen_random_uuid(), tenant_id, id, role_id, active, created_at, updated_at, created_by
FROM users;

-- 3. Drop the old (tenant_id, email) unique constraint before altering columns
ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_users_tenant_email;

-- 4. Remove columns that moved to user_tenants
ALTER TABLE users DROP COLUMN IF EXISTS tenant_id;
ALTER TABLE users DROP COLUMN IF EXISTS role_id;
ALTER TABLE users DROP COLUMN IF EXISTS active;

-- 5. Add global unique constraint on email
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
