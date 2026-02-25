@echo off
echo ========================================
echo Starting QA Auto Plus Web Application
echo ========================================
echo.

cd /d "%~dp0"

echo Cleaning and compiling the project...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.
echo Starting the application...
echo The application will be available at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the server.
echo.

java -cp "target/classes;target/dependency/*" com.qaautoplus.Main

pause

