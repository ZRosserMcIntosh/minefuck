package com.minefuck;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auto-updater that checks GitHub Releases for new versions on startup.
 * Downloads and applies updates seamlessly.
 */
public class AutoUpdater {

    // Current version - bump this with each release
    public static final String CURRENT_VERSION = "1.0.0";

    private static final String GITHUB_API = "https://api.github.com/repos/ZRosserMcIntosh/minefuck/releases/latest";
    private static final String USER_AGENT = "MineFuck-Updater/" + CURRENT_VERSION;
    private static final int TIMEOUT_MS = 8000;

    /**
     * Check for updates and apply if available.
     * Returns true if the game should restart with the new version.
     */
    public static boolean checkAndUpdate() {
        try {
            System.out.println("[Updater] Checking for updates...");
            System.out.println("[Updater] Current version: " + CURRENT_VERSION);

            // Get latest release info from GitHub API
            String releaseJson = httpGet(GITHUB_API);
            if (releaseJson == null) {
                System.out.println("[Updater] Could not reach GitHub. Skipping update check.");
                return false;
            }

            // Parse version from release
            String latestVersion = parseJsonField(releaseJson, "tag_name");
            if (latestVersion == null) {
                System.out.println("[Updater] Could not parse release version. Skipping.");
                return false;
            }

            // Clean version string (remove 'v' prefix, 'latest' tag, etc.)
            latestVersion = latestVersion.replaceAll("^[vV]", "").trim();

            // Check the body for a version marker like "Version: 1.0.1"
            String body = parseJsonField(releaseJson, "body");
            if (body != null) {
                Matcher versionMatcher = Pattern.compile("Version:\\s*(\\d+\\.\\d+\\.\\d+)").matcher(body);
                if (versionMatcher.find()) {
                    latestVersion = versionMatcher.group(1);
                }
            }

            // If tag is "latest", check published_at date or name for version info
            if (latestVersion.equalsIgnoreCase("latest")) {
                String releaseName = parseJsonField(releaseJson, "name");
                if (releaseName != null) {
                    Matcher m = Pattern.compile("(\\d+\\.\\d+\\.\\d+)").matcher(releaseName);
                    if (m.find()) {
                        latestVersion = m.group(1);
                    }
                }
            }

            System.out.println("[Updater] Latest version: " + latestVersion);

            if (latestVersion.equalsIgnoreCase("latest") || !isNewerVersion(latestVersion, CURRENT_VERSION)) {
                System.out.println("[Updater] Already up to date!");
                return false;
            }

            System.out.println("[Updater] New version available: " + latestVersion);

            // Find the right download URL
            String downloadUrl = findDownloadUrl(releaseJson);
            if (downloadUrl == null) {
                System.out.println("[Updater] Could not find download URL. Skipping.");
                return false;
            }

            System.out.println("[Updater] Downloading update from: " + downloadUrl);

            // Determine where we're running from
            Path currentJar = getCurrentJarPath();
            if (currentJar == null) {
                System.out.println("[Updater] Not running from a JAR/EXE. Skipping auto-update.");
                return false;
            }

            // Download to temp file
            Path tempFile = currentJar.getParent().resolve(".minefuck-update.tmp");
            if (!downloadFile(downloadUrl, tempFile)) {
                System.out.println("[Updater] Download failed. Skipping.");
                Files.deleteIfExists(tempFile);
                return false;
            }

            // Verify download is reasonable size (> 1MB)
            long size = Files.size(tempFile);
            if (size < 1_000_000) {
                System.out.println("[Updater] Downloaded file too small (" + size + " bytes). Skipping.");
                Files.deleteIfExists(tempFile);
                return false;
            }

            System.out.println("[Updater] Downloaded " + (size / 1024 / 1024) + " MB");

            // Backup current version
            Path backup = currentJar.getParent().resolve(currentJar.getFileName() + ".backup");
            try {
                Files.copy(currentJar, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("[Updater] Could not backup current version: " + e.getMessage());
            }

            // Replace current file with update
            try {
                Files.move(tempFile, currentJar, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Updater] Update applied! Please restart the game.");
                return true;
            } catch (IOException e) {
                // On Windows, the running exe/jar may be locked. Try rename trick.
                System.out.println("[Updater] Could not replace running file. Trying alternative...");
                try {
                    Path updateReady = currentJar.getParent().resolve(".minefuck-update-ready");
                    Files.move(tempFile, updateReady, StandardCopyOption.REPLACE_EXISTING);
                    // Write a small script that will apply the update on next launch
                    writeUpdateScript(updateReady, currentJar);
                    System.out.println("[Updater] Update downloaded! Will be applied on next restart.");
                    return false;
                } catch (IOException e2) {
                    System.out.println("[Updater] Auto-update failed: " + e2.getMessage());
                    Files.deleteIfExists(tempFile);
                    return false;
                }
            }

        } catch (Exception e) {
            System.out.println("[Updater] Update check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a pending update needs to be applied (from a previous failed in-place update).
     */
    public static void applyPendingUpdate() {
        try {
            Path currentJar = getCurrentJarPath();
            if (currentJar == null) return;

            Path updateReady = currentJar.getParent().resolve(".minefuck-update-ready");
            if (Files.exists(updateReady)) {
                System.out.println("[Updater] Applying pending update...");
                Files.move(updateReady, currentJar, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Updater] Update applied! Restarting...");
                // Clean up
                Path backup = currentJar.getParent().resolve(currentJar.getFileName() + ".backup");
                Files.deleteIfExists(backup);
            }
        } catch (Exception e) {
            System.out.println("[Updater] Could not apply pending update: " + e.getMessage());
        }
    }

    private static String findDownloadUrl(String json) {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        // Look for the right asset
        // Parse browser_download_url entries
        Pattern urlPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = urlPattern.matcher(json);

        String jarUrl = null;
        String exeUrl = null;

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url.endsWith(".exe")) {
                exeUrl = url;
            } else if (url.endsWith(".jar")) {
                jarUrl = url;
            }
        }

        // On Windows prefer .exe, otherwise .jar
        if (isWindows && exeUrl != null) return exeUrl;
        if (jarUrl != null) return jarUrl;
        return exeUrl; // fallback
    }

    private static boolean downloadFile(String urlStr, Path dest) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(30000); // 30s read timeout for large files
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();
            if (code != 200) {
                System.out.println("[Updater] HTTP " + code + " from download URL");
                return false;
            }

            long total = conn.getContentLengthLong();
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(dest)) {
                byte[] buf = new byte[8192];
                long downloaded = 0;
                int n;
                int lastPercent = -1;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                    downloaded += n;
                    if (total > 0) {
                        int percent = (int) (downloaded * 100 / total);
                        if (percent != lastPercent && percent % 10 == 0) {
                            System.out.println("[Updater] Downloading... " + percent + "%");
                            lastPercent = percent;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("[Updater] Download error: " + e.getMessage());
            return false;
        }
    }

    private static String httpGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            int code = conn.getResponseCode();
            if (code != 200) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Simple JSON field parser (avoids needing a JSON library dependency).
     */
    private static String parseJsonField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher m = Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\n", "\n").replace("\\r", "");
        }
        return null;
    }

