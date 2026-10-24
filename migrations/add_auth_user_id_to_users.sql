-- Run this migration against every existing tenant database.
-- AuthUserId links a tenant business user to the login account in central_db.

BEGIN;

ALTER TABLE "users"
    ADD COLUMN IF NOT EXISTS "AuthUserId" INTEGER NULL;

CREATE UNIQUE INDEX IF NOT EXISTS "users_AuthUserId_unique"
    ON "users" ("AuthUserId")
    WHERE "AuthUserId" IS NOT NULL;

COMMIT;
