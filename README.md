# MineFuck 🧱⛏️

A vanilla Minecraft clone built from scratch in Java using LWJGL (OpenGL).

## Features

- **Procedural World Generation** - Infinite terrain with Perlin noise-based heightmaps
- **Block System** - Grass, dirt, stone, sand, water, wood, leaves, and more
- **First-Person Camera** - Smooth mouse-look and WASD movement
- **Block Interaction** - Place and destroy blocks
- **Chunk System** - Efficient chunk-based world loading/rendering
- **Day/Night Cycle** - Dynamic sky lighting
- **Physics** - Gravity and collision detection
- **Frustum Culling** - Only render what's visible

## Controls

| Key | Action |
|-----|--------|
| W/A/S/D | Move forward/left/backward/right |
| Space | Jump |
| Shift | Sneak/descend |
| Left Click | Destroy block |
| Right Click | Place block |
| 1-9 | Select block type |
| E | Toggle inventory |
| Escape | Pause/release mouse |
| F3 | Debug info |
| F11 | Toggle fullscreen |

## Building & Running

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean package
```

### Run
```bash
mvn exec:java -Dexec.mainClass="com.minefuck.Main"
```

Or after building:
```bash
java -jar target/minefuck-1.0-SNAPSHOT.jar
```

**macOS Note:** You may need to add `-XstartOnFirstThread` JVM argument:
```bash
java -XstartOnFirstThread -jar target/minefuck-1.0-SNAPSHOT.jar
```

## License

MIT
