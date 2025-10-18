@echo off
echo Renaming migration files to fix duplicate versions...

cd src\main\resources\db\migration

echo Renaming V5__Allow_Null_CheckInTime.sql to V9__Allow_Null_CheckInTime.sql
ren "V5__Allow_Null_CheckInTime.sql" "V9__Allow_Null_CheckInTime.sql"

echo Renaming V10__Update_Existing_Attendance_Logs.sql to V10__Update_Existing_Attendance_Logs.sql
ren "V10__Update_Existing_Attendance_Logs.sql" "V10__Update_Existing_Attendance_Logs.sql"

echo Done! Files renamed successfully.
echo.
echo IMPORTANT: You must now run fix_migration_versions.sql on your database!
echo.
echo New migration order:
echo   V5 - Add Subscription Tables (unchanged)
echo   V6 - Add Free Plan (unchanged - KEPT)
echo   V7 - Fix Missing Free Plan (unchanged)
echo   V8 - Add Subscription Payment Table (unchanged)
echo   V9 - Allow Null CheckInTime (renamed from V5)
echo   V10 - Update Existing Attendance Logs (renamed from V6)
pause
