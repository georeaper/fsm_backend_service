-- Apply to every existing tenant database. Safe to execute repeatedly.
ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS "Type" VARCHAR(100);
