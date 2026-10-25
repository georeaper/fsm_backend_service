-- Apply to every tenant database. Safe to execute repeatedly.
ALTER TABLE ticket
    ADD COLUMN IF NOT EXISTS "Status" VARCHAR(50),
    ADD COLUMN IF NOT EXISTS "Type" VARCHAR(100);

ALTER TABLE ticket_history
    ADD COLUMN IF NOT EXISTS "Status" VARCHAR(50),
    ADD COLUMN IF NOT EXISTS "Type" VARCHAR(100);

-- Preserve the meaning of the legacy Active flag for existing technical cases.
UPDATE ticket
SET "Status" = CASE WHEN "Active" = FALSE THEN 'RESOLVED' ELSE 'OPEN' END
WHERE "Status" IS NULL;

UPDATE ticket_history
SET "Status" = CASE WHEN "Active" = FALSE THEN 'RESOLVED' ELSE 'OPEN' END
WHERE "Status" IS NULL;
