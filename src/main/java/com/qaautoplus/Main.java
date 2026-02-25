package com.qaautoplus;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.qaautoplus.servlets.HomeServlet;
import com.qaautoplus.servlets.ApiServlet;
import com.qaautoplus.db.DatabaseConfig;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main class to start the QA Auto Plus web application.
 *
 * Cloud-ready: reads the PORT from environment variable (set by Azure, AWS, Heroku, etc.)
 * Falls back to 8088 for local development.
 *
 * Run locally : java -jar qaautoplus.jar
 * Run in cloud: the platform sets PORT automatically
 */
public class Main {

    private static final int DEFAULT_PORT = 8088;

    public static void main(String[] args) throws Exception {

        // --- Dynamic port: cloud platforms inject PORT env variable ---
        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT env value '" + envPort + "', using default " + DEFAULT_PORT);
            }
        }
        // Allow override via command-line arg as well:  java -jar app.jar 9090
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) { }
        }

        Server server = new Server(port);

        // Initialize Neon PostgreSQL database
        DatabaseConfig.initialize();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // --- Resolve static resources (works from fat-jar AND local dev) ---
        String resourceBase = resolveResourceBase();
        context.setResourceBase(resourceBase);

        // Register servlets
        context.addServlet(new ServletHolder(new HomeServlet()), "/home");
        context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");

        // Default servlet serves static files (css, js, html, blog page, etc.)
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        defaultHolder.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultHolder, "/");

        try {
            server.start();
            System.out.println("============================================");
            System.out.println("  QA Auto Plus server started successfully!");
            System.out.println("============================================");
            System.out.println("  Port       : " + port + (envPort != null ? "  (from PORT env)" : "  (default)"));
            System.out.println("  Home page  : http://localhost:" + port + "/home");
            System.out.println("  Blog page  : http://localhost:" + port + "/blog/ai-automation-blog.html");
            System.out.println("  API status : http://localhost:" + port + "/api/status");
            System.out.println("  API info   : http://localhost:" + port + "/api/info");
            System.out.println("============================================");
            System.out.println("  Press Ctrl+C to stop the server.");
            System.out.println("============================================");
            server.join();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Finds the webapp resource base in order of preference:
     *  1. "webapp/" on the classpath  (inside the fat-jar)
     *  2. src/main/webapp             (local development)
     */
    private static String resolveResourceBase() {
        // 1. Classpath (fat-jar / cloud)
        URL classpathUrl = Main.class.getClassLoader().getResource("webapp/");
        if (classpathUrl != null) {
            System.out.println("  Serving static files from classpath: " + classpathUrl);
            return classpathUrl.toExternalForm();
        }

        // 2. Local dev – relative to working directory
        Path localWebapp = Path.of("src/main/webapp");
        if (Files.isDirectory(localWebapp)) {
            String abs = localWebapp.toAbsolutePath().toString();
            System.out.println("  Serving static files from local path: " + abs);
            return abs;
        }

        System.err.println("  WARNING: No webapp resource directory found!");
        return ".";
    }
}

