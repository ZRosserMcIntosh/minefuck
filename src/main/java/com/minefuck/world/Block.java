package com.minefuck.world;

/**
 * Defines all block types in the game.
 */
public class Block {

    public static final byte AIR = 0;
    public static final byte GRASS = 1;
    public static final byte DIRT = 2;
    public static final byte STONE = 3;
    public static final byte SAND = 4;
    public static final byte WATER = 5;
    public static final byte WOOD = 6;
    public static final byte LEAVES = 7;
    public static final byte COBBLESTONE = 8;
    public static final byte BEDROCK = 9;
    public static final byte GRAVEL = 10;
    public static final byte PLANKS = 11;
    public static final byte GLASS = 12;
    public static final byte COAL_ORE = 13;
    public static final byte IRON_ORE = 14;
    public static final byte SNOW = 15;

    public static final int NUM_TYPES = 16;

    private static final String[] NAMES = {
        "Air", "Grass", "Dirt", "Stone", "Sand", "Water",
        "Wood", "Leaves", "Cobblestone", "Bedrock", "Gravel",
        "Planks", "Glass", "Coal Ore", "Iron Ore", "Snow"
    };

    // Colors for each block type (R, G, B) - used for procedural textures
    private static final float[][] COLORS = {
        {0.0f, 0.0f, 0.0f},       // AIR (not rendered)
        {0.30f, 0.65f, 0.20f},     // GRASS (top)
        {0.55f, 0.37f, 0.24f},     // DIRT
        {0.50f, 0.50f, 0.50f},     // STONE
        {0.85f, 0.80f, 0.55f},     // SAND
        {0.20f, 0.40f, 0.80f},     // WATER
        {0.45f, 0.30f, 0.15f},     // WOOD
        {0.15f, 0.50f, 0.10f},     // LEAVES
        {0.40f, 0.40f, 0.40f},     // COBBLESTONE
        {0.15f, 0.15f, 0.15f},     // BEDROCK
        {0.55f, 0.52f, 0.50f},     // GRAVEL
        {0.70f, 0.55f, 0.30f},     // PLANKS
        {0.75f, 0.85f, 0.90f},     // GLASS
        {0.35f, 0.35f, 0.35f},     // COAL ORE
        {0.60f, 0.50f, 0.45f},     // IRON ORE
        {0.95f, 0.95f, 0.95f},     // SNOW
    };

    // Grass side color (brown-ish with green top)
    private static final float[] GRASS_SIDE = {0.45f, 0.35f, 0.22f};
    private static final float[] GRASS_BOTTOM = {0.55f, 0.37f, 0.24f}; // same as dirt

    public static String getName(int type) {
        if (type >= 0 && type < NAMES.length) return NAMES[type];
        return "Unknown";
    }

    public static float[] getColor(int type) {
        if (type >= 0 && type < COLORS.length) return COLORS[type];
        return new float[]{1.0f, 0.0f, 1.0f}; // magenta for unknown
    }

    /**
     * Get color for a specific face of a block.
     * Face: 0=top, 1=bottom, 2=front, 3=back, 4=left, 5=right
     */
    public static float[] getFaceColor(int type, int face) {
        if (type == GRASS) {
            if (face == 0) return COLORS[GRASS]; // green top
            if (face == 1) return GRASS_BOTTOM;   // dirt bottom
            return GRASS_SIDE;                     // sides
        }
        return getColor(type);
    }

    public static boolean isTransparent(int type) {
        return type == AIR || type == WATER || type == GLASS || type == LEAVES;
    }

    public static boolean isSolid(int type) {
        return type != AIR && type != WATER;
    }

    public static boolean isLiquid(int type) {
        return type == WATER;
    }

    /** Get block type from hotbar slot (1-9) */
    public static byte fromSlot(int slot) {
        return switch (slot) {
            case 1 -> GRASS;
            case 2 -> DIRT;
            case 3 -> STONE;
            case 4 -> SAND;
            case 5 -> WOOD;
            case 6 -> PLANKS;
            case 7 -> COBBLESTONE;
            case 8 -> GLASS;
            case 9 -> LEAVES;
            default -> GRASS;
        };
    }
}
