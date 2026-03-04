package com.minefuck.renderer;

import com.minefuck.engine.Window;
import com.minefuck.entity.Player;
import com.minefuck.world.Block;
import com.minefuck.world.World;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

/**
 * Renders the HUD overlay (crosshair, hotbar, debug info).
 */
public class HUDRenderer {

    private final Window window;
    private ShaderProgram hudShader;

    // Crosshair VAO
    private int crosshairVao;
    private int crosshairVertexCount;

    // Hotbar VAOs
    private int hotbarBgVao;
    private int hotbarSelectorVao;
    private int hotbarBlockVao;

    private final float[] matrixBuffer = new float[16];

    public HUDRenderer(Window window) throws Exception {
        this.window = window;
        init();
    }

    private void init() throws Exception {
        hudShader = new ShaderProgram(Shaders.HUD_VERTEX, Shaders.HUD_FRAGMENT);
        createCrosshair();
    }

    private void createCrosshair() {
        float size = 10.0f;
        float thickness = 1.5f;

        float[] vertices = {
                // Horizontal line
                -size, -thickness,
                 size, -thickness,
                 size,  thickness,
                -size, -thickness,
                 size,  thickness,
                -size,  thickness,
                // Vertical line
                -thickness, -size,
                 thickness, -size,
                 thickness,  size,
                -thickness, -size,
                 thickness,  size,
                -thickness,  size,
        };
        crosshairVertexCount = vertices.length / 2;

        crosshairVao = glGenVertexArrays();
        glBindVertexArray(crosshairVao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private int createQuad(float x, float y, float w, float h) {
        float[] vertices = {
                x, y,
                x + w, y,
                x + w, y + h,
                x, y,
                x + w, y + h,
                x, y + h,
        };

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return vao;
    }

    public void render(int selectedBlock, Player player, boolean debugMode, World world) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        hudShader.bind();

        // Orthographic projection centered on screen
        float w = window.getWidth();
        float h = window.getHeight();
        Matrix4f ortho = new Matrix4f().ortho(-w / 2, w / 2, -h / 2, h / 2, -1, 1);
        ortho.get(matrixBuffer);
        hudShader.setUniformMatrix4f("projection", matrixBuffer);

        // Draw crosshair
        hudShader.setUniform3f("color", 1.0f, 1.0f, 1.0f);
        hudShader.setUniform1f("alpha", 0.8f);

        glBindVertexArray(crosshairVao);
        glDrawArrays(GL_TRIANGLES, 0, crosshairVertexCount);
        glBindVertexArray(0);

        // Draw hotbar
        renderHotbar(selectedBlock, w, h);

        hudShader.unbind();

        glEnable(GL_DEPTH_TEST);
    }

    private void renderHotbar(int selectedBlock, float screenW, float screenH) {
        float slotSize = 40.0f;
        float padding = 4.0f;
        float totalWidth = 9 * slotSize + 8 * padding;
        float startX = -totalWidth / 2;
        float startY = -screenH / 2 + 10;

        for (int i = 0; i < 9; i++) {
            float x = startX + i * (slotSize + padding);
            float y = startY;

            // Slot background
            if (i + 1 == selectedBlock) {
                hudShader.setUniform3f("color", 1.0f, 1.0f, 1.0f);
                hudShader.setUniform1f("alpha", 0.5f);
            } else {
                hudShader.setUniform3f("color", 0.2f, 0.2f, 0.2f);
                hudShader.setUniform1f("alpha", 0.5f);
            }

            int slotVao = createQuad(x, y, slotSize, slotSize);
            glBindVertexArray(slotVao);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
            glDeleteVertexArrays(slotVao);

            // Block color preview inside slot
            float[] color = Block.getColor(Block.fromSlot(i + 1));
            hudShader.setUniform3f("color", color[0], color[1], color[2]);
            hudShader.setUniform1f("alpha", 0.9f);

            float innerPad = 6.0f;
            int blockVao = createQuad(x + innerPad, y + innerPad,
                    slotSize - innerPad * 2, slotSize - innerPad * 2);
            glBindVertexArray(blockVao);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
            glDeleteVertexArrays(blockVao);
        }
    }

    public void cleanup() {
        if (hudShader != null) hudShader.cleanup();
    }
}
