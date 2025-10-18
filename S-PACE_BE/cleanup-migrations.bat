@echo off
echo Cleaning up duplicate migration files...
echo.

cd /d "d:\EXE201_BE\S-PACE-BE\S-PACE_BE\src\main\resources\db\migration"

echo Deleting V9__Allow_Null_CheckInTime.sql (duplicate)...
if exist "V9__Allow_Null_CheckInTime.sql" (
    del "V9__Allow_Null_CheckInTime.sql"
    echo Deleted V9__Allow_Null_CheckInTime.sql
) else (
    echo File V9__Allow_Null_CheckInTime.sql not found
)

echo Deleting V10__Update_Existing_Attendance_Logs.sql (duplicate)...
if exist "V10__Update_Existing_Attendance_Logs.sql" (
    del "V10__Update_Existing_Attendance_Logs.sql"
    echo Deleted V10__Update_Existing_Attendance_Logs.sql
) else (
    echo File V10__Update_Existing_Attendance_Logs.sql not found
)

echo.
echo Cleanup complete!
echo.
echo Current migration files:
dir /b V*.sql
echo.
echo Press any key to continue...
pause >nul

