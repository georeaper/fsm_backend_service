CREATE TABLE IF NOT EXISTS fieldreport_technicians (
    "Id" VARCHAR(36) PRIMARY KEY,
    "FieldReportId" VARCHAR(36) NOT NULL REFERENCES fieldreports("FieldReportId"),
    "UserId" VARCHAR(36) NOT NULL REFERENCES users("UserId"),
    "Role" VARCHAR(20) NOT NULL DEFAULT 'ASSISTANT',
    "AssignedAt" VARCHAR(50),
    "LastModified" VARCHAR(50),
    "IsDeleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_fieldreport_technicians_active
    ON fieldreport_technicians ("FieldReportId", "UserId")
    WHERE "IsDeleted" = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_fieldreport_technicians_lead
    ON fieldreport_technicians ("FieldReportId")
    WHERE "IsDeleted" = FALSE AND "Role" = 'LEAD';

CREATE INDEX IF NOT EXISTS ix_fieldreport_technicians_user
    ON fieldreport_technicians ("UserId")
    WHERE "IsDeleted" = FALSE;

INSERT INTO fieldreport_technicians
    ("Id", "FieldReportId", "UserId", "Role", "AssignedAt", "LastModified", "IsDeleted")
SELECT gen_random_uuid()::text, fr."FieldReportId", fr."UserId", 'LEAD',
       fr."DateCreated", fr."LastModified", FALSE
FROM fieldreports fr
WHERE fr."UserId" IS NOT NULL
  AND COALESCE(fr."IsDeleted", FALSE) = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM fieldreport_technicians assignment
      WHERE assignment."FieldReportId" = fr."FieldReportId"
        AND assignment."UserId" = fr."UserId"
        AND assignment."IsDeleted" = FALSE
  );
