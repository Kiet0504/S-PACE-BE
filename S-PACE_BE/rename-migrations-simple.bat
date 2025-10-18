@echo off
echo ============================================
echo   Renaming Migration Files (After DB Drop)
echo ============================================
echo.

cd src\main\resources\db\migration

echo [1/2] Renaming V5__Allow_Null_CheckInTime.sql to V9__Allow_Null_CheckInTime.sql
if exist "V5__Allow_Null_CheckInTime.sql" (
    ren "V5__Allow_Null_CheckInTime.sql" "V9__Allow_Null_CheckInTime.sql"
    echo   ✓ Done
) else (
    echo   ✗ File not found or already renamed
)

echo.
echo [2/2] Renaming V6__Update_Existing_Attendance_Logs.sql to V10__Update_Existing_Attendance_Logs.sql
if exist "V6__Update_Existing_Attendance_Logs.sql" (
    ren "V6__Update_Existing_Attendance_Logs.sql" "V10__Update_Existing_Attendance_Logs.sql"
    echo   ✓ Done
) else (
    echo   ✗ File not found or already renamed
)

echo.
echo ============================================
echo   Renaming Complete!
echo ============================================
echo.
echo Migration order after renaming:
echo   V1 - Initial Schema
echo   V2 - Add Collaborator Role
echo   V3 - Add Rating System
echo   V4 - Add Event Created By
echo   V5 - Add Subscription Tables
echo   V6 - Add Free Plan (KEPT)
echo   V7 - Fix Missing Free Plan
echo   V8 - Add Subscription Payment Table
echo   V9 - Allow Null CheckInTime (renamed)
echo   V10 - Update Existing Attendance Logs (renamed)
echo.
echo You can now restart your application!
echo Flyway will create a new database and run all migrations.
echo.
pause

