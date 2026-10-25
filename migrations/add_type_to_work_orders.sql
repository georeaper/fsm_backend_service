-- Apply to every existing tenant database. Safe to execute repeatedly.
ALTER TABLE fieldreports
    ADD COLUMN IF NOT EXISTS "Type" VARCHAR(100);
