package com.qaautoplus.servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qaautoplus.EmailService;
import com.qaautoplus.NewsFeedService;
import com.qaautoplus.SiteConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * API Servlet — REST endpoints:
 *   GET  /api/config    → site-config.json
 *   GET  /api/status    → health check
 *   GET  /api/news      → live articles from RSS feeds (optional ?cat=ai)
 *   GET  /api/trending  → latest trending items with real timestamps
 *   POST /api/subscribe → sends subscription email notification
 */
public class ApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String path = req.getPathInfo();

        if ("/config".equals(path)) {
            resp.getWriter().write(SiteConfig.getJsonString());
        } else if ("/status".equals(path)) {
            resp.getWriter().write("{\"status\":\"ok\",\"app\":\"QAAutoPlus\",\"version\":\"2.0.0\"}");
        } else if ("/news".equals(path)) {
            String cat = req.getParameter("cat");
            resp.getWriter().write(NewsFeedService.getNewsJson(cat));
        } else if ("/trending".equals(path)) {
            resp.getWriter().write(NewsFeedService.getTrending().toString());
        } else {
            resp.setStatus(404);
            resp.getWriter().write("{\"error\":\"Not found\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String path = req.getPathInfo();
        if ("/subscribe".equals(path)) {
            handleSubscribe(req, resp);
        } else {
            resp.setStatus(404);
            resp.getWriter().write("{\"error\":\"Not found\"}");
        }
    }

    private void handleSubscribe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        String name = "", email = "";
        try {
            JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();
            name  = body.has("name")  ? body.get("name").getAsString().trim()  : "";
            email = body.has("email") ? body.get("email").getAsString().trim() : "";
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON body\"}");
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"Please enter a valid email address\"}");
            return;
        }
        if (name.isEmpty()) {
            name = email.substring(0, email.indexOf('@'));
        }

        try {
            EmailService.sendSubscriptionNotification(name, email);
            resp.getWriter().write("{\"success\":true,\"message\":\"You're subscribed! A confirmation has been sent.\"}");
        } catch (Exception e) {
            System.err.println("  [Subscribe] Email failed: " + e.getMessage());
            resp.setStatus(500);
            String err = e.getMessage();
            if (err != null && err.contains("not configured")) {
                resp.getWriter().write("{\"success\":false,\"message\":\"Email not configured yet. Update application.properties.\"}");
            } else {
                resp.getWriter().write("{\"success\":false,\"message\":\"Failed to send email. Try again later.\"}");
            }
        }
    }
}
