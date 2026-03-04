#!/bin/bash
# ===========================================
#  MineFuck - macOS Double-Click Launcher
# ===========================================
#  Just double-click this file to play!
#  If blocked: Right-click → Open → Open
#

set -e
cd "$(dirname "$0")"

# Find the JAR
JAR=""
if [ -f "MineFuck.jar" ]; then
    JAR="MineFuck.jar"
elif [ -f "target/minefuck-1.0-SNAPSHOT.jar" ]; then
    JAR="target/minefuck-1.0-SNAPSHOT.jar"
else
    osascript -e 'display alert "MineFuck" message "MineFuck.jar not found! Make sure this file is in the same folder as MineFuck.jar." as critical'
    exit 1
fi

# Check for Java
if ! command -v java &> /dev/null; then
    osascript -e 'display alert "Java Required" message "Java 17+ is required to run MineFuck.\n\nInstall it with:\n  brew install openjdk@17\n\nOr download from:\n  https://adoptium.net/" as critical buttons {"Open Download Page", "Cancel"} default button 1'
    RESULT=$?
    if [ "$RESULT" = "0" ]; then
        open "https://adoptium.net/temurin/releases/?os=mac&arch=x64&package=jdk&version=17"
    fi
    exit 1
fi

echo ""
echo "  ============================================"
echo "    MineFuck - Launching..."
echo "  ============================================"
echo ""

exec java -XstartOnFirstThread -Xmx2G -Xms512M -jar "$JAR"
