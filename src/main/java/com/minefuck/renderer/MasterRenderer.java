package com.minefuck.renderer;

import com.minefuck.engine.Window;
import com.minefuck.entity.Player;
import com.minefuck.world.Block;
import com.minefuck.world.Chunk;
import com.minefuck.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

/**
 * Master renderer that manages all rendering subsystems.
 */
public class MasterRenderer {

    private static final float FOV = 70.0f;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 300.0f;

    private final Window window;
    private ShaderProgram worldShader;
    private ShaderProgram skyShader;
    private ShaderProgram highlightShader;

    // Sky colors (day)
    private final Vector3f skyTopColor = new Vector3f(0.4f, 0.65f, 1.0f);
    private final Vector3f skyBottomColor = new Vector3f(0.7f, 0.85f, 1.0f);
    private final Vector3f skyColor = new Vector3f(0.6f, 0.78f, 1.0f); // fog color

    // Lighting
    private final Vector3f lightDir = new Vector3f(0.4f, 0.8f, 0.3f).normalize();

    // Sky quad
    private int skyVao;

    // Block highlight
    private int highlightVao;

    // Matrices
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final float[] matrixBuffer = new float[16];

    // Frustum culling planes
    private final float[][] frustumPlanes = new float[6][4];

    public MasterRenderer(Window window) throws Exception {
        this.window = window;
        init();
    }

    private void init() throws Exception {
        worldShader = new ShaderProgram(Shaders.WORLD_VERTEX, Shaders.WORLD_FRAGMENT);
        skyShader = new ShaderProgram(Shaders.SKY_VERTEX, Shaders.SKY_FRAGMENT);
        highlightShader = new ShaderProgram(Shaders.HIGHLIGHT_VERTEX, Shaders.HIGHLIGHT_FRAGMENT);

        createSkyQuad();
        createHighlightCube();
    }

