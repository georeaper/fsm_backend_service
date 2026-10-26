-- Apply to the central database. Existing tenants default to UTC until configured.
ALTER TABLE "companies" ADD COLUMN IF NOT EXISTS "time_zone" VARCHAR(64) NOT NULL DEFAULT 'UTC';
