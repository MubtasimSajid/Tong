@echo off
echo ╔══════════════════════════════════════════════════════════════╗
echo ║               🚀 TONG MESSENGER - UNIFIED LAUNCHER           ║
echo ║                Premium Chat Experience                      ║
echo ║          📱 JavaFX Frontend + 🖥️ Spring Backend           ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven not found! Please install Maven and add it to PATH
    pause
    exit /b 1
)

REM Build the application
echo 🔨 Building Tong Messenger...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)

REM Copy dependencies
echo 📦 Preparing dependencies...
call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

REM Run the unified application
echo 🚀 Starting Tong Messenger...
mvn javafx:run

pause
