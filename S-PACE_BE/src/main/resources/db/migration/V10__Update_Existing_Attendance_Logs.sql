-- Update existing attendance logs with default participation_status
-- Map old values to new enum values
UPDATE attendance_logs
SET participation_status = 'ATTENDED'
WHERE participation_status = 'WORKED';

UPDATE attendance_logs
SET participation_status = 'NOT_ATTENDED'
WHERE participation_status IS NULL;