    private void createSkyQuad() {
        float[] vertices = {
                -1, -1, 0,
                 1, -1, 0,
                 1,  1, 0,
                -1, -1, 0,
                 1,  1, 0,
                -1,  1, 0,
        };

        skyVao = glGenVertexArrays();
        glBindVertexArray(skyVao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void createHighlightCube() {
        // Wireframe-style cube using lines (all 12 edges)
        float[] vertices = {
                // Bottom face
                0, 0, 0,  1, 0, 0,
                1, 0, 0,  1, 0, 1,
                1, 0, 1,  0, 0, 1,
                0, 0, 1,  0, 0, 0,
                // Top face
                0, 1, 0,  1, 1, 0,
                1, 1, 0,  1, 1, 1,
                1, 1, 1,  0, 1, 1,
                0, 1, 1,  0, 1, 0,
                // Vertical edges
                0, 0, 0,  0, 1, 0,
                1, 0, 0,  1, 1, 0,
                1, 0, 1,  1, 1, 1,
                0, 0, 1,  0, 1, 1,
        };

        highlightVao = glGenVertexArrays();
        glBindVertexArray(highlightVao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(World world, Player player) {
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Update projection matrix
        float aspect = window.getAspectRatio();
        projectionMatrix.identity().perspective(
                (float) Math.toRadians(FOV), aspect, NEAR_PLANE, FAR_PLANE);

        // Update view matrix
        updateViewMatrix(player);

        // Extract frustum planes
        extractFrustumPlanes();

        // Render sky
        renderSky();

        // Render world
        renderWorld(world, player);

        // Render block highlight
        renderBlockHighlight(world, player);
    }

    private void updateViewMatrix(Player player) {
        viewMatrix.identity();
        viewMatrix.rotateX((float) Math.toRadians(-player.getPitch()));
        viewMatrix.rotateY((float) Math.toRadians(-player.getYaw()));
        viewMatrix.translate(-player.getX(), -player.getEyeY(), -player.getZ());
    }

    private void renderSky() {
        glDisable(GL_DEPTH_TEST);

        skyShader.bind();
        skyShader.setUniform3f("topColor", skyTopColor.x, skyTopColor.y, skyTopColor.z);
        skyShader.setUniform3f("bottomColor", skyBottomColor.x, skyBottomColor.y, skyBottomColor.z);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        skyShader.unbind();
        glEnable(GL_DEPTH_TEST);
    }

    private void renderWorld(World world, Player player) {
        worldShader.bind();

        // Upload matrices
        projectionMatrix.get(matrixBuffer);
        worldShader.setUniformMatrix4f("projection", matrixBuffer);

        viewMatrix.get(matrixBuffer);
        worldShader.setUniformMatrix4f("view", matrixBuffer);

        // Lighting
        worldShader.setUniform3f("lightDir", lightDir.x, lightDir.y, lightDir.z);
        worldShader.setUniform1f("ambientStrength", 0.4f);
        worldShader.setUniform3f("skyColor", skyColor.x, skyColor.y, skyColor.z);

        // Fog
        float fogStart = (World.RENDER_DISTANCE - 2) * Chunk.SIZE;
        float fogEnd = World.RENDER_DISTANCE * Chunk.SIZE;
        worldShader.setUniform1f("fogStart", fogStart);
        worldShader.setUniform1f("fogEnd", fogEnd);
        worldShader.setUniform3f("cameraPos", player.getX(), player.getEyeY(), player.getZ());

        // Render chunks
        for (Chunk chunk : world.getLoadedChunks()) {
            if (!chunk.isGenerated()) continue;

            // Rebuild mesh if dirty
            if (chunk.isDirty()) {
                ChunkMesh mesh = ChunkMeshBuilder.buildMesh(chunk, world);
                chunk.setMesh(mesh);
            }

            // Frustum culling
            if (!isChunkInFrustum(chunk)) continue;

            ChunkMesh mesh = chunk.getMesh();
            if (mesh != null) {
                mesh.render();
            }
        }

        worldShader.unbind();
    }

    private void renderBlockHighlight(World world, Player player) {
        Vector3f look = player.getLookDir();
        int[] hit = world.raycast(
                player.getX(), player.getEyeY(), player.getZ(),
                look.x, look.y, look.z, 5.0f);

        if (hit == null) return;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glLineWidth(2.0f);

        highlightShader.bind();

        projectionMatrix.get(matrixBuffer);
        highlightShader.setUniformMatrix4f("projection", matrixBuffer);

        viewMatrix.get(matrixBuffer);
        highlightShader.setUniformMatrix4f("view", matrixBuffer);

        highlightShader.setUniform3f("blockPos", hit[0], hit[1], hit[2]);

        glBindVertexArray(highlightVao);
        glDrawArrays(GL_LINES, 0, 24);
        glBindVertexArray(0);

        highlightShader.unbind();
    }

    private void extractFrustumPlanes() {
        float[] proj = new float[16];
        float[] view = new float[16];
        projectionMatrix.get(proj);
        viewMatrix.get(view);

        // Multiply projection * view
        float[] clip = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                clip[i * 4 + j] = 0;
                for (int k = 0; k < 4; k++) {
                    clip[i * 4 + j] += proj[k * 4 + j] * view[i * 4 + k];
                }
            }
        }

        // Extract planes
        // Left
        frustumPlanes[0][0] = clip[3] + clip[0];
        frustumPlanes[0][1] = clip[7] + clip[4];
        frustumPlanes[0][2] = clip[11] + clip[8];
        frustumPlanes[0][3] = clip[15] + clip[12];
        // Right
        frustumPlanes[1][0] = clip[3] - clip[0];
        frustumPlanes[1][1] = clip[7] - clip[4];
        frustumPlanes[1][2] = clip[11] - clip[8];
        frustumPlanes[1][3] = clip[15] - clip[12];
        // Bottom
        frustumPlanes[2][0] = clip[3] + clip[1];
        frustumPlanes[2][1] = clip[7] + clip[5];
        frustumPlanes[2][2] = clip[11] + clip[9];
        frustumPlanes[2][3] = clip[15] + clip[13];
        // Top
        frustumPlanes[3][0] = clip[3] - clip[1];
        frustumPlanes[3][1] = clip[7] - clip[5];
        frustumPlanes[3][2] = clip[11] - clip[9];
        frustumPlanes[3][3] = clip[15] - clip[13];
        // Near
        frustumPlanes[4][0] = clip[3] + clip[2];
        frustumPlanes[4][1] = clip[7] + clip[6];
        frustumPlanes[4][2] = clip[11] + clip[10];
        frustumPlanes[4][3] = clip[15] + clip[14];
        // Far
        frustumPlanes[5][0] = clip[3] - clip[2];
        frustumPlanes[5][1] = clip[7] - clip[6];
        frustumPlanes[5][2] = clip[11] - clip[10];
        frustumPlanes[5][3] = clip[15] - clip[14];

        // Normalize planes
        for (int i = 0; i < 6; i++) {
            float len = (float) Math.sqrt(
                    frustumPlanes[i][0] * frustumPlanes[i][0] +
                    frustumPlanes[i][1] * frustumPlanes[i][1] +
                    frustumPlanes[i][2] * frustumPlanes[i][2]);
            if (len > 0) {
                frustumPlanes[i][0] /= len;
                frustumPlanes[i][1] /= len;
                frustumPlanes[i][2] /= len;
                frustumPlanes[i][3] /= len;
            }
        }
    }

    private boolean isChunkInFrustum(Chunk chunk) {
        float minX = chunk.getWorldX();
        float minY = 0;
        float minZ = chunk.getWorldZ();
        float maxX = minX + Chunk.SIZE;
        float maxY = Chunk.HEIGHT;
        float maxZ = minZ + Chunk.SIZE;

        for (int i = 0; i < 6; i++) {
            float px = frustumPlanes[i][0] > 0 ? maxX : minX;
            float py = frustumPlanes[i][1] > 0 ? maxY : minY;
            float pz = frustumPlanes[i][2] > 0 ? maxZ : minZ;

            if (frustumPlanes[i][0] * px + frustumPlanes[i][1] * py +
                frustumPlanes[i][2] * pz + frustumPlanes[i][3] < 0) {
                return false;
            }
        }
        return true;
    }

    public void cleanup() {
        if (worldShader != null) worldShader.cleanup();
        if (skyShader != null) skyShader.cleanup();
        if (highlightShader != null) highlightShader.cleanup();
    }
}
