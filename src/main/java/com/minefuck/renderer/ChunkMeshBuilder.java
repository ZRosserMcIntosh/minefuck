package com.minefuck.renderer;

import com.minefuck.world.Block;
import com.minefuck.world.Chunk;
import com.minefuck.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the mesh for a chunk by only creating faces between solid and transparent blocks.
 * This is the greedy/culled mesh approach that makes voxel rendering efficient.
 */
public class ChunkMeshBuilder {

    // Face vertices for a unit cube - each face is 2 triangles (6 vertices)
    // Top face (Y+)
    private static final float[][] TOP_FACE = {
            {0, 1, 0}, {0, 1, 1}, {1, 1, 1},
            {0, 1, 0}, {1, 1, 1}, {1, 1, 0}
    };
    // Bottom face (Y-)
    private static final float[][] BOTTOM_FACE = {
            {0, 0, 1}, {0, 0, 0}, {1, 0, 0},
            {0, 0, 1}, {1, 0, 0}, {1, 0, 1}
    };
    // Front face (Z+)
    private static final float[][] FRONT_FACE = {
            {0, 0, 1}, {1, 0, 1}, {1, 1, 1},
            {0, 0, 1}, {1, 1, 1}, {0, 1, 1}
    };
    // Back face (Z-)
    private static final float[][] BACK_FACE = {
            {1, 0, 0}, {0, 0, 0}, {0, 1, 0},
            {1, 0, 0}, {0, 1, 0}, {1, 1, 0}
    };
    // Left face (X-)
    private static final float[][] LEFT_FACE = {
            {0, 0, 0}, {0, 0, 1}, {0, 1, 1},
            {0, 0, 0}, {0, 1, 1}, {0, 1, 0}
    };
    // Right face (X+)
    private static final float[][] RIGHT_FACE = {
            {1, 0, 1}, {1, 0, 0}, {1, 1, 0},
            {1, 0, 1}, {1, 1, 0}, {1, 1, 1}
    };

    // Normals for each face
    private static final float[][] FACE_NORMALS = {
            {0, 1, 0},  // Top
            {0, -1, 0}, // Bottom
            {0, 0, 1},  // Front (south, +Z)
            {0, 0, -1}, // Back (north, -Z)
            {-1, 0, 0}, // Left (west)
            {1, 0, 0},  // Right (east)
    };

    public static ChunkMesh buildMesh(Chunk chunk, World world) {
        List<Float> vertices = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        int worldX = chunk.getWorldX();
        int worldZ = chunk.getWorldZ();

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    byte block = chunk.getBlock(x, y, z);
                    if (block == Block.AIR) continue;

                    int wx = worldX + x;
                    int wz = worldZ + z;

                    // Check each face - only add if neighbor is transparent
                    // Top (Y+)
                    if (shouldRenderFace(block, world.getBlock(wx, y + 1, wz))) {
                        addFace(vertices, colors, normals, TOP_FACE, 0,
                                wx, y, wz, block);
                    }
                    // Bottom (Y-)
                    if (shouldRenderFace(block, world.getBlock(wx, y - 1, wz))) {
                        addFace(vertices, colors, normals, BOTTOM_FACE, 1,
                                wx, y, wz, block);
                    }
                    // Front/South (Z+)
                    if (shouldRenderFace(block, world.getBlock(wx, y, wz + 1))) {
                        addFace(vertices, colors, normals, FRONT_FACE, 2,
                                wx, y, wz, block);
                    }
                    // Back/North (Z-)
                    if (shouldRenderFace(block, world.getBlock(wx, y, wz - 1))) {
                        addFace(vertices, colors, normals, BACK_FACE, 3,
                                wx, y, wz, block);
                    }
                    // Left/West (X-)
                    if (shouldRenderFace(block, world.getBlock(wx - 1, y, wz))) {
                        addFace(vertices, colors, normals, LEFT_FACE, 4,
                                wx, y, wz, block);
                    }
                    // Right/East (X+)
                    if (shouldRenderFace(block, world.getBlock(wx + 1, y, wz))) {
                        addFace(vertices, colors, normals, RIGHT_FACE, 5,
                                wx, y, wz, block);
                    }
                }
            }
        }

        // Convert to arrays
        float[] vertArray = toFloatArray(vertices);
        float[] colorArray = toFloatArray(colors);
        float[] normalArray = toFloatArray(normals);

        return new ChunkMesh(vertArray, colorArray, normalArray);
    }

    private static boolean shouldRenderFace(byte block, byte neighbor) {
        if (block == neighbor) return false;
        if (Block.isTransparent(neighbor)) return true;
        return false;
    }

    private static void addFace(List<Float> vertices, List<Float> colors, List<Float> normals,
                                 float[][] face, int faceIndex, int x, int y, int z, byte block) {
        float[] color = Block.getFaceColor(block, faceIndex);

        // Slight ambient occlusion-like darkening based on face
        float shade = 1.0f;
        switch (faceIndex) {
            case 0 -> shade = 1.0f;   // top - brightest
            case 1 -> shade = 0.5f;   // bottom - darkest
            case 2, 3 -> shade = 0.8f; // front/back
            case 4, 5 -> shade = 0.7f; // left/right
        }

        // Water transparency
        float alpha = (block == Block.WATER) ? 0.6f : 1.0f;

        for (float[] vertex : face) {
            vertices.add(x + vertex[0]);
            vertices.add(y + vertex[1]);
            vertices.add(z + vertex[2]);

            colors.add(color[0] * shade);
            colors.add(color[1] * shade);
            colors.add(color[2] * shade);

            normals.add(FACE_NORMALS[faceIndex][0]);
            normals.add(FACE_NORMALS[faceIndex][1]);
            normals.add(FACE_NORMALS[faceIndex][2]);
        }
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
