-- Script để fix lỗi migration version trùng lặp
-- Chạy script này TRƯỚC KHI đổi tên file

-- 1. Kiểm tra các migration đã chạy
SELECT version, description, installed_on, success
FROM flyway_schema_history
WHERE version IN ('5', '6', '7', '8', '9', '10')
ORDER BY installed_rank;

-- 2. Update version cho các file sẽ đổi tên
-- Nếu V5__Allow_Null_CheckInTime đã chạy, update thành V9
UPDATE flyway_schema_history
SET version = '9',
    description = 'Allow Null CheckInTime',
    script = 'V9__Allow_Null_CheckInTime.sql'
WHERE version = '5'
  AND (description = 'Allow Null CheckInTime' OR script LIKE '%Allow_Null_CheckInTime%');

-- Nếu V6__Update_Existing_Attendance_Logs đã chạy, update thành V10
UPDATE flyway_schema_history
SET version = '10',
    description = 'Update Existing Attendance Logs',
    script = 'V10__Update_Existing_Attendance_Logs.sql'
WHERE version = '6'
  AND (description = 'Update Existing Attendance Logs' OR script LIKE '%Update_Existing_Attendance_Logs%');

-- 3. Kiểm tra lại sau khi update
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
