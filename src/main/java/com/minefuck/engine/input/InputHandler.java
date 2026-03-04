package com.minefuck.engine.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles keyboard input.
 */
public class InputHandler {

    private final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] prevKeys = new boolean[GLFW_KEY_LAST + 1];

    public InputHandler(long window) {
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = (action != GLFW_RELEASE);
            }
        });
    }

    public void update() {
        for (int i = 0; i < keys.length; i++) {
            keysPressed[i] = keys[i] && !prevKeys[i];
            prevKeys[i] = keys[i];
        }
    }

    /** Returns true if key is currently held down */
    public boolean isKeyDown(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keys[key];
    }

    /** Returns true only on the frame the key was first pressed */
    public boolean isKeyPressed(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keysPressed[key];
    }
}
