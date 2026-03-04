package com.minefuck;

import com.minefuck.engine.Window;
import com.minefuck.engine.Timer;
import com.minefuck.engine.input.InputHandler;
import com.minefuck.engine.input.MouseHandler;
import com.minefuck.renderer.MasterRenderer;
import com.minefuck.renderer.HUDRenderer;
import com.minefuck.world.World;
import com.minefuck.entity.Player;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Main game class that manages the game loop and all subsystems.
 */
public class Game {

    public static final String TITLE = "MineFuck";
    public static final int INITIAL_WIDTH = 1280;
    public static final int INITIAL_HEIGHT = 720;
    public static final float TARGET_UPS = 60.0f; // Updates per second

    private Window window;
    private Timer timer;
    private InputHandler input;
    private MouseHandler mouse;
    private MasterRenderer renderer;
    private HUDRenderer hudRenderer;
    private World world;
    private Player player;

    private boolean paused = false;
    private boolean debugMode = false;
    private int selectedBlock = 1;

    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void init() throws Exception {
        window = new Window(TITLE, INITIAL_WIDTH, INITIAL_HEIGHT);
        window.init();

        timer = new Timer();
        input = new InputHandler(window.getHandle());
        mouse = new MouseHandler(window.getHandle());

        renderer = new MasterRenderer(window);
        hudRenderer = new HUDRenderer(window);

        world = new World(42); // seed
        player = new Player(0, 80, 0);

        // Capture mouse
        mouse.setCaptured(true);
    }

    private void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!window.shouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            // Process input
            processInput();

            // Fixed timestep updates
            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            // Render
            render();

            // Swap buffers and poll events
            window.update();
        }
    }

    private void processInput() {
        input.update();
        mouse.update();

        // Escape to toggle pause/mouse capture
        if (input.isKeyPressed(GLFW_KEY_ESCAPE)) {
            paused = !paused;
            mouse.setCaptured(!paused);
        }

        // F3 for debug
        if (input.isKeyPressed(GLFW_KEY_F3)) {
            debugMode = !debugMode;
        }

        // F11 for fullscreen
        if (input.isKeyPressed(GLFW_KEY_F11)) {
            window.toggleFullscreen();
        }

        // Block selection (1-9)
        for (int i = GLFW_KEY_1; i <= GLFW_KEY_9; i++) {
            if (input.isKeyPressed(i)) {
                selectedBlock = i - GLFW_KEY_1 + 1;
            }
        }

        // Scroll wheel block selection
        double scroll = mouse.getScrollY();
        if (scroll != 0) {
            selectedBlock -= (int) scroll;
            if (selectedBlock < 1) selectedBlock = 9;
            if (selectedBlock > 9) selectedBlock = 1;
        }
    }

    private void update(float dt) {
        if (paused) return;

        // Player movement
        float moveX = 0, moveZ = 0;
        boolean jump = false;
        boolean sneak = false;

        if (input.isKeyDown(GLFW_KEY_W)) moveZ -= 1;
        if (input.isKeyDown(GLFW_KEY_S)) moveZ += 1;
        if (input.isKeyDown(GLFW_KEY_A)) moveX -= 1;
        if (input.isKeyDown(GLFW_KEY_D)) moveX += 1;
        if (input.isKeyDown(GLFW_KEY_SPACE)) jump = true;
        if (input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) sneak = true;

        float mouseDX = paused ? 0 : mouse.getDX();
        float mouseDY = paused ? 0 : mouse.getDY();

        player.handleInput(moveX, moveZ, jump, sneak, mouseDX, mouseDY);
        player.update(dt, world);

        // Block interaction
        if (!paused) {
            if (mouse.isLeftButtonPressed()) {
                player.breakBlock(world);
            }
            if (mouse.isRightButtonPressed()) {
                player.placeBlock(world, selectedBlock);
            }
        }

        // Update world (load/unload chunks around player)
        world.update(player.getX(), player.getY(), player.getZ());
    }

    private void render() {
        renderer.render(world, player);
        hudRenderer.render(selectedBlock, player, debugMode, world);
    }

    private void cleanup() {
        if (renderer != null) renderer.cleanup();
        if (hudRenderer != null) hudRenderer.cleanup();
        if (world != null) world.cleanup();
        if (window != null) window.cleanup();
    }
}
