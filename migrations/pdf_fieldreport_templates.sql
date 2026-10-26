-- Apply to every existing tenant database. Preserves all current setting values.
ALTER TABLE "settings" ALTER COLUMN "SettingsValue" TYPE TEXT;
