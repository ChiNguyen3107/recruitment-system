@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.1
echo Starting Spring Boot application...
echo JAVA_HOME set to: %JAVA_HOME%

cd /d "d:\xampp\htdocs\HTQL_TuyenDung\recruitment-system"

rem Try to run with Maven wrapper
if exist "mvnw.cmd" (
    echo Found Maven wrapper, running application...
    call mvnw.cmd spring-boot:run
) else (
    echo Maven wrapper not found. Please install Maven or use VS Code Java extension.
    echo.
    echo Alternative: Open the project in VS Code and run RecruitmentSystemApplication.java directly
    pause
)