@echo off
setlocal enableextensions enabledelayedexpansion
title Recruitment System - Server
color 0A

echo ========================================
echo RECRUITMENT SYSTEM - SERVER STARTUP
echo ========================================
echo.

REM --- Configurable arguments/environment ---
REM Usage: START_SERVER.bat [PORT] [PROFILE]
set PORT=%~1
if "%PORT%"=="" set PORT=%PORT%
if "%PORT%"=="" set PORT=8081

set PROFILE=%~2
if "%PROFILE%"=="" set PROFILE=%SPRING_PROFILE%
if "%PROFILE%"=="" set PROFILE=dev

REM Change to project directory (adjust if moved)
cd /d "D:\xampp\htdocs\HTQL_TuyenDung\recruitment-system"
echo [OK] Project directory: %CD%

REM --- Java detection ---
set "JAVA_CMD="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    ) else (
        echo [WARN] JAVA_HOME is set but java.exe not found: %JAVA_HOME%
    )
) 
if not defined JAVA_CMD (
    for /f "delims=" %%j in ('where java 2^>nul') do (
        set "JAVA_CMD=%%j"
        goto :afterWhereJava
    )
)
:afterWhereJava

if not defined JAVA_CMD (
    if exist "C:\Program Files\Java\jdk-17.0.1\bin\java.exe" set "JAVA_CMD=C:\Program Files\Java\jdk-17.0.1\bin\java.exe"
)

if not defined JAVA_CMD (
    echo [ERROR] Java not found. Please install JDK 17 and/or set JAVA_HOME.
    echo        Example: setx JAVA_HOME "C:\Program Files\Java\jdk-17"
    pause
    exit /b 1
)

REM Ensure JAVA_HOME points to a JDK (must have bin\javac.exe)
set "_JAVA_OK="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set _JAVA_OK=1

if not defined _JAVA_OK (
    REM Try to derive JAVA_HOME from JAVA_CMD when JAVA_CMD ends with \bin\java.exe
    for %%i in ("%JAVA_CMD%") do (
        set "_JC_DIR=%%~dpi"
    )
    if defined _JC_DIR if exist "%_JC_DIR%javac.exe" (
        pushd "%_JC_DIR%.."
        set "JAVA_HOME=%CD%"
        popd
        set _JAVA_OK=1
    )
)

if not defined _JAVA_OK (
    REM Probe common JDK locations
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set _JAVA_OK=1
            goto :afterJdkProbe
        )
    )
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set _JAVA_OK=1
            goto :afterJdkProbe
        )
    )
    for /d %%d in ("C:\Program Files\Microsoft\jdk-*\") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set _JAVA_OK=1
            goto :afterJdkProbe
        )
    )
)
:afterJdkProbe

if not defined _JAVA_OK (
    echo [ERROR] Valid JDK not found. Please install JDK 17+ and set JAVA_HOME.
    echo        Example: setx JAVA_HOME "C:\Program Files\Java\jdk-17"
    pause
    exit /b 1
)

REM Set JAVA_CMD to JAVA_HOME\bin\java.exe for consistency
set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"

echo [OK] Using Java: %JAVA_CMD%
echo [OK] JAVA_HOME: %JAVA_HOME%

REM --- Maven wrapper detection ---
set MVNW=.\mvnw.cmd
if not exist "%MVNW%" (
    for /f "delims=" %%m in ('where mvn 2^>nul') do (
        set MVNW=mvn
    )
)

if not exist "%MVNW%" (
    echo [ERROR] Maven wrapper or mvn not found. Ensure .\mvnw.cmd or Apache Maven is installed.
    pause
    exit /b 1
) else (
    echo [OK] Using Maven: %MVNW%
)

REM --- Optional MySQL check (set SKIP_MYSQL=1 to skip) ---
if "%SKIP_MYSQL%"=="1" (
    echo [INFO] Skipping MySQL check (SKIP_MYSQL=1)
) else (
    echo [INFO] Checking MySQL...
    tasklist | findstr /i mysqld.exe >nul
    if %errorlevel% equ 0 (
        echo [OK] MySQL is running
    ) else (
        echo [ERROR] MySQL is not running! Please start XAMPP first.
        echo [TIP] Open XAMPP Control Panel and Start MySQL service
        pause
        exit /b 1
    )
)

REM --- Free target port (robust via PowerShell) ---
echo [INFO] Checking port %PORT%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$port=%PORT%; $conns=Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue; if ($conns) { Write-Output \"[WARNING] Port $port is in use, stopping old process(es)...\"; $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($pid in $pids) { Write-Output \"[INFO] Stopping process PID: $pid\"; try { Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue } catch {} }; Start-Sleep -Seconds 2 } else { Write-Output \"[OK] Port $port is available\" }" >nul

REM --- Always build before run to avoid stale JAR ---
echo [INFO] Building project (skip tests)...
call %MVNW% -q clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
) else (
    echo [OK] Build successful!
)

REM --- Locate JAR file (pick newest in target) ---
set "JAR_FILE="
for /f "delims=" %%J in ('dir /b /o-d target\*.jar 2^>nul') do (
    set "JAR_FILE=target\%%J"
    goto :afterJarSearch
)
:afterJarSearch
if not defined JAR_FILE (
    echo [ERROR] No JAR produced in target\*.jar
    pause
    exit /b 1
)
echo [OK] JAR file: %JAR_FILE%

echo.
echo ========================================
echo STARTING SERVER ON PORT %PORT% (profile=%PROFILE%)...
echo ========================================
echo [URL]   http://localhost:%PORT%
echo [API]   http://localhost:%PORT%/api
echo [DEBUG] http://localhost:%PORT%/debug/test
echo [STOP]  Press Ctrl+C to stop server
echo ========================================
echo.

REM --- Start server ---
echo [INFO] Starting server...
"%JAVA_CMD%" -Xms512m -Xmx1024m -Dspring.profiles.active=%PROFILE% -Dserver.port=%PORT% -Dspring.main.keep-alive=true -Dserver.shutdown=graceful -jar "%JAR_FILE%"

echo.
echo [INFO] Server stopped.
pause












