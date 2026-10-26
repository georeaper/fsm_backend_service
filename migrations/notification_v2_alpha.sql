-- Apply to every tenant database. Additive Alpha migration; no legacy column is removed.
ALTER TABLE "fieldreports" ADD COLUMN IF NOT EXISTS "ScheduledStart" VARCHAR(255);
ALTER TABLE "fieldreports" ADD COLUMN IF NOT EXISTS "ScheduledEnd" VARCHAR(255);

ALTER TABLE "notifications" ADD COLUMN IF NOT EXISTS "JobId" VARCHAR(36);
ALTER TABLE "notifications" ADD COLUMN IF NOT EXISTS "IdempotencyKey" VARCHAR(255);
ALTER TABLE "notifications" ADD COLUMN IF NOT EXISTS "CreatedAtEpochMs" BIGINT;
ALTER TABLE "notifications" ADD COLUMN IF NOT EXISTS "ReadAtEpochMs" BIGINT;

CREATE TABLE IF NOT EXISTS "notification_rules" (
    "RuleId" VARCHAR(36) PRIMARY KEY,
    "Name" VARCHAR(150) NOT NULL,
    "EntityType" VARCHAR(40) NOT NULL,
    "TriggerType" VARCHAR(20) NOT NULL,
    "EventType" VARCHAR(40),
    "DateField" VARCHAR(50),
    "OffsetValue" INTEGER,
    "OffsetUnit" VARCHAR(20),
    "OffsetDirection" VARCHAR(10),
    "ConditionsJson" TEXT NOT NULL DEFAULT '[]',
    "RecipientType" VARCHAR(30) NOT NULL,
    "SpecificUserId" VARCHAR(36) REFERENCES "users"("UserId"),
    "TitleTemplate" TEXT NOT NULL,
    "MessageTemplate" TEXT NOT NULL,
    "IsEnabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "CreatedByUserId" VARCHAR(36) REFERENCES "users"("UserId"),
    "DateCreatedEpochMs" BIGINT NOT NULL,
    "LastModifiedEpochMs" BIGINT NOT NULL,
    CONSTRAINT "notification_rule_trigger_ck" CHECK (
        ("TriggerType" = 'EVENT' AND "EventType" IS NOT NULL AND "DateField" IS NULL)
        OR ("TriggerType" = 'DATE' AND "EventType" IS NULL AND "DateField" IS NOT NULL
            AND "OffsetValue" IS NOT NULL AND "OffsetUnit" IS NOT NULL AND "OffsetDirection" IS NOT NULL)
    ),
    CONSTRAINT "notification_rule_specific_user_ck" CHECK (
        ("RecipientType" = 'SPECIFIC_USER' AND "SpecificUserId" IS NOT NULL)
        OR ("RecipientType" <> 'SPECIFIC_USER' AND "SpecificUserId" IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS "notification_jobs" (
    "JobId" VARCHAR(36) PRIMARY KEY,
    "RuleId" VARCHAR(36) REFERENCES "notification_rules"("RuleId"),
    "EntityType" VARCHAR(40) NOT NULL,
    "EntityId" VARCHAR(36) NOT NULL,
    "RecipientUserId" VARCHAR(36) NOT NULL REFERENCES "users"("UserId"),
    "ScheduledAtEpochMs" BIGINT NOT NULL,
    "Status" VARCHAR(20) NOT NULL,
    "Attempts" INTEGER NOT NULL DEFAULT 0,
    "MaxAttempts" INTEGER NOT NULL DEFAULT 5,
    "NextAttemptAtEpochMs" BIGINT,
    "ClaimedAtEpochMs" BIGINT,
    "ClaimedBy" VARCHAR(100),
    "LeaseUntilEpochMs" BIGINT,
    "LastError" TEXT,
    "SourceValue" VARCHAR(255),
    "IdempotencyKey" VARCHAR(255) NOT NULL,
    "DateCreatedEpochMs" BIGINT NOT NULL,
    "ProcessedAtEpochMs" BIGINT,
    "CancelledAtEpochMs" BIGINT,
    "CancelReason" TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS "notification_jobs_idempotency_uq" ON "notification_jobs"("IdempotencyKey");
CREATE INDEX IF NOT EXISTS "notification_jobs_due_idx" ON "notification_jobs"("Status", "ScheduledAtEpochMs");
CREATE INDEX IF NOT EXISTS "notification_jobs_entity_idx" ON "notification_jobs"("RuleId", "EntityType", "EntityId");
CREATE UNIQUE INDEX IF NOT EXISTS "notifications_job_uq" ON "notifications"("JobId") WHERE "JobId" IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "notifications_idempotency_uq" ON "notifications"("IdempotencyKey") WHERE "IdempotencyKey" IS NOT NULL;
CREATE INDEX IF NOT EXISTS "notifications_user_unread_idx" ON "notifications"("UserId", "IsRead", "CreatedAtEpochMs") WHERE "IsDeleted" = FALSE;

CREATE TABLE IF NOT EXISTS "user_device_tokens" (
    "TokenId" VARCHAR(36) PRIMARY KEY,
    "UserId" VARCHAR(36) NOT NULL REFERENCES "users"("UserId"),
    "DeviceToken" TEXT NOT NULL,
    "Platform" VARCHAR(20) NOT NULL,
    "IsActive" BOOLEAN NOT NULL DEFAULT TRUE,
    "LastSeenAtEpochMs" BIGINT NOT NULL,
    "DateCreatedEpochMs" BIGINT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS "user_device_tokens_token_uq" ON "user_device_tokens"("DeviceToken");
CREATE INDEX IF NOT EXISTS "user_device_tokens_user_idx" ON "user_device_tokens"("UserId", "IsActive");
