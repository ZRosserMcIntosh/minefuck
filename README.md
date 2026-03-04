# MineFuck ⛏️🧱

A vanilla Minecraft clone built from scratch in Java using LWJGL3 (OpenGL 3.3).

**Works on macOS, Windows, and Linux — Intel and ARM.**

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ⬇️ ONE-CLICK DOWNLOAD

> **No terminal required. No build tools needed.**

### 🪟 Windows — Double-click the .exe!

[![Download MineFuck.exe](https://img.shields.io/badge/⬇_Download-MineFuck.exe-blue?style=for-the-badge&logo=windows)](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck.exe)

1. Click the button above to download **`MineFuck.exe`**
2. Double-click it. Done.
3. If you don't have Java, it will open the download page for you automatically
4. Windows SmartScreen may say "Unknown publisher" — click **"More info" → "Run anyway"**

### 🍎 macOS — Double-click to play

[![Download for macOS](https://img.shields.io/badge/⬇_Download-macOS-blue?style=for-the-badge&logo=apple)](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck-macOS.zip)

1. Download and extract the zip
2. Double-click **`MineFuck-Mac.command`**
3. If macOS blocks it: Right-click → Open → Open
4. Requires Java 17+ (`brew install openjdk@17`)

### 🐧 Linux

[![Download for Linux](https://img.shields.io/badge/⬇_Download-Linux-blue?style=for-the-badge&logo=linux)](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck-macOS.zip)

Extract → `chmod +x run.sh && ./run.sh`

### Other Downloads

| Download | Description |
|----------|-------------|
| [MineFuck-Windows.zip](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck-Windows.zip) | `.exe` + `.jar` + `.bat` launcher |
| [MineFuck-Universal.zip](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck-Universal.zip) | All platforms in one zip |
| [MineFuck.jar](https://github.com/ZRosserMcIntosh/minefuck/releases/latest/download/MineFuck.jar) | Standalone JAR (advanced, needs Java 17+) |

> 💡 **All downloads include native libraries for every platform** — the same binary runs on Windows, macOS, and Linux.

---

## 🎮 Features

- **Procedural World Generation** — Infinite terrain with Perlin noise heightmaps, biomes (forests, deserts, snow)
- **16 Block Types** — Grass, dirt, stone, sand, water, wood, leaves, cobblestone, bedrock, gravel, planks, glass, coal ore, iron ore, snow
- **First-Person Camera** — Smooth mouse-look and WASD movement
- **Block Interaction** — Break and place blocks with raycasting
- **Chunk System** — 16×256×16 chunks with multi-threaded async loading
- **Cave Generation** — 3D Perlin noise cave systems
- **Ore Distribution** — Coal and iron at realistic depths
- **Tree Generation** — Oak trees with trunk and leaf canopy
- **Physics** — Gravity, jumping, AABB collision detection
- **Frustum Culling** — Only renders visible chunks
- **Fog** — Distance-based fog blending into sky
- **HUD** — Crosshair and hotbar with block selection
- **Block Highlight** — Wireframe outline on targeted block

---

## 🚀 Quick Start (All Platforms)

### Prerequisites

| Requirement | Version | How to Install |
|-------------|---------|----------------|
| **Java (JDK)** | 17 or later | See platform instructions below |
| **Maven** | 3.8+ | See platform instructions below |
| **Git** | Any | See platform instructions below |

---

## 🍎 macOS Installation

### 1. Install prerequisites

**Using Homebrew** (recommended):
```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 17 and Maven
brew install openjdk@17 maven git
```

**Or download manually:**
- Java: https://adoptium.net/ (Temurin 17)
- Maven: https://maven.apache.org/download.cgi

### 2. Clone and run
```bash
git clone https://github.com/ZRosserMcIntosh/minefuck.git
cd minefuck
chmod +x run.sh
./run.sh
```

The launcher script handles everything automatically, including the macOS `-XstartOnFirstThread` requirement.

**Or build and run manually:**
```bash
mvn clean package
java -XstartOnFirstThread -Xmx2G -jar target/minefuck-1.0-SNAPSHOT.jar
```

> ⚠️ **macOS users MUST use `-XstartOnFirstThread`** when running manually. The `run.sh` script adds this automatically.

> Works on both Intel Macs and Apple Silicon (M1/M2/M3/M4).

---

## 🪟 Windows Installation

### Option A: One-click (if you have Java + Maven)

1. **Install Java 17:**
   - Download from https://adoptium.net/ (pick Windows x64 `.msi`)
   - **Or** run in PowerShell: `winget install EclipseAdoptium.Temurin.17.JDK`
   - **Or** with Chocolatey: `choco install temurin17`

2. **Install Maven:**
   - Download from https://maven.apache.org/download.cgi (Binary zip)
   - Extract to `C:\Program Files\Maven`
   - Add `C:\Program Files\Maven\bin` to your PATH
   - **Or** run: `winget install Apache.Maven`
   - **Or** with Chocolatey: `choco install maven`

3. **Install Git:**
   - Download from https://git-scm.com/download/win
   - **Or** run: `winget install Git.Git`

4. **Clone and run:**
   ```cmd
   git clone https://github.com/ZRosserMcIntosh/minefuck.git
   cd minefuck
   run.bat
   ```

### Option B: Manual build
```cmd
git clone https://github.com/ZRosserMcIntosh/minefuck.git
cd minefuck
mvn clean package
java -Xmx2G -jar target\minefuck-1.0-SNAPSHOT.jar
```

> Works on Windows 10/11, both x64 and ARM64.

---

## 🐧 Linux Installation

### Ubuntu / Debian
```bash
sudo apt update
sudo apt install openjdk-17-jdk maven git
git clone https://github.com/ZRosserMcIntosh/minefuck.git
cd minefuck
chmod +x run.sh
./run.sh
```

### Fedora / RHEL
```bash
sudo dnf install java-17-openjdk-devel maven git
git clone https://github.com/ZRosserMcIntosh/minefuck.git
cd minefuck
chmod +x run.sh
./run.sh
```

### Arch Linux
```bash
sudo pacman -S jdk17-openjdk maven git
git clone https://github.com/ZRosserMcIntosh/minefuck.git
cd minefuck
chmod +x run.sh
./run.sh
```

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| **W / A / S / D** | Move forward / left / backward / right |
| **Space** | Jump |
| **Left Shift** | Sneak (slow walk) |
| **Left Click** | Break block |
| **Right Click** | Place block |
| **1 – 9** | Select block from hotbar |
| **Scroll Wheel** | Cycle hotbar selection |
| **Escape** | Pause / release mouse |
| **F3** | Toggle debug info |
| **F11** | Toggle fullscreen |

### Hotbar Slots
| Slot | Block |
|------|-------|
| 1 | Grass |
| 2 | Dirt |
| 3 | Stone |
| 4 | Sand |
| 5 | Wood |
| 6 | Planks |
| 7 | Cobblestone |
| 8 | Glass |
| 9 | Leaves |

---

## 🏗️ Project Structure

```
minefuck/
├── pom.xml                          # Maven build (all platform natives)
├── run.sh                           # macOS/Linux launcher
├── run.bat                          # Windows launcher
└── src/main/java/com/minefuck/
    ├── Main.java                    # Entry point (auto macOS handling)
    ├── Game.java                    # Game loop & subsystem management
    ├── engine/
    │   ├── Window.java              # GLFW window management
    │   ├── Timer.java               # Game loop timer
    │   └── input/
    │       ├── InputHandler.java    # Keyboard input
    │       └── MouseHandler.java    # Mouse input
    ├── entity/
    │   └── Player.java              # Player movement, physics, interaction
    ├── renderer/
    │   ├── MasterRenderer.java      # OpenGL rendering pipeline
    │   ├── ChunkMesh.java           # GPU mesh (VAO/VBO)
    │   ├── ChunkMeshBuilder.java    # Greedy face-culled mesh builder
    │   ├── HUDRenderer.java         # Crosshair & hotbar
    │   ├── ShaderProgram.java       # GLSL shader management
    │   └── Shaders.java             # All shader source code
    └── world/
        ├── World.java               # Chunk management & raycasting
        ├── Chunk.java               # 16×256×16 block storage
        ├── Block.java               # Block type definitions
        ├── WorldGenerator.java      # Terrain, caves, ores, trees
        └── PerlinNoise.java         # Noise generation
```

---

## 🔧 Troubleshooting

### "Java not found" or wrong version
```bash
java -version   # Should show 17+
```
Make sure `JAVA_HOME` points to JDK 17+ and Java is in your `PATH`.

### macOS: Game crashes immediately
You must run with `-XstartOnFirstThread`. Use `./run.sh` which does this automatically, or:
```bash
java -XstartOnFirstThread -jar target/minefuck-1.0-SNAPSHOT.jar
```

### macOS: "Cannot be opened because the developer cannot be verified"
Go to **System Settings → Privacy & Security** and click "Allow Anyway".

### Linux: No display / OpenGL errors
Make sure you have GPU drivers installed:
```bash
# Check OpenGL version
glxinfo | grep "OpenGL version"   # Needs 3.3+
```

### Out of memory
Increase heap size:
```bash
java -Xmx4G -jar target/minefuck-1.0-SNAPSHOT.jar
```

### Build fails on Windows
Make sure `JAVA_HOME` is set:
```cmd
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
```

---

## 📦 Supported Platforms

| Platform | Architecture | Status |
|----------|-------------|--------|
| macOS | x86_64 (Intel) | ✅ Tested |
| macOS | arm64 (Apple Silicon) | ✅ Supported |
| Windows | x86_64 | ✅ Supported |
| Windows | arm64 | ✅ Supported |
| Linux | x86_64 | ✅ Supported |
| Linux | arm64 | ✅ Supported |

The fat JAR includes native libraries for **all platforms** — one build runs everywhere.

---

## License

MIT
