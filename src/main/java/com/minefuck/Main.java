package com.minefuck;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * MineFuck - A vanilla Minecraft clone
 * Entry point for the application.
 * 
 * An experimental project — part of a bigger vision:
 * a game for the kids (Ross's children and Stella's daughter).
 * 
 * Automatically handles macOS -XstartOnFirstThread requirement
 * and works on Windows, macOS (Intel + Apple Silicon), and Linux.
 * Includes auto-updating from GitHub Releases.
 */
public class Main {
    
    private static final String RESTART_FLAG = "minefuck.restarted";

    public static void main(String[] args) {
        // On macOS, GLFW requires the main thread. If we haven't already
        // restarted with -XstartOnFirstThread, do it now automatically.
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") && System.getProperty(RESTART_FLAG) == null) {
            // Check if -XstartOnFirstThread is already set
            List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            boolean hasFirstThread = vmArgs.stream()
                    .anyMatch(arg -> arg.contains("XstartOnFirstThread"));

            if (!hasFirstThread) {
                // Restart the JVM with -XstartOnFirstThread
                try {
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
                    String classpath = System.getProperty("java.class.path");
                    
                    List<String> command = new ArrayList<>();
                    command.add(javaBin);
                    command.add("-XstartOnFirstThread");
                    command.add("-D" + RESTART_FLAG + "=true");
                    command.addAll(vmArgs);
                    command.add("-cp");
                    command.add(classpath);
                    command.add(Main.class.getName());
                    for (String arg : args) {
                        command.add(arg);
                    }
                    
                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.inheritIO();
                    Process process = pb.start();
                    System.exit(process.waitFor());
                } catch (Exception e) {
                    System.err.println("Failed to restart with -XstartOnFirstThread: " + e.getMessage());
                    System.err.println("Please run manually with: java -XstartOnFirstThread -jar minefuck.jar");
                    System.exit(1);
                }
                return;
            }
        }

        System.out.println("=================================");
        System.out.println("  MineFuck v" + AutoUpdater.CURRENT_VERSION);
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  Java: " + System.getProperty("java.version"));
        System.out.println("  For the kids. ❤️");
        System.out.println("=================================");

        // Apply any pending update from a previous session
        AutoUpdater.applyPendingUpdate();

        // Check for updates (non-blocking, won't prevent game from starting if offline)
        boolean needsRestart = AutoUpdater.checkAndUpdate();
        if (needsRestart) {
            System.out.println("Update applied! Please restart MineFuck to use the new version.");
            System.out.println("Launching game with current version for now...");
        }

        Game game = new Game();
        game.run();
    }
}
