@echo off
echo Killing any process on port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do taskkill /PID %%a /F >nul 2>&1

echo Building project...
call mvn clean package -DskipTests

echo Starting application on port 8080...
java -jar target\MilkSystemApplication-0.0.1-SNAPSHOT.jar

