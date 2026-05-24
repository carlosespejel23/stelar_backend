-- Add profession column to users table
-- Stores the academic title/profession of teachers (LIC, ING, MTRO, MTRA, DR, DRA, PROF, PROFA, MC, ESP)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profession VARCHAR(10);
