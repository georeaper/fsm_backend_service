CREATE TABLE IF NOT EXISTS fieldreport_technical_cases (
    "Id" VARCHAR(36) PRIMARY KEY,
    "FieldReportId" VARCHAR(36) NOT NULL REFERENCES fieldreports("FieldReportId"),
    "TechnicalCaseId" VARCHAR(36) NOT NULL REFERENCES ticket("TicketId"),
    "ResolutionStatus" VARCHAR(50),
    "ResolutionNotes" TEXT,
    "DateCreated" VARCHAR(50),
    "LastModified" VARCHAR(50),
    "IsDeleted" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_fieldreport_technical_cases_active
    ON fieldreport_technical_cases ("FieldReportId", "TechnicalCaseId")
    WHERE "IsDeleted" = FALSE;

CREATE INDEX IF NOT EXISTS ix_fieldreport_technical_cases_case
    ON fieldreport_technical_cases ("TechnicalCaseId")
    WHERE "IsDeleted" = FALSE;

INSERT INTO fieldreport_technical_cases
    ("Id", "FieldReportId", "TechnicalCaseId", "DateCreated", "LastModified", "IsDeleted")
SELECT gen_random_uuid()::text, fr."FieldReportId", fr."CaseId",
       fr."DateCreated", fr."LastModified", FALSE
FROM fieldreports fr
WHERE fr."CaseId" IS NOT NULL
  AND COALESCE(fr."IsDeleted", FALSE) = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM fieldreport_technical_cases link
      WHERE link."FieldReportId" = fr."FieldReportId"
        AND link."TechnicalCaseId" = fr."CaseId"
        AND link."IsDeleted" = FALSE
  );

ALTER TABLE ticket_history
    ADD COLUMN IF NOT EXISTS "TicketId" VARCHAR(36) REFERENCES ticket("TicketId");
