-- =====================================================================
--  V3 — Invitations
-- =====================================================================

CREATE TABLE invitations (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    invited_email       VARCHAR(200) NOT NULL,
    type                VARCHAR(20)  NOT NULL,           -- EXTERNAL | INTERNAL
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING | ACCEPTED | REVOKED
    token               VARCHAR(500) NOT NULL,
    role_id             UUID         REFERENCES roles(id) ON DELETE SET NULL,
    invited_by_user_id  UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at          TIMESTAMPTZ  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          UUID,

    CONSTRAINT uq_invitations_token  UNIQUE (token),
    CONSTRAINT chk_invitation_type   CHECK (type   IN ('EXTERNAL', 'INTERNAL')),
    CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED'))
);

CREATE INDEX idx_invitations_tenant ON invitations (tenant_id);
CREATE INDEX idx_invitations_token  ON invitations (token);
CREATE INDEX idx_invitations_email  ON invitations (tenant_id, invited_email);
