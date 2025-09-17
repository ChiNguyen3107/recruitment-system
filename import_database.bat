@echo off
echo ================================================
echo IMPORT DATABASE FOR RECRUITMENT SYSTEM
echo ================================================
echo.
echo BUOC 1: Khoi dong XAMPP Control Panel
echo - Mo XAMPP Control Panel
echo - Click "Start" cho MySQL service
echo - Doi den khi trang thai la "Running"
echo.
echo BUOC 2: Chay lenh import database
echo.
pause
echo.
echo Dang import database...
"C:\xampp\mysql\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS recruitment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
"C:\xampp\mysql\bin\mysql.exe" -u root -p recruitment_db < database_schema.sql
echo.
echo ================================================
echo DATABASE IMPORT COMPLETED!
echo ================================================
echo.
echo Thong tin dang nhap mau:
echo - Admin: admin@recruitment.com / admin123
echo - Employer: employer@techinnovate.com / employer123
echo - Applicant: applicant@test.com / applicant123
echo.
pause