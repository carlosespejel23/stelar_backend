-- =====================================================================
--  V7 — Analytics module schema
--  risk_weights → academic_alerts → notification_preferences
--                                → in_app_notifications → reports
-- =====================================================================

-- ── risk_weights ──────────────────────────────────────────────────────
CREATE TABLE risk_weights (
    id                   UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID           NOT NULL REFERENCES tenants(id),
    low_average          DOUBLE PRECISION NOT NULL DEFAULT 0.30,
    negative_trend       DOUBLE PRECISION NOT NULL DEFAULT 0.15,
    low_attendance       DOUBLE PRECISION NOT NULL DEFAULT 0.20,
    consecutive_absences DOUBLE PRECISION NOT NULL DEFAULT 0.10,
    failed_subjects      DOUBLE PRECISION NOT NULL DEFAULT 0.15,
    temporal_urgency     DOUBLE PRECISION NOT NULL DEFAULT 0.10,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by           UUID,
    CONSTRAINT uq_risk_weights_tenant UNIQUE (tenant_id)
);

-- ── academic_alerts ───────────────────────────────────────────────────
CREATE TABLE academic_alerts (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID         NOT NULL REFERENCES tenants(id),
    student_id         UUID         NOT NULL REFERENCES students(id),
    academic_period_id UUID         NOT NULL REFERENCES academic_periods(id),
    risk_level         VARCHAR(20)  NOT NULL,
    risk_score         INTEGER      NOT NULL,
    factor_scores      TEXT,                    -- JSON: {"lowAverage":40, ...}
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    dismissed_by       UUID,
    dismissed_at       TIMESTAMPTZ,
    dismiss_reason     VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         UUID
);

CREATE INDEX idx_alerts_tenant_student ON academic_alerts(tenant_id, student_id);
CREATE INDEX idx_alerts_tenant_status  ON academic_alerts(tenant_id, status);
CREATE INDEX idx_alerts_tenant_level   ON academic_alerts(tenant_id, risk_level);
CREATE INDEX idx_alerts_student_period ON academic_alerts(student_id, academic_period_id, status);

-- ── notification_preferences ──────────────────────────────────────────
CREATE TABLE notification_preferences (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID        NOT NULL REFERENCES tenants(id),
    user_id                 UUID        NOT NULL REFERENCES users(id),
    alert_level_threshold   VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    email_enabled           BOOLEAN     NOT NULL DEFAULT true,
    in_app_enabled          BOOLEAN     NOT NULL DEFAULT true,
    email_frequency         VARCHAR(20) NOT NULL DEFAULT 'DAILY_DIGEST',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID,
    CONSTRAINT uq_notification_pref_tenant_user UNIQUE (tenant_id, user_id)
);

-- ── in_app_notifications ──────────────────────────────────────────────
CREATE TABLE in_app_notifications (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  UUID         NOT NULL REFERENCES tenants(id),
    user_id    UUID         NOT NULL REFERENCES users(id),
    title      VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    severity   VARCHAR(20)  NOT NULL,
    alert_id   UUID         REFERENCES academic_alerts(id),
    is_read    BOOLEAN      NOT NULL DEFAULT false,
    read_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user        ON in_app_notifications(user_id, is_read);
CREATE INDEX idx_notifications_tenant_user ON in_app_notifications(tenant_id, user_id);

-- ── reports ───────────────────────────────────────────────────────────
CREATE TABLE reports (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL REFERENCES tenants(id),
    report_type   VARCHAR(50)  NOT NULL,
    title         VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    parameters    TEXT,                    -- JSON of generation parameters
    file_url      VARCHAR(500),
    generated_at  TIMESTAMPTZ,
    requested_by  UUID         NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    UUID
);

CREATE INDEX idx_reports_tenant        ON reports(tenant_id);
CREATE INDEX idx_reports_tenant_status ON reports(tenant_id, status);
