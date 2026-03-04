package com.minefuck.engine.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles mouse input including position, buttons, and scroll.
 */
public class MouseHandler {

    private final long window;
    private double mouseX, mouseY;
    private double prevMouseX, prevMouseY;
    private float dx, dy;
    private double scrollY;

    private boolean leftPressed, rightPressed;
    private boolean leftDown, rightDown;
    private boolean prevLeftDown, prevRightDown;

    private boolean firstMouse = true;
    private boolean captured = false;

    public MouseHandler(long window) {
        this.window = window;

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                leftDown = (action != GLFW_RELEASE);
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                rightDown = (action != GLFW_RELEASE);
            }
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollY = yoffset;
        });
    }

    public void update() {
        if (firstMouse) {
            prevMouseX = mouseX;
            prevMouseY = mouseY;
            firstMouse = false;
        }

        dx = (float) (mouseX - prevMouseX);
        dy = (float) (mouseY - prevMouseY);
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        leftPressed = leftDown && !prevLeftDown;
        rightPressed = rightDown && !prevRightDown;
        prevLeftDown = leftDown;
        prevRightDown = rightDown;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
        if (captured) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            firstMouse = true;
        } else {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public float getDX() { return dx; }
    public float getDY() { return dy; }
    public double getScrollY() {
        double val = scrollY;
        scrollY = 0;
        return val;
    }

    public boolean isLeftButtonPressed() { return leftPressed; }
    public boolean isRightButtonPressed() { return rightPressed; }
    public boolean isLeftButtonDown() { return leftDown; }
    public boolean isRightButtonDown() { return rightDown; }
    public boolean isCaptured() { return captured; }
}
