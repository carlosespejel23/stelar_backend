-- =====================================================================
--  V2 — Academic module schema
--  Groups → Subjects → Students → Enrollments → Assistance → Grades
-- =====================================================================

-- ── groups ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS groups (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    name            VARCHAR(100) NOT NULL,
    level           VARCHAR(50),
    school_year     VARCHAR(20),
    teacher_id      UUID,                        -- FK to users.id (cross-module)
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_groups_tenant_name_year UNIQUE (tenant_id, name, school_year)
);
CREATE INDEX idx_groups_tenant ON groups (tenant_id);

-- ── subjects ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS subjects (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(300),
    teacher_id      UUID,                        -- FK to users.id (cross-module)
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_subjects_tenant_name UNIQUE (tenant_id, name)
);
CREATE INDEX idx_subjects_tenant ON subjects (tenant_id);

-- ── students ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    student_code    VARCHAR(50),
    email           VARCHAR(200),
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_students_tenant_code UNIQUE (tenant_id, student_code)
);
CREATE INDEX idx_students_tenant ON students (tenant_id);

-- ── enrollments ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS enrollments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    student_id      UUID        NOT NULL REFERENCES students(id),
    group_id        UUID        NOT NULL REFERENCES groups(id),
    subject_id      UUID        NOT NULL REFERENCES subjects(id),
    school_year     VARCHAR(20) NOT NULL,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_enrollment UNIQUE (tenant_id, student_id, group_id, subject_id)
);
CREATE INDEX idx_enrollments_tenant   ON enrollments (tenant_id);
CREATE INDEX idx_enrollments_student  ON enrollments (tenant_id, student_id);
CREATE INDEX idx_enrollments_group    ON enrollments (tenant_id, group_id);
CREATE INDEX idx_enrollments_subject  ON enrollments (tenant_id, subject_id);

-- ── assistance ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS assistance (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL,
    student_id      UUID        NOT NULL REFERENCES students(id),
    subject_id      UUID        NOT NULL REFERENCES subjects(id),
    group_id        UUID        NOT NULL REFERENCES groups(id),
    date            DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL,        -- PRESENT | ABSENT | LATE | JUSTIFIED
    notes           VARCHAR(300),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_assistance UNIQUE (tenant_id, student_id, subject_id, date)
);
CREATE INDEX idx_assistance_tenant   ON assistance (tenant_id);
CREATE INDEX idx_assistance_student  ON assistance (tenant_id, student_id);
CREATE INDEX idx_assistance_subject  ON assistance (tenant_id, subject_id);
CREATE INDEX idx_assistance_group    ON assistance (tenant_id, group_id);

-- ── grades ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS grades (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID            NOT NULL,
    student_id      UUID            NOT NULL REFERENCES students(id),
    subject_id      UUID            NOT NULL REFERENCES subjects(id),
    group_id        UUID            NOT NULL REFERENCES groups(id),
    period          VARCHAR(20)     NOT NULL,    -- PARCIAL_1 | PARCIAL_2 | PARCIAL_3 | FINAL
    score           NUMERIC(5, 2)   NOT NULL,
    notes           VARCHAR(300),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    CONSTRAINT uq_grade UNIQUE (tenant_id, student_id, subject_id, period)
);
CREATE INDEX idx_grades_tenant   ON grades (tenant_id);
CREATE INDEX idx_grades_student  ON grades (tenant_id, student_id);
CREATE INDEX idx_grades_subject  ON grades (tenant_id, subject_id);
CREATE INDEX idx_grades_group    ON grades (tenant_id, group_id);
