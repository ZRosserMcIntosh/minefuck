package com.minefuck.world;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages the entire block world, including chunk loading/unloading.
 */
public class World {

    public static final int RENDER_DISTANCE = 8; // chunks
    public static final int LOAD_DISTANCE = RENDER_DISTANCE + 2;

    private final Map<Long, Chunk> chunks = new ConcurrentHashMap<>();
    private final WorldGenerator generator;
    private final ExecutorService chunkLoader;
    private final Set<Long> loadingChunks = ConcurrentHashMap.newKeySet();

    private int centerChunkX, centerChunkZ;

    public World(long seed) {
        this.generator = new WorldGenerator(seed);
        this.chunkLoader = Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    }

    public void update(float playerX, float playerY, float playerZ) {
        int newCenterX = (int) Math.floor(playerX / Chunk.SIZE);
        int newCenterZ = (int) Math.floor(playerZ / Chunk.SIZE);

        centerChunkX = newCenterX;
        centerChunkZ = newCenterZ;

        // Load new chunks
        for (int dx = -LOAD_DISTANCE; dx <= LOAD_DISTANCE; dx++) {
            for (int dz = -LOAD_DISTANCE; dz <= LOAD_DISTANCE; dz++) {
                if (dx * dx + dz * dz > LOAD_DISTANCE * LOAD_DISTANCE) continue;

                int cx = newCenterX + dx;
                int cz = newCenterZ + dz;
                long key = chunkKey(cx, cz);

                if (!chunks.containsKey(key) && !loadingChunks.contains(key)) {
                    loadingChunks.add(key);
                    chunkLoader.submit(() -> {
                        try {
                            Chunk chunk = new Chunk(cx, cz);
                            generator.generateChunk(chunk);
                            chunks.put(key, chunk);
                        } finally {
                            loadingChunks.remove(key);
                        }
                    });
                }
            }
        }

        // Unload distant chunks
        List<Long> toRemove = new ArrayList<>();
        for (Map.Entry<Long, Chunk> entry : chunks.entrySet()) {
            Chunk chunk = entry.getValue();
            int dx = chunk.getChunkX() - newCenterX;
            int dz = chunk.getChunkZ() - newCenterZ;
            if (dx * dx + dz * dz > (LOAD_DISTANCE + 2) * (LOAD_DISTANCE + 2)) {
                toRemove.add(entry.getKey());
            }
        }
        for (Long key : toRemove) {
            Chunk chunk = chunks.remove(key);
            if (chunk != null) chunk.cleanup();
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.HEIGHT) return Block.AIR;

        int cx = Math.floorDiv(x, Chunk.SIZE);
        int cz = Math.floorDiv(z, Chunk.SIZE);
        Chunk chunk = chunks.get(chunkKey(cx, cz));
        if (chunk == null || !chunk.isGenerated()) return Block.AIR;

        int lx = Math.floorMod(x, Chunk.SIZE);
        int lz = Math.floorMod(z, Chunk.SIZE);
        return chunk.getBlock(lx, y, lz);
    }

    public void setBlock(int x, int y, int z, byte type) {
        if (y < 0 || y >= Chunk.HEIGHT) return;

        int cx = Math.floorDiv(x, Chunk.SIZE);
        int cz = Math.floorDiv(z, Chunk.SIZE);
        Chunk chunk = chunks.get(chunkKey(cx, cz));
        if (chunk == null || !chunk.isGenerated()) return;

        int lx = Math.floorMod(x, Chunk.SIZE);
        int lz = Math.floorMod(z, Chunk.SIZE);
        chunk.setBlock(lx, y, lz, type);

        // Mark neighboring chunks dirty if block is on edge
        if (lx == 0) markDirty(cx - 1, cz);
        if (lx == Chunk.SIZE - 1) markDirty(cx + 1, cz);
        if (lz == 0) markDirty(cx, cz - 1);
        if (lz == Chunk.SIZE - 1) markDirty(cx, cz + 1);
    }

    private void markDirty(int cx, int cz) {
        Chunk chunk = chunks.get(chunkKey(cx, cz));
        if (chunk != null) chunk.setDirty(true);
    }

    public Collection<Chunk> getLoadedChunks() {
        return chunks.values();
    }

    public Chunk getChunk(int cx, int cz) {
        return chunks.get(chunkKey(cx, cz));
    }

    public int getCenterChunkX() { return centerChunkX; }
    public int getCenterChunkZ() { return centerChunkZ; }

    public int getChunkCount() { return chunks.size(); }

    public WorldGenerator getGenerator() { return generator; }

    /**
     * Raycast into the world to find the block the player is looking at.
     * Returns int[4]: {blockX, blockY, blockZ, face} or null if nothing hit.
     * Face: 0=top, 1=bottom, 2=north(-Z), 3=south(+Z), 4=west(-X), 5=east(+X)
     */
    public int[] raycast(float ox, float oy, float oz, float dx, float dy, float dz, float maxDist) {
        float t = 0;
        float step = 0.05f;

        int prevBX = Integer.MIN_VALUE, prevBY = Integer.MIN_VALUE, prevBZ = Integer.MIN_VALUE;

        while (t < maxDist) {
            float px = ox + dx * t;
            float py = oy + dy * t;
            float pz = oz + dz * t;

            int bx = (int) Math.floor(px);
            int by = (int) Math.floor(py);
            int bz = (int) Math.floor(pz);

            if (bx != prevBX || by != prevBY || bz != prevBZ) {
                byte block = getBlock(bx, by, bz);
                if (block != Block.AIR && block != Block.WATER) {
                    // Determine face
                    int face = 0;
                    if (prevBY != Integer.MIN_VALUE) {
                        int dbx = bx - prevBX;
                        int dby = by - prevBY;
                        int dbz = bz - prevBZ;

                        if (dby == 1) face = 1;       // hit bottom face
                        else if (dby == -1) face = 0;  // hit top face
                        else if (dbz == -1) face = 3;  // hit south face
                        else if (dbz == 1) face = 2;   // hit north face
                        else if (dbx == -1) face = 5;  // hit east face
                        else if (dbx == 1) face = 4;   // hit west face
                    }
                    return new int[]{bx, by, bz, face, prevBX, prevBY, prevBZ};
                }
                prevBX = bx;
                prevBY = by;
                prevBZ = bz;
            }

            t += step;
        }

        return null;
    }

    private static long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    public void cleanup() {
        chunkLoader.shutdown();
        try {
            chunkLoader.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            chunkLoader.shutdownNow();
        }
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
    }
}
