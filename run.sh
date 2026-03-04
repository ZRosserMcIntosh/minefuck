#!/bin/bash
# ===========================================
#  MineFuck Launcher for macOS / Linux
# ===========================================
#
# This script builds (if needed) and runs MineFuck.
# On macOS it automatically adds -XstartOnFirstThread.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_FILE="$SCRIPT_DIR/target/minefuck-1.0-SNAPSHOT.jar"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}"
echo "  ███╗   ███╗██╗███╗   ██╗███████╗███████╗██╗   ██╗ ██████╗██╗  ██╗"
echo "  ████╗ ████║██║████╗  ██║██╔════╝██╔════╝██║   ██║██╔════╝██║ ██╔╝"
echo "  ██╔████╔██║██║██╔██╗ ██║█████╗  █████╗  ██║   ██║██║     █████╔╝ "
echo "  ██║╚██╔╝██║██║██║╚██╗██║██╔══╝  ██╔══╝  ██║   ██║██║     ██╔═██╗ "
echo "  ██║ ╚═╝ ██║██║██║ ╚████║███████╗██║     ╚██████╔╝╚██████╗██║  ██╗"
echo "  ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝╚═╝      ╚═════╝  ╚═════╝╚═╝  ╚═╝"
echo -e "${NC}"

# Check for Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed!${NC}"
    echo ""
    echo "Install Java 17 or later:"
    echo "  macOS:   brew install openjdk@17"
    echo "  Ubuntu:  sudo apt install openjdk-17-jdk"
    echo "  Fedora:  sudo dnf install java-17-openjdk"
    echo ""
    exit 1
fi

# Check Java version
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 17 ] 2>/dev/null; then
    echo -e "${RED}Error: Java 17+ required (found Java $JAVA_VER)${NC}"
    exit 1
fi

# Build if JAR doesn't exist
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}First run - building MineFuck...${NC}"
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Error: Maven is not installed!${NC}"
        echo ""
        echo "Install Maven:"
        echo "  macOS:   brew install maven"
        echo "  Ubuntu:  sudo apt install maven"
        echo "  Fedora:  sudo dnf install maven"
        echo ""
        exit 1
    fi
    
    cd "$SCRIPT_DIR"
    mvn clean package -q
    echo -e "${GREEN}Build complete!${NC}"
fi

# Detect OS and set JVM args
JVM_ARGS="-Xmx2G -Xms512M"

OS="$(uname -s)"
case "$OS" in
    Darwin*)
        JVM_ARGS="$JVM_ARGS -XstartOnFirstThread"
        echo -e "${GREEN}Platform: macOS $(uname -m)${NC}"
        ;;
    Linux*)
        echo -e "${GREEN}Platform: Linux $(uname -m)${NC}"
        ;;
    *)
        echo -e "${YELLOW}Platform: $OS (untested)${NC}"
        ;;
esac

echo -e "${GREEN}Launching MineFuck...${NC}"
echo ""

# Launch the game
exec java $JVM_ARGS -jar "$JAR_FILE" "$@"
