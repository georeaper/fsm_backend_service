-- Apply to every existing tenant database. Safe to execute repeatedly.
-- Adds priority categories to the existing SystemCategory domain and classifies
-- legacy priorities. Review tenant-specific labels after running the migration.

ALTER TABLE settings
    DROP CONSTRAINT IF EXISTS settings_system_category_check;

ALTER TABLE settings
    ADD CONSTRAINT settings_system_category_check
    CHECK (
        "SystemCategory" IS NULL OR
        "SystemCategory" IN ('OPEN', 'CLOSED', 'CANCELED', 'LOW', 'MEDIUM', 'HIGH')
    );

UPDATE settings
SET "SystemCategory" = CASE
    WHEN LOWER(TRIM("SettingsValue")) IN ('high', 'urgent', 'critical', 'υψηλή', 'υψηλο', 'επείγον', 'κρίσιμο') THEN 'HIGH'
    WHEN LOWER(TRIM("SettingsValue")) IN ('low', 'χαμηλή', 'χαμηλο') THEN 'LOW'
    ELSE 'MEDIUM'
END
WHERE "SettingsKey" IN ('TechnicalCasePriority', 'WorkOrderPriority', 'TaskPriority')
  AND "SystemCategory" IS NULL;
