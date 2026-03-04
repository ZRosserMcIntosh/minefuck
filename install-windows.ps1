# ===========================================
#  MineFuck - Windows One-Click Installer
# ===========================================
#
#  Right-click this file -> "Run with PowerShell"
#  OR double-click if .ps1 files are associated with PowerShell
#
#  This script will:
#  1. Check if Java 17+ is installed
#  2. Offer to install Java automatically if missing
#  3. Create a desktop shortcut
#  4. Launch the game
#

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host "    MineFuck Installer for Windows" -ForegroundColor Cyan
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jarFile = Join-Path $scriptDir "MineFuck.jar"

# --- Check for the JAR ---
if (-not (Test-Path $jarFile)) {
    # Also check for maven-built jar
    $mavenJar = Join-Path $scriptDir "target\minefuck-1.0-SNAPSHOT.jar"
    if (Test-Path $mavenJar) {
        $jarFile = $mavenJar
    } else {
        Write-Host "  [ERROR] MineFuck.jar not found!" -ForegroundColor Red
        Write-Host "  Make sure this script is in the same folder as MineFuck.jar" -ForegroundColor Red
        Write-Host ""
        Read-Host "  Press Enter to exit"
        exit 1
    }
}

# --- Check for Java ---
Write-Host "  [1/3] Checking for Java..." -ForegroundColor Yellow

$javaFound = $false
$javaPath = "java"

try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    if ($javaVersion -match '"(\d+)') {
        $majorVersion = [int]$Matches[1]
        if ($majorVersion -ge 17) {
            Write-Host "  [OK]   Java $majorVersion found!" -ForegroundColor Green
            $javaFound = $true
        } else {
            Write-Host "  [WARN] Java $majorVersion found, but Java 17+ is required" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "  [WARN] Java not found in PATH" -ForegroundColor Yellow
}

if (-not $javaFound) {
    Write-Host ""
    Write-Host "  Java 17 or later is required to run MineFuck." -ForegroundColor White
    Write-Host ""
    Write-Host "  Options:" -ForegroundColor White
    Write-Host "    [1] Open Java download page (recommended)" -ForegroundColor White
    Write-Host "    [2] Try to install with winget (if available)" -ForegroundColor White
    Write-Host "    [3] Exit" -ForegroundColor White
    Write-Host ""
    $choice = Read-Host "  Choose (1/2/3)"

    switch ($choice) {
        "1" {
            Start-Process "https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=17"
            Write-Host ""
            Write-Host "  Download and install Java 17 (Temurin JDK), then run this script again." -ForegroundColor Cyan
            Write-Host ""
            Read-Host "  Press Enter to exit"
            exit 0
        }
        "2" {
            Write-Host ""
            Write-Host "  Installing Java 17 via winget..." -ForegroundColor Yellow
            try {
                & winget install EclipseAdoptium.Temurin.17.JDK --accept-source-agreements --accept-package-agreements
                Write-Host "  [OK]   Java installed! You may need to restart this script." -ForegroundColor Green
                Write-Host ""
                # Refresh PATH
                $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
                $javaFound = $true
            } catch {
                Write-Host "  [ERROR] winget install failed. Please install Java manually from:" -ForegroundColor Red
                Write-Host "          https://adoptium.net/" -ForegroundColor White
                Read-Host "  Press Enter to exit"
                exit 1
            }
        }
        default {
            exit 0
        }
    }
}

if (-not $javaFound) {
    Write-Host "  [ERROR] Java is still not available. Please install it and try again." -ForegroundColor Red
    Read-Host "  Press Enter to exit"
    exit 1
}

# --- Create Desktop Shortcut ---
Write-Host "  [2/3] Creating desktop shortcut..." -ForegroundColor Yellow

try {
    $desktopPath = [System.Environment]::GetFolderPath("Desktop")
    $shortcutPath = Join-Path $desktopPath "MineFuck.lnk"

    # Create a batch launcher next to the jar
    $launcherBat = Join-Path $scriptDir "MineFuck-Launch.bat"
    @"
@echo off
cd /d "%~dp0"
start "" javaw -Xmx2G -Xms512M -jar "$jarFile"
"@ | Out-File -FilePath $launcherBat -Encoding ASCII

    $WshShell = New-Object -ComObject WScript.Shell
    $shortcut = $WshShell.CreateShortcut($shortcutPath)
    $shortcut.TargetPath = $launcherBat
    $shortcut.WorkingDirectory = $scriptDir
    $shortcut.Description = "MineFuck - A Minecraft Clone"
    $shortcut.Save()

    Write-Host "  [OK]   Desktop shortcut created!" -ForegroundColor Green
} catch {
    Write-Host "  [WARN] Could not create shortcut (non-critical)" -ForegroundColor Yellow
}

# --- Launch the Game ---
Write-Host "  [3/3] Launching MineFuck..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  ============================================" -ForegroundColor Green
Write-Host "    MineFuck is starting!" -ForegroundColor Green
Write-Host "    Close this window anytime." -ForegroundColor Green
Write-Host "  ============================================" -ForegroundColor Green
Write-Host ""

try {
    Start-Process -FilePath "javaw" -ArgumentList "-Xmx2G", "-Xms512M", "-jar", "`"$jarFile`"" -WorkingDirectory $scriptDir
    Write-Host "  Game launched! You can close this window." -ForegroundColor Cyan
} catch {
    # Fallback to java (blocking)
    & java -Xmx2G -Xms512M -jar "$jarFile"
}

Start-Sleep -Seconds 3
