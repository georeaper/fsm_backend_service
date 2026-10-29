CREATE INDEX IF NOT EXISTS ix_fieldreports_scheduled_start
    ON fieldreports ("ScheduledStart")
    WHERE "IsDeleted" = FALSE AND "ScheduledStart" IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_fieldreports_scheduled_end
    ON fieldreports ("ScheduledEnd")
    WHERE "IsDeleted" = FALSE AND "ScheduledEnd" IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_fieldreports_scheduled_range
    ON fieldreports ("ScheduledStart", "ScheduledEnd")
    WHERE "IsDeleted" = FALSE AND "ScheduledStart" IS NOT NULL AND "ScheduledEnd" IS NOT NULL;
