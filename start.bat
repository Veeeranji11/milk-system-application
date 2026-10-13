@echo off
echo ===================================================================
echo   Starting Village Milk Collection & Money Management System
echo ===================================================================
echo.

cd /d "%~dp0"

echo [1/2] Building and Packaging Application...
call .\mvnw.cmd clean package -DskipTests=true

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven build failed. Please inspect console logs.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Launching Spring Boot Server on http://localhost:8080 ...
java -jar target\MilkSystemApplication-0.0.1-SNAPSHOT.jar

pause
