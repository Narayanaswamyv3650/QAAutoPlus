@echo off
echo ========================================
echo Building QA Auto Plus Web Application
echo ========================================
echo.

cd /d "%~dp0"

echo Building the project with Maven...
call mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build successful!
    echo ========================================
    echo.
    echo The WAR file has been created at:
    echo target\qaautoplus.war
    echo.
    echo You can deploy it to any servlet container
    echo or run it using: start.bat
    echo.
) else (
    echo.
    echo ========================================
    echo Build failed!
    echo ========================================
    echo Please check the errors above.
    echo.
)

pause

