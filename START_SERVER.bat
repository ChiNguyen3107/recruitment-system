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
set "PORT=%~1"
if "%PORT%"=="" set "PORT=%PORT%"
if "%PORT%"=="" set "PORT=8081"

set "PROFILE=%~2"
if "%PROFILE%"=="" set "PROFILE=%SPRING_PROFILE%"
if "%PROFILE%"=="" set "PROFILE=dev"

REM Optional: health path to verify readiness (override by setting HEALTH_PATH env or 3rd arg)
if not "%~3"=="" set "HEALTH_PATH=%~3"
if "%HEALTH_PATH%"=="" set "HEALTH_PATH=/actuator/health"

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
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "_JAVA_OK=1"

if not defined _JAVA_OK (
    for %%i in ("%JAVA_CMD%") do set "_JC_DIR=%%~dpi"
    if defined _JC_DIR if exist "%_JC_DIR%javac.exe" (
        pushd "%_JC_DIR%.."
        set "JAVA_HOME=%CD%"
        popd
        set "_JAVA_OK=1"
    )
)

if not defined _JAVA_OK (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set "_JAVA_OK=1"
            goto :afterJdkProbe
        )
    )
    for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set "_JAVA_OK=1"
            goto :afterJdkProbe
        )
    )
    for /d %%d in ("C:\Program Files\Microsoft\jdk-*\") do (
        if exist "%%d\bin\javac.exe" (
            set "JAVA_HOME=%%d"
            set "_JAVA_OK=1"
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

set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
echo [OK] Using Java: %JAVA_CMD%
echo [OK] JAVA_HOME: %JAVA_HOME%

REM --- Maven wrapper detection ---
set "MVNW=.\mvnw.cmd"
if not exist "%MVNW%" (
    for /f "delims=" %%m in ('where mvn 2^>nul') do set "MVNW=mvn"
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
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$p=%PORT%; $c=Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue; ^
   if($c){Write-Output '[WARNING] Port {0} is in use, stopping old process(es)...' -f $p; ^
   $pids=$c|Select-Object -Expand OwningProcess -Unique; foreach($pid in $pids){ ^
     try{Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue}catch{}}; ^
   Start-Sleep 2 } else { Write-Output '[OK] Port {0} is available' -f $p }" >nul

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
echo [HEALTH]%HEALTH_PATH%
echo [STOP]  taskkill /PID {PID} /T /F
echo ========================================
echo.

REM --- Start server in background and capture PID ---
for /f "usebackq delims=" %%P in (`
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$args=@('-Xms512m','-Xmx1024m','-Dspring.profiles.active=%PROFILE%','-Dserver.port=%PORT%','-Dspring.main.keep-alive=true','-Dserver.shutdown=graceful','-jar','%JAR_FILE%'); ^
   $p=Start-Process -FilePath '%JAVA_CMD%' -ArgumentList $args -PassThru; $p.Id"
`) do set "SERVER_PID=%%P"

if not defined SERVER_PID (
    echo [ERROR] Could not start server process.
    pause
    exit /b 1
)
echo [INFO] Server PID: %SERVER_PID%

REM --- Wait for readiness: try HTTP then fallback to port listen ---
echo [INFO] Waiting for server to be ready (timeout 60s)...
set /a "__t=0"
:wait_loop

REM check if process already exited
powershell -NoProfile -Command "Get-Process -Id %SERVER_PID% -ErrorAction SilentlyContinue | Out-Null; if($?){exit 0}else{exit 1}"
if errorlevel 1 (
    echo ❌ [ERROR] Server process exited unexpectedly (PID %SERVER_PID%).
    echo [HINT] Kiểm tra log ứng dụng hoặc thử chạy lại với PROFILE=dev và PORT khác.
    goto :end
)

REM try HTTP /actuator/health (or custom HEALTH_PATH)
powershell -NoProfile -Command ^
  "$ProgressPreference='SilentlyContinue'; ^
   try{ $r=Invoke-WebRequest -Uri 'http://localhost:%PORT%%HEALTH_PATH%' -UseBasicParsing -TimeoutSec 2; ^
        if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){ exit 0 } else { exit 1 } } ^
   catch{ exit 1 }"
if not errorlevel 1 (
    echo ✅ [READY] Server đã chạy thành công tại: http://localhost:%PORT%  (PID %SERVER_PID%)
    echo [TIP] API root: http://localhost:%PORT%/api
    goto :running
)

REM fallback: check if TCP port is listening
powershell -NoProfile -Command ^
  "$p=%PORT%; if(Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue){ exit 0 } else { exit 1 }"
if not errorlevel 1 (
    echo ✅ [LISTEN] Server đang lắng nghe cổng %PORT%  (PID %SERVER_PID%)
    echo [TIP] Có thể endpoint health chưa sẵn sàng/khác đường dẫn: %HEALTH_PATH%
    goto :running
)

REM wait and retry
timeout /t 2 >nul
set /a "__t+=2"
if %__t% GEQ 60 (
    echo ⚠️  [WARN] Quá thời gian chờ 60s — chưa xác nhận được trạng thái sẵn sàng.
    echo [HINT] Mở trình duyệt: http://localhost:%PORT% hoặc kiểm tra log.
    goto :running
)
goto :wait_loop

:running
echo.
echo ========================================
echo [RUNNING] Server đang chạy nền (PID %SERVER_PID%)
echo [OPEN]   start "" http://localhost:%PORT%
echo [STOP]   taskkill /PID %SERVER_PID% /T /F
echo ========================================
echo.
pause

:end
endlocal
