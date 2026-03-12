package com.qaautoplus;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads application.properties once and caches it.
 * Used by EmailService and NewsFeedService.
 */
public class AppConfig {

    private static volatile Properties props;

    public static Properties get() {
        if (props == null) {
            synchronized (AppConfig.class) {
                if (props == null) props = load();
            }
        }
        return props;
    }

    private static Properties load() {
        Properties p = new Properties();
        Path local = Path.of("src/main/resources/application.properties");
        if (Files.isRegularFile(local)) {
            try (var r = Files.newBufferedReader(local)) {
                p.load(r);
                System.out.println("  [AppConfig] Loaded from " + local.toAbsolutePath());
                return p;
            } catch (Exception e) { /* fall through */ }
        }
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) { p.load(is); System.out.println("  [AppConfig] Loaded from classpath"); }
        } catch (Exception ignored) {}
        return p;
    }
}

