-- Apply to every existing tenant database. Additive; existing jobs remain valid.
ALTER TABLE "notification_jobs" ADD COLUMN IF NOT EXISTS "RenderedTitle" TEXT NULL;
ALTER TABLE "notification_jobs" ADD COLUMN IF NOT EXISTS "RenderedMessage" TEXT NULL;
