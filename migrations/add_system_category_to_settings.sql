-- Apply to every existing tenant database. Safe to execute repeatedly.
ALTER TABLE settings
    ADD COLUMN IF NOT EXISTS "SystemCategory" VARCHAR(20);

ALTER TABLE settings
    DROP CONSTRAINT IF EXISTS settings_system_category_check;

ALTER TABLE settings
    ADD CONSTRAINT settings_system_category_check
    CHECK ("SystemCategory" IS NULL OR "SystemCategory" IN ('OPEN', 'CLOSED', 'CANCELED'));
