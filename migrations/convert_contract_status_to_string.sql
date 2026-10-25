-- Apply to every existing tenant database. Safe to execute repeatedly.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'contracts'
          AND column_name = 'ContractStatus'
          AND data_type = 'boolean'
    ) THEN
        ALTER TABLE contracts
            ALTER COLUMN "ContractStatus" TYPE VARCHAR(50)
            USING CASE
                WHEN "ContractStatus" IS TRUE THEN 'ACTIVE'
                WHEN "ContractStatus" IS FALSE THEN 'INACTIVE'
                ELSE NULL
            END;
    END IF;
END $$;
