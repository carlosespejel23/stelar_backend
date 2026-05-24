-- =====================================================================
--  V6 — Restructure academic tables for period-based model
--  - Add academic_period_id to groups
--  - Rename assistance → attendances, migrate via enrollment
--  - Restructure grades: enrollment_id + evaluation_period_id
-- =====================================================================

-- ── groups: add academic_period_id (nullable) ─────────────────────────
ALTER TABLE groups ADD COLUMN academic_period_id UUID REFERENCES academic_periods(id);
CREATE INDEX idx_groups_academic_period ON groups(academic_period_id);

-- ── assistance → attendances ──────────────────────────────────────────
ALTER TABLE assistance RENAME TO attendances;

-- Rename notes → justification_reason
ALTER TABLE attendances RENAME COLUMN notes TO justification_reason;

-- Add new columns
ALTER TABLE attendances ADD COLUMN enrollment_id UUID REFERENCES enrollments(id);
ALTER TABLE attendances ADD COLUMN recorded_by   UUID;

-- Migrate: find enrollment from student+subject+group match
UPDATE attendances a
SET enrollment_id = e.id
FROM enrollments e
WHERE e.student_id = a.student_id
  AND e.subject_id = a.subject_id
  AND e.group_id   = a.group_id
  AND e.tenant_id  = a.tenant_id;

-- Remove rows that could not be matched to an enrollment
DELETE FROM attendances WHERE enrollment_id IS NULL;

-- Best-effort: set recorded_by from created_by
UPDATE attendances SET recorded_by = created_by WHERE recorded_by IS NULL AND created_by IS NOT NULL;
UPDATE attendances SET recorded_by = '00000000-0000-0000-0000-000000000000' WHERE recorded_by IS NULL;

-- Enforce NOT NULL now that all rows are migrated
ALTER TABLE attendances ALTER COLUMN enrollment_id SET NOT NULL;
ALTER TABLE attendances ALTER COLUMN recorded_by   SET NOT NULL;

-- Drop old unique constraint and stale columns
ALTER TABLE attendances DROP CONSTRAINT IF EXISTS uq_assistance;
ALTER TABLE attendances DROP COLUMN IF EXISTS student_id;
ALTER TABLE attendances DROP COLUMN IF EXISTS subject_id;
ALTER TABLE attendances DROP COLUMN IF EXISTS group_id;

-- New unique constraint: one record per enrollment per day
ALTER TABLE attendances ADD CONSTRAINT uq_attendance UNIQUE (enrollment_id, date);

-- Replace old indexes with new ones
DROP INDEX IF EXISTS idx_assistance_student;
DROP INDEX IF EXISTS idx_assistance_subject;
DROP INDEX IF EXISTS idx_assistance_group;
DROP INDEX IF EXISTS idx_assistance_tenant;

CREATE INDEX idx_attendances_tenant     ON attendances(tenant_id);
CREATE INDEX idx_attendances_enrollment ON attendances(enrollment_id);
CREATE INDEX idx_attendances_date       ON attendances(enrollment_id, date);

-- ── grades: add enrollment_id + evaluation_period_id ─────────────────
-- First add nullable columns (before dropping the old ones)
ALTER TABLE grades ADD COLUMN enrollment_id       UUID REFERENCES enrollments(id);
ALTER TABLE grades ADD COLUMN evaluation_period_id UUID REFERENCES evaluation_periods(id);
ALTER TABLE grades ADD COLUMN recorded_by          UUID;

-- Rename notes → remarks
ALTER TABLE grades RENAME COLUMN notes TO remarks;

-- Clear all existing grade data: evaluation_period_id cannot be auto-migrated
-- (there are no evaluation_periods yet to map the old GradePeriod enum values to)
TRUNCATE TABLE grades;

-- Drop old unique constraint and stale columns
ALTER TABLE grades DROP CONSTRAINT IF EXISTS uq_grade;
ALTER TABLE grades DROP COLUMN IF EXISTS student_id;
ALTER TABLE grades DROP COLUMN IF EXISTS subject_id;
ALTER TABLE grades DROP COLUMN IF EXISTS group_id;
ALTER TABLE grades DROP COLUMN IF EXISTS period;

-- Enforce NOT NULL on new columns
ALTER TABLE grades ALTER COLUMN enrollment_id        SET NOT NULL;
ALTER TABLE grades ALTER COLUMN evaluation_period_id SET NOT NULL;
ALTER TABLE grades ALTER COLUMN recorded_by          SET NOT NULL;

-- New unique constraint: one grade per enrollment per evaluation period
ALTER TABLE grades ADD CONSTRAINT uq_grade_enrollment_period UNIQUE (enrollment_id, evaluation_period_id);

-- Replace old indexes with new ones
DROP INDEX IF EXISTS idx_grades_student;
DROP INDEX IF EXISTS idx_grades_subject;
DROP INDEX IF EXISTS idx_grades_group;

CREATE INDEX idx_grades_enrollment  ON grades(enrollment_id);
CREATE INDEX idx_grades_eval_period ON grades(evaluation_period_id);
