package com.minefuck.world;

import com.minefuck.renderer.ChunkMesh;

/**
 * A 16x256x16 chunk of blocks, the fundamental unit of the world.
 */
public class Chunk {

    public static final int SIZE = 16;
    public static final int HEIGHT = 256;

    private final int chunkX, chunkZ; // chunk coordinates (not world coordinates)
    private final byte[][][] blocks;
    private ChunkMesh mesh;
    private boolean dirty = true;
    private boolean generated = false;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new byte[SIZE][HEIGHT][SIZE];
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }

    public void setBlock(int x, int y, int z, byte type) {
        if (x < 0 || x >= SIZE || y < 0 || y >= HEIGHT || z < 0 || z >= SIZE) {
            return;
        }
        blocks[x][y][z] = type;
        dirty = true;
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public int getWorldX() { return chunkX * SIZE; }
    public int getWorldZ() { return chunkZ * SIZE; }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }

    public ChunkMesh getMesh() { return mesh; }
    public void setMesh(ChunkMesh mesh) {
        // Cleanup old mesh
        if (this.mesh != null) {
            this.mesh.cleanup();
        }
        this.mesh = mesh;
        this.dirty = false;
    }

    public byte[][][] getBlocks() { return blocks; }

    public void cleanup() {
        if (mesh != null) {
            mesh.cleanup();
            mesh = null;
        }
    }
}
