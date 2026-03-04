package com.minefuck.world;

import java.util.Random;

/**
 * Generates terrain for chunks using Perlin noise.
 * Creates a Minecraft-like world with varied terrain, caves, ores, and trees.
 */
public class WorldGenerator {

    private final PerlinNoise heightNoise;
    private final PerlinNoise detailNoise;
    private final PerlinNoise caveNoise;
    private final PerlinNoise biomeNoise;
    private final PerlinNoise oreNoise;
    private final long seed;

    // Terrain parameters
    private static final int SEA_LEVEL = 62;
    private static final int BASE_HEIGHT = 64;
    private static final double TERRAIN_SCALE = 0.01;
    private static final double DETAIL_SCALE = 0.05;
    private static final double CAVE_SCALE = 0.04;
    private static final double BIOME_SCALE = 0.005;

    public WorldGenerator(long seed) {
        this.seed = seed;
        this.heightNoise = new PerlinNoise(seed);
        this.detailNoise = new PerlinNoise(seed + 1);
        this.caveNoise = new PerlinNoise(seed + 2);
        this.biomeNoise = new PerlinNoise(seed + 3);
        this.oreNoise = new PerlinNoise(seed + 4);
    }

    public void generateChunk(Chunk chunk) {
        int worldX = chunk.getWorldX();
        int worldZ = chunk.getWorldZ();
        Random treeRand = new Random(seed ^ ((long) chunk.getChunkX() * 341873128712L + (long) chunk.getChunkZ() * 132897987541L));

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int wx = worldX + x;
                int wz = worldZ + z;

                // Get biome value
                double biome = biomeNoise.octaveNoise(wx * BIOME_SCALE, wz * BIOME_SCALE, 3, 0.5);

                // Calculate height
                double baseNoise = heightNoise.octaveNoise(wx * TERRAIN_SCALE, wz * TERRAIN_SCALE, 6, 0.5);
                double detail = detailNoise.octaveNoise(wx * DETAIL_SCALE, wz * DETAIL_SCALE, 4, 0.5);

                // Height varies based on biome
                double heightMultiplier = 20 + biome * 15;
                int height = (int) (BASE_HEIGHT + baseNoise * heightMultiplier + detail * 4);
                height = Math.max(1, Math.min(height, Chunk.HEIGHT - 2));

                // Determine surface block based on biome
                byte surfaceBlock;
                byte subSurfaceBlock = Block.DIRT;
                boolean isDesert = biome > 0.4;
                boolean isSnowy = biome < -0.4 && height > 75;

                if (isDesert) {
                    surfaceBlock = Block.SAND;
                    subSurfaceBlock = Block.SAND;
                } else if (isSnowy) {
                    surfaceBlock = Block.SNOW;
                } else if (height <= SEA_LEVEL + 1) {
                    surfaceBlock = Block.SAND;
                } else {
                    surfaceBlock = Block.GRASS;
                }

                // Fill column
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    byte block;

                    if (y == 0) {
                        block = Block.BEDROCK;
                    } else if (y < height - 4) {
                        block = Block.STONE;
                    } else if (y < height) {
                        block = subSurfaceBlock;
                    } else if (y == height) {
                        block = surfaceBlock;
                    } else if (y <= SEA_LEVEL) {
                        block = Block.WATER;
                    } else {
                        block = Block.AIR;
                    }

                    // Caves
                    if (y > 1 && y < height - 2 && block != Block.BEDROCK) {
                        double cave = caveNoise.octaveNoise3D(
                                wx * CAVE_SCALE, y * CAVE_SCALE, wz * CAVE_SCALE, 3, 0.5);
                        if (cave > 0.55) {
                            block = Block.AIR;
                        }
                    }

                    // Ores (only in stone)
                    if (block == Block.STONE) {
                        block = generateOre(wx, y, wz, block);
                    }

                    chunk.setBlock(x, y, z, block);
                }
            }
        }

        // Generate trees
        generateTrees(chunk, treeRand);

        chunk.setGenerated(true);
        chunk.setDirty(true);
    }

    private byte generateOre(int x, int y, int z, byte current) {
        // Coal (common, y=5-128)
        if (y >= 5 && y <= 128) {
            double coal = oreNoise.noise(x * 0.1, y * 0.1 + 1000, z * 0.1);
            if (coal > 0.7) return Block.COAL_ORE;
        }

        // Iron (less common, y=5-64)
        if (y >= 5 && y <= 64) {
            double iron = oreNoise.noise(x * 0.1 + 2000, y * 0.1, z * 0.1 + 2000);
            if (iron > 0.75) return Block.IRON_ORE;
        }

        // Gravel patches
        double gravel = oreNoise.noise(x * 0.08 + 3000, y * 0.08, z * 0.08 + 3000);
        if (gravel > 0.78) return Block.GRAVEL;

        return current;
    }

    private void generateTrees(Chunk chunk, Random rand) {
        int worldX = chunk.getWorldX();
        int worldZ = chunk.getWorldZ();

        // Try to place a few trees per chunk
        int treeAttempts = 3 + rand.nextInt(4);
        for (int t = 0; t < treeAttempts; t++) {
            int tx = 2 + rand.nextInt(Chunk.SIZE - 4);
            int tz = 2 + rand.nextInt(Chunk.SIZE - 4);

            // Find ground level
            int groundY = -1;
            for (int y = Chunk.HEIGHT - 1; y > SEA_LEVEL; y--) {
                byte block = chunk.getBlock(tx, y, tz);
                if (block == Block.GRASS || block == Block.DIRT) {
                    groundY = y;
                    break;
                }
            }

            if (groundY < 0) continue;

            // Check biome (no trees in desert)
            double biome = biomeNoise.octaveNoise(
                    (worldX + tx) * BIOME_SCALE, (worldZ + tz) * BIOME_SCALE, 3, 0.5);
            if (biome > 0.4) continue; // desert

            // Place tree
            int trunkHeight = 4 + rand.nextInt(3);
            int treeTop = groundY + trunkHeight;

            if (treeTop >= Chunk.HEIGHT - 2) continue;

            // Trunk
            for (int y = groundY + 1; y <= treeTop; y++) {
                chunk.setBlock(tx, y, tz, Block.WOOD);
            }

            // Leaves (sphere-ish canopy)
            int leafStart = groundY + trunkHeight - 2;
            int leafEnd = treeTop + 1;

            for (int ly = leafStart; ly <= leafEnd; ly++) {
                int radius = (ly == leafEnd) ? 1 : 2;
                for (int lx = -radius; lx <= radius; lx++) {
                    for (int lz = -radius; lz <= radius; lz++) {
                        if (lx == 0 && lz == 0 && ly <= treeTop) continue; // trunk
                        if (Math.abs(lx) == radius && Math.abs(lz) == radius && rand.nextBoolean()) continue; // round corners

                        int px = tx + lx;
                        int pz = tz + lz;
                        if (px >= 0 && px < Chunk.SIZE && pz >= 0 && pz < Chunk.SIZE && ly < Chunk.HEIGHT) {
                            if (chunk.getBlock(px, ly, pz) == Block.AIR) {
                                chunk.setBlock(px, ly, pz, Block.LEAVES);
                            }
                        }
                    }
                }
            }
        }
    }

    public int getHeightAt(int wx, int wz) {
        double biome = biomeNoise.octaveNoise(wx * BIOME_SCALE, wz * BIOME_SCALE, 3, 0.5);
        double baseNoise = heightNoise.octaveNoise(wx * TERRAIN_SCALE, wz * TERRAIN_SCALE, 6, 0.5);
        double detail = detailNoise.octaveNoise(wx * DETAIL_SCALE, wz * DETAIL_SCALE, 4, 0.5);
        double heightMultiplier = 20 + biome * 15;
        return (int) (BASE_HEIGHT + baseNoise * heightMultiplier + detail * 4);
    }
}
