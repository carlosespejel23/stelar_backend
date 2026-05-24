-- =====================================================================
--  V5 — Academic period configuration tables
--  tenant_academic_configs → academic_periods → evaluation_periods
-- =====================================================================

-- ── tenant_academic_configs ───────────────────────────────────────────
CREATE TABLE tenant_academic_configs (
    id                    UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID           NOT NULL REFERENCES tenants(id),
    period_type           VARCHAR(20)    NOT NULL DEFAULT 'SEMESTER',
    grading_scale_min     DECIMAL(5,2)   NOT NULL DEFAULT 0,
    grading_scale_max     DECIMAL(5,2)   NOT NULL DEFAULT 10,
    passing_grade         DECIMAL(5,2)   NOT NULL DEFAULT 6.0,
    attendance_minimum_pct INTEGER        NOT NULL DEFAULT 80,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by            UUID,
    CONSTRAINT uq_tenant_academic_config UNIQUE (tenant_id)
);

-- ── academic_periods ──────────────────────────────────────────────────
CREATE TABLE academic_periods (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID          NOT NULL REFERENCES tenants(id),
    name        VARCHAR(255)  NOT NULL,
    period_type VARCHAR(20)   NOT NULL,
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by  UUID,
    CONSTRAINT uq_academic_period_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_academic_periods_tenant ON academic_periods(tenant_id);
CREATE INDEX idx_academic_periods_active ON academic_periods(tenant_id, active);

-- ── evaluation_periods ────────────────────────────────────────────────
CREATE TABLE evaluation_periods (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID          NOT NULL REFERENCES tenants(id),
    academic_period_id UUID          NOT NULL REFERENCES academic_periods(id) ON DELETE CASCADE,
    name               VARCHAR(100)  NOT NULL,
    weight             DECIMAL(5,2)  NOT NULL,
    sequence_order     INTEGER       NOT NULL,
    start_date         DATE,
    end_date           DATE,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by         UUID
);

CREATE INDEX idx_eval_periods_academic ON evaluation_periods(academic_period_id);
CREATE INDEX idx_eval_periods_tenant ON evaluation_periods(tenant_id);
