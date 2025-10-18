-- Allow checkInTime to be nullable for flexible attendance logging
ALTER TABLE attendance_logs ALTER COLUMN check_in_time DROP NOT NULL;
