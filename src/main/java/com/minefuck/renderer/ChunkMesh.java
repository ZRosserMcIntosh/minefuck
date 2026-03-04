package com.minefuck.renderer;

import static org.lwjgl.opengl.GL30.*;

/**
 * Represents the mesh data for a single chunk, uploaded to the GPU.
 */
public class ChunkMesh {

    private int vaoId;
    private int vboVertices;
    private int vboColors;
    private int vboNormals;
    private int vertexCount;

    public ChunkMesh(float[] vertices, float[] colors, float[] normals) {
        this.vertexCount = vertices.length / 3;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Vertices (location 0)
        vboVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // Colors (location 1)
        vboColors = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColors);
        glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // Normals (location 2)
        vboNormals = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormals);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        if (vertexCount == 0) return;

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public int getVertexCount() { return vertexCount; }

    public void cleanup() {
        glDeleteBuffers(vboVertices);
        glDeleteBuffers(vboColors);
        glDeleteBuffers(vboNormals);
        glDeleteVertexArrays(vaoId);
    }
}
