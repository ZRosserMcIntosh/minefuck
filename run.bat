@echo off
REM ===========================================
REM  MineFuck Launcher for Windows
REM ===========================================
REM
REM This script builds (if needed) and runs MineFuck.
REM

title MineFuck

echo.
echo   ███╗   ███╗██╗███╗   ██╗███████╗███████╗██╗   ██╗ ██████╗██╗  ██╗
echo   ████╗ ████║██║████╗  ██║██╔════╝██╔════╝██║   ██║██╔════╝██║ ██╔╝
echo   ██╔████╔██║██║██╔██╗ ██║█████╗  █████╗  ██║   ██║██║     █████╔╝ 
echo   ██║╚██╔╝██║██║██║╚██╗██║██╔══╝  ██╔══╝  ██║   ██║██║     ██╔═██╗ 
echo   ██║ ╚═╝ ██║██║██║ ╚████║███████╗██║     ╚██████╔╝╚██████╗██║  ██╗
echo   ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝╚═╝      ╚═════╝  ╚═════╝╚═╝  ╚═╝
echo.

REM Check for Java
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed!
    echo.
    echo Install Java 17 or later:
    echo   1. Download from: https://adoptium.net/
    echo   2. Or use: winget install EclipseAdoptium.Temurin.17.JDK
    echo   3. Or use: choco install temurin17
    echo.
    pause
    exit /b 1
)

REM Get script directory
set "SCRIPT_DIR=%~dp0"
set "JAR_FILE=%SCRIPT_DIR%target\minefuck-1.0-SNAPSHOT.jar"

REM Build if JAR doesn't exist
if not exist "%JAR_FILE%" (
    echo [INFO] First run - building MineFuck...
    
    where mvn >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Maven is not installed!
        echo.
        echo Install Maven:
        echo   1. Download from: https://maven.apache.org/download.cgi
        echo   2. Or use: winget install Apache.Maven
        echo   3. Or use: choco install maven
        echo.
        pause
        exit /b 1
    )
    
    cd /d "%SCRIPT_DIR%"
    call mvn clean package -q
    if errorlevel 1 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo [OK] Build complete!
)

echo [OK] Platform: Windows %PROCESSOR_ARCHITECTURE%
echo [OK] Launching MineFuck...
echo.

REM Launch the game (no -XstartOnFirstThread needed on Windows)
java -Xmx2G -Xms512M -jar "%JAR_FILE%" %*

if errorlevel 1 (
    echo.
    echo [ERROR] MineFuck crashed! Check the output above for details.
    pause
)