    /**
     * Compare semantic versions. Returns true if 'latest' is newer than 'current'.
     */
    private static boolean isNewerVersion(String latest, String current) {
        try {
            int[] latestParts = parseVersion(latest);
            int[] currentParts = parseVersion(current);

            for (int i = 0; i < 3; i++) {
                if (latestParts[i] > currentParts[i]) return true;
                if (latestParts[i] < currentParts[i]) return false;
            }
            return false; // same version
        } catch (Exception e) {
            return false;
        }
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
        }
        return result;
    }

    private static Path getCurrentJarPath() {
        try {
            // Get the path of the running JAR/EXE
            String path = AutoUpdater.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();

            // On Windows, strip leading slash from /C:/...
            if (System.getProperty("os.name").toLowerCase().contains("win") && path.startsWith("/")) {
                path = path.substring(1);
            }

            Path jarPath = Paths.get(path);
            if (Files.isRegularFile(jarPath) &&
                    (path.endsWith(".jar") || path.endsWith(".exe"))) {
                return jarPath;
            }

            return null; // Running from IDE or classes directory
        } catch (Exception e) {
            return null;
        }
    }

    private static void writeUpdateScript(Path updateFile, Path targetFile) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Path scriptPath;

        if (os.contains("win")) {
            scriptPath = targetFile.getParent().resolve(".minefuck-apply-update.bat");
            String script = "@echo off\r\n" +
                    "timeout /t 2 /nobreak >nul\r\n" +
                    "move /Y \"" + updateFile + "\" \"" + targetFile + "\"\r\n" +
                    "start \"\" \"" + targetFile + "\"\r\n" +
                    "del \"%~f0\"\r\n";
            Files.writeString(scriptPath, script);
        } else {
            scriptPath = targetFile.getParent().resolve(".minefuck-apply-update.sh");
            String script = "#!/bin/bash\n" +
                    "sleep 2\n" +
                    "mv \"" + updateFile + "\" \"" + targetFile + "\"\n" +
                    "chmod +x \"" + targetFile + "\"\n" +
                    "\"" + targetFile + "\" &\n" +
                    "rm -- \"$0\"\n";
            Files.writeString(scriptPath, script);
            scriptPath.toFile().setExecutable(true);
        }
    }
}
