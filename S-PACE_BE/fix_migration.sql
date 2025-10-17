-- Run this SQL manually in your PostgreSQL database to fix the migration issue

-- OPTION 1: Delete and re-run migration (Recommended)
-- =======================================================
-- 1. Delete migration V6 record from history
DELETE FROM flyway_schema_history WHERE version = '6';

-- 2. Update old WORKED values to ATTENDED
UPDATE attendance_logs
SET participation_status = 'ATTENDED'
WHERE participation_status = 'WORKED';

-- 3. Update NULL values to NOT_ATTENDED
UPDATE attendance_logs
SET participation_status = 'NOT_ATTENDED'
WHERE participation_status IS NULL;

-- 4. Verify the update
SELECT participation_status, COUNT(*)
FROM attendance_logs
GROUP BY participation_status;

-- After running this, restart your application and V6 will run again with the new checksum


-- OPTION 2: Just update checksum (If data already fixed)
-- =======================================================
-- If you already ran the updates manually, just fix the checksum:
-- UPDATE flyway_schema_history
-- SET checksum = -1653500932
-- WHERE version = '6';
