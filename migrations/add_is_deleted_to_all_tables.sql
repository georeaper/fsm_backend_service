-- PostgreSQL migration: add soft-delete support to every table declared in
-- models/databaseModels. Safe to rerun after a successful execution.
--
-- "Contract_equipments" is quoted because the Exposed model declares it with
-- an uppercase C, making its PostgreSQL identifier case-sensitive.

BEGIN;

ALTER TABLE "customer"              ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "equipment"             ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "contracts"             ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "inventory_table"       ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "maintenances"          ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "tasks"                 ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "ticket"                ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "ticket_history"        ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "tools"                 ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "fieldreports"          ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "fieldreport_equipment" ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "fieldreport_inventory" ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "fieldreport_tools"     ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "fieldreport_checkform" ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "check_forms"           ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "settings"              ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "category_asset"        ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "manufacturer"          ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "model_asset"           ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "users"                 ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "notifications"         ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "Contract_equipments"   ADD COLUMN IF NOT EXISTS "IsDeleted" BOOLEAN NOT NULL DEFAULT false;

COMMIT;
