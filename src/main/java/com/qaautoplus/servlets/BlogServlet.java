package com.qaautoplus.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qaautoplus.SiteConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Blog Servlet — Renders a category listing / "Explore" page using site-config.json
 */
public class BlogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        JsonObject config = SiteConfig.get();
        JsonObject site = config.getAsJsonObject("site");
        JsonArray nav = config.getAsJsonArray("navigation");
        JsonArray categories = config.getAsJsonArray("categories");
        JsonObject footer = config.getAsJsonObject("footer");

        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.printf("<title>Explore — %s</title>%n", esc(site.get("name").getAsString()));
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@400;500;700&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");

        // ========== TOP BAR ==========
        out.println("<div class='topbar'>");
        out.println("  <div class='topbar-inner'>");
        out.println("    <div class='topbar-left'>");
        out.printf("      <a href='/home' class='logo'><span class='logo-qa'>QA</span><span class='logo-auto'>Auto</span><span class='logo-plus'>+</span></a>%n");
        out.println("    </div>");
        out.println("    <nav class='topbar-nav'>");
        if (nav != null) {
            for (JsonElement n : nav) {
                JsonObject item = n.getAsJsonObject();
                String label = item.get("label").getAsString();
                String cls = "More".equals(label) ? " class='active'" : "";
                out.printf("      <a href='%s'%s>%s</a>%n", esc(item.get("url").getAsString()), cls, esc(label));
            }
        }
        out.println("    </nav>");
        out.println("    <div class='topbar-right'>");
        out.println("      <button class='theme-toggle' id='themeToggle' title='Toggle dark mode'>🌙</button>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("</div>");

        // ========== PAGE HEADER ==========
        out.println("<div class='explore-header'>");
        out.println("  <h1>Explore All Categories</h1>");
        out.println("  <p>Dive into all the tech topics curated for QA & automation engineers</p>");
        out.println("</div>");

        // ========== CATEGORY CARDS ==========
        out.println("<main class='explore-content'>");
        out.println("<div class='explore-grid'>");
        if (categories != null) {
            for (JsonElement catEl : categories) {
                JsonObject cat = catEl.getAsJsonObject();
                String catColor = cat.get("color").getAsString();
                String catUrl = cat.get("url").getAsString();
                JsonArray articles = cat.getAsJsonArray("articles");

                out.printf("<a href='%s' target='_blank' class='explore-card'>%n", esc(catUrl));
                out.printf("  <div class='explore-card-top' style='background:%s20;border-left:4px solid %s'>%n", esc(catColor), esc(catColor));
                out.printf("    <span class='explore-icon'>%s</span>%n", cat.get("icon").getAsString());
                out.printf("    <h2>%s</h2>%n", esc(cat.get("name").getAsString()));
                out.printf("    <p>%s</p>%n", esc(cat.get("description").getAsString()));
                out.printf("    <span class='explore-count'>%d articles</span>%n", articles != null ? articles.size() : 0);
                out.println("  </div>");

                // Show article previews
                if (articles != null) {
                    out.println("  <div class='explore-articles'>");
                    for (int i = 0; i < Math.min(articles.size(), 3); i++) {
                        JsonObject art = articles.get(i).getAsJsonObject();
                        out.printf("    <div class='explore-article-item'>%n");
                        out.printf("      <span class='explore-num'>%d</span>%n", i + 1);
                        out.printf("      <span>%s</span>%n", esc(art.get("title").getAsString()));
                        out.println("    </div>");
                    }
                    out.println("  </div>");
                }
                out.println("</a>");
            }
        }
        out.println("</div>");
        out.println("</main>");

        // ========== FOOTER ==========
        out.println("<footer class='site-footer'>");
        out.println("<div class='footer-inner'>");
        out.println("  <div class='footer-bottom'>");
        out.printf("    <span>%s</span>%n", site.get("copyright").getAsString());
        out.println("  </div>");
        out.println("</div>");
        out.println("</footer>");

        out.println("<script src='/js/app.js'></script>");
        out.println("</body></html>");
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
