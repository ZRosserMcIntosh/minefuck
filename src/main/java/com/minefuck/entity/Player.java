package com.minefuck.entity;

import com.minefuck.world.Block;
import com.minefuck.world.World;
import org.joml.Vector3f;

/**
 * The player entity with first-person camera, movement, physics, and block interaction.
 */
public class Player {

    // Position
    private float x, y, z;

    // Camera rotation
    private float yaw = 0;   // horizontal rotation (degrees)
    private float pitch = 0; // vertical rotation (degrees)

    // Velocity
    private float vx, vy, vz;

    // Physics constants
    private static final float MOVE_SPEED = 4.317f;    // blocks per second (Minecraft walk speed)
    private static final float SPRINT_SPEED = 5.612f;
    private static final float SNEAK_SPEED = 1.3f;
    private static final float JUMP_VELOCITY = 8.0f;
    private static final float GRAVITY = -25.0f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float PLAYER_HEIGHT = 1.62f;   // eye height
    private static final float PLAYER_TOTAL_HEIGHT = 1.8f;
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float REACH_DISTANCE = 5.0f;

    private boolean onGround = false;
    private boolean sneaking = false;

    // Look direction vector
    private final Vector3f lookDir = new Vector3f();

    public Player(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void handleInput(float moveX, float moveZ, boolean jump, boolean sneak, float mouseDX, float mouseDY) {
        // Mouse look - mouseDY is screen delta (positive = mouse moved down = look down = decrease pitch)
        yaw += mouseDX * MOUSE_SENSITIVITY;
        pitch += mouseDY * MOUSE_SENSITIVITY;

        // Clamp pitch
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        // Normalize yaw
        while (yaw > 360) yaw -= 360;
        while (yaw < 0) yaw += 360;

        // Calculate movement direction
        float speed = sneak ? SNEAK_SPEED : MOVE_SPEED;
        sneaking = sneak;

        float yawRad = (float) Math.toRadians(yaw);
        float forwardX = -(float) Math.sin(yawRad);
        float forwardZ = -(float) Math.cos(yawRad);
        float rightX = (float) Math.cos(yawRad);
        float rightZ = -(float) Math.sin(yawRad);

        // Horizontal velocity from input: forward/back along look direction, strafe perpendicular
        float targetVX = (moveX * rightX + moveZ * forwardX) * speed;
        float targetVZ = (moveX * rightZ + moveZ * forwardZ) * speed;

        // Normalize diagonal movement
        if (moveX != 0 && moveZ != 0) {
            float len = (float) Math.sqrt(targetVX * targetVX + targetVZ * targetVZ);
            if (len > 0) {
                targetVX = targetVX / len * speed;
                targetVZ = targetVZ / len * speed;
            }
        }

        vx = targetVX;
        vz = targetVZ;

        // Jump
        if (jump && onGround) {
            vy = JUMP_VELOCITY;
            onGround = false;
        }

        // Update look direction
        float pitchRad = (float) Math.toRadians(pitch);
        lookDir.x = (float) (Math.cos(pitchRad) * (-Math.sin(yawRad)));
        lookDir.y = (float) Math.sin(pitchRad);
        lookDir.z = (float) (Math.cos(pitchRad) * (-Math.cos(yawRad)));
        lookDir.normalize();
    }

    public void update(float dt, World world) {
        // Apply gravity
        vy += GRAVITY * dt;

        // Terminal velocity
        if (vy < -50) vy = -50;

        // Try to move with collision detection
        float newX = x + vx * dt;
        float newY = y + vy * dt;
        float newZ = z + vz * dt;

        // Simple AABB collision
        float halfWidth = PLAYER_WIDTH / 2.0f;

        // X-axis collision
        if (vx != 0) {
            if (isColliding(newX, y, z, halfWidth, world)) {
                newX = x;
                vx = 0;
            }
        }

        // Z-axis collision
        if (vz != 0) {
            if (isColliding(newX, y, newZ, halfWidth, world)) {
                newZ = z;
                vz = 0;
            }
        }

        // Y-axis collision
        if (vy != 0) {
            if (isColliding(newX, newY, newZ, halfWidth, world)) {
                if (vy < 0) {
                    // Landing on ground
                    newY = (float) Math.ceil(newY);
                    onGround = true;
                } else {
                    // Hit ceiling
                    newY = y;
                }
                vy = 0;
            } else {
                onGround = false;
            }
        }

        x = newX;
        y = newY;
        z = newZ;

        // Keep player above bedrock
        if (y < 1) {
            y = 1;
            vy = 0;
            onGround = true;
        }
    }

    private boolean isColliding(float px, float py, float pz, float halfW, World world) {
        // Check several points around the player's AABB
        float feetY = py;
        float headY = py + PLAYER_TOTAL_HEIGHT - 0.01f;

        for (float ox = -halfW; ox <= halfW; ox += halfW) {
            for (float oz = -halfW; oz <= halfW; oz += halfW) {
                for (float oy = 0; oy <= PLAYER_TOTAL_HEIGHT; oy += 0.9f) {
                    int bx = (int) Math.floor(px + ox);
                    int by = (int) Math.floor(py + oy);
                    int bz = (int) Math.floor(pz + oz);

                    byte block = world.getBlock(bx, by, bz);
                    if (Block.isSolid(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void breakBlock(World world) {
        float eyeX = x;
        float eyeY = y + PLAYER_HEIGHT;
        float eyeZ = z;

        int[] hit = world.raycast(eyeX, eyeY, eyeZ, lookDir.x, lookDir.y, lookDir.z, REACH_DISTANCE);
        if (hit != null) {
            world.setBlock(hit[0], hit[1], hit[2], Block.AIR);
        }
    }

    public void placeBlock(World world, int slotIndex) {
        float eyeX = x;
        float eyeY = y + PLAYER_HEIGHT;
        float eyeZ = z;

        int[] hit = world.raycast(eyeX, eyeY, eyeZ, lookDir.x, lookDir.y, lookDir.z, REACH_DISTANCE);
        if (hit != null && hit.length >= 7) {
            int px = hit[4]; // previous block position (air block adjacent to hit face)
            int py = hit[5];
            int pz = hit[6];

            if (px != Integer.MIN_VALUE) {
                // Don't place inside the player
                float halfW = PLAYER_WIDTH / 2.0f;
                float playerMinX = x - halfW;
                float playerMaxX = x + halfW;
                float playerMinY = y;
                float playerMaxY = y + PLAYER_TOTAL_HEIGHT;
                float playerMinZ = z - halfW;
                float playerMaxZ = z + halfW;

                if (px + 1 > playerMinX && px < playerMaxX &&
                    py + 1 > playerMinY && py < playerMaxY &&
                    pz + 1 > playerMinZ && pz < playerMaxZ) {
                    return; // Would place inside player
                }

                world.setBlock(px, py, pz, Block.fromSlot(slotIndex));
            }
        }
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getEyeY() { return y + PLAYER_HEIGHT; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getVY() { return vy; }
    public boolean isOnGround() { return onGround; }
    public boolean isSneaking() { return sneaking; }
    public Vector3f getLookDir() { return lookDir; }
}
