-- Work-order status already exists as fieldreports."ReportStatus".
-- Apply this migration to every existing tenant database.
ALTER TABLE fieldreports
    ADD COLUMN IF NOT EXISTS "Priority" VARCHAR(50);
