package com.qaautoplus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class to load and cache site configuration from site-config.json.
 * The JSON file is the single source of truth for all links, articles, and content.
 */
public class SiteConfig {

    private static volatile JsonObject cached;
    private static volatile long lastLoadTime = 0;
    private static final long CACHE_TTL_MS = 5000; // reload every 5s in dev

    /**
     * Returns the parsed JSON config, reloading from disk/classpath if stale.
     */
    public static JsonObject get() {
        long now = System.currentTimeMillis();
        if (cached == null || (now - lastLoadTime) > CACHE_TTL_MS) {
            synchronized (SiteConfig.class) {
                if (cached == null || (now - lastLoadTime) > CACHE_TTL_MS) {
                    cached = load();
                    lastLoadTime = System.currentTimeMillis();
                }
            }
        }
        return cached;
    }

    /**
     * Returns the raw JSON string of the config.
     */
    public static String getJsonString() {
        return new Gson().toJson(get());
    }

    private static JsonObject load() {
        Gson gson = new Gson();

        // 1. Try local dev path first
        Path localPath = Path.of("src/main/resources/site-config.json");
        if (Files.isRegularFile(localPath)) {
            try (var reader = Files.newBufferedReader(localPath, StandardCharsets.UTF_8)) {
                System.out.println("  [SiteConfig] Loaded from local file: " + localPath.toAbsolutePath());
                return gson.fromJson(reader, JsonObject.class);
            } catch (Exception e) {
                System.err.println("  [SiteConfig] Error reading local file: " + e.getMessage());
            }
        }

        // 2. Classpath (fat-jar)
        try (InputStream is = SiteConfig.class.getClassLoader().getResourceAsStream("site-config.json")) {
            if (is != null) {
                try (var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    System.out.println("  [SiteConfig] Loaded from classpath");
                    return gson.fromJson(reader, JsonObject.class);
                }
            }
        } catch (Exception e) {
            System.err.println("  [SiteConfig] Error reading classpath: " + e.getMessage());
        }

        System.err.println("  [SiteConfig] WARNING: site-config.json not found!");
        return new JsonObject();
    }
}

