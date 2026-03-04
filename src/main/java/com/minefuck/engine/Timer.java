package com.minefuck.engine;

/**
 * High-resolution timer for the game loop.
 */
public class Timer {

    private double lastLoopTime;

    public Timer() {
        lastLoopTime = getTime();
    }

    public double getTime() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }
}
