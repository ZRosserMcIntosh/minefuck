package com.minefuck.renderer;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages an OpenGL shader program (vertex + fragment shader).
 */
public class ShaderProgram {

    private final int programId;

    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking shader program: " + glGetProgramInfoLog(programId, 1024));
        }

        // Shaders can be deleted after linking
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            String typeName = (type == GL_VERTEX_SHADER) ? "vertex" : "fragment";
            throw new RuntimeException("Error compiling " + typeName + " shader: " + glGetShaderInfoLog(shader, 1024));
        }

        return shader;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programId, name);
    }

    public void setUniformMatrix4f(String name, float[] matrix) {
        glUniformMatrix4fv(getUniformLocation(name), false, matrix);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        glUniform3f(getUniformLocation(name), x, y, z);
    }

    public void setUniform1f(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }

    public void setUniform1i(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}
