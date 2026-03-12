package com.qaautoplus.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qaautoplus.SiteConfig;
import com.qaautoplus.NewsFeedService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Home Servlet — Renders a TechCrunch-style homepage using data from site-config.json
 */
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        JsonObject config = SiteConfig.get();
        JsonObject site = config.getAsJsonObject("site");
        JsonObject hero = config.getAsJsonObject("hero");
        JsonArray nav = config.getAsJsonArray("navigation");
        JsonArray trending = config.getAsJsonArray("trending");
        JsonArray categories = config.getAsJsonArray("categories");
        JsonObject sidebar = config.getAsJsonObject("sidebar");
        JsonObject footer = config.getAsJsonObject("footer");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.printf("<title>%s — %s</title>%n", esc(site.get("name").getAsString()), esc(site.get("tagline").getAsString()));
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@400;500;700&display=swap' rel='stylesheet'>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head>");
        out.println("<body>");

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
                String cls = item.get("active").getAsBoolean() ? " class='active'" : "";
                out.printf("      <a href='%s'%s>%s</a>%n", esc(item.get("url").getAsString()), cls, esc(item.get("label").getAsString()));
            }
        }
        out.println("    </nav>");
        out.println("    <div class='topbar-right'>");
        out.println("      <button class='search-btn' id='searchToggle' title='Search'>🔍</button>");
        out.println("      <button class='theme-toggle' id='themeToggle' title='Toggle dark mode'>🌙</button>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("</div>");

        // ========== SEARCH OVERLAY ==========
        out.println("<div class='search-overlay' id='searchOverlay'>");
        out.println("  <div class='search-box'>");
        out.println("    <input type='text' id='searchInput' placeholder='Search articles, categories, topics...' autocomplete='off'>");
        out.println("    <button class='search-close' id='searchClose'>✕</button>");
        out.println("  </div>");
        out.println("  <div class='search-results' id='searchResults'></div>");
        out.println("</div>");

        // ========== HERO SECTION ==========
        out.println("<section class='hero'>");
        out.printf("<a href='%s' target='_blank' class='hero-link'>%n", esc(hero.get("url").getAsString()));
        out.printf("  <div class='hero-bg' style=\"background-image:url('%s')\"></div>%n", esc(hero.get("image").getAsString()));
        out.println("  <div class='hero-overlay'></div>");
        out.println("  <div class='hero-content'>");
        out.printf("    <span class='hero-cat'>%s</span>%n", esc(hero.get("category").getAsString()));
        out.printf("    <h1>%s</h1>%n", esc(hero.get("title").getAsString()));
        out.printf("    <p class='hero-sub'>%s</p>%n", esc(hero.get("subtitle").getAsString()));
        out.println("    <div class='hero-meta'>");
        out.printf("      <span>By <strong>%s</strong></span>%n", esc(hero.get("author").getAsString()));
        out.printf("      <span>%s</span>%n", esc(hero.get("date").getAsString()));
        out.println("    </div>");
        out.println("  </div>");
        out.println("</a>");
        out.println("</section>");

        // ========== TRENDING BAR (LIVE from RSS) ==========
        JsonArray liveTrending = NewsFeedService.getTrending();
        // Fall back to static JSON if live is empty
        JsonArray trendingData = (liveTrending != null && liveTrending.size() > 0) ? liveTrending : trending;

        out.println("<section class='trending-bar'>");
        out.println("  <div class='trending-inner'>");
        out.println("    <span class='trending-label'>🔥 Trending</span>");
        out.println("    <div class='trending-scroll' id='trendingScroll'>");
        if (trendingData != null) {
            for (JsonElement t : trendingData) {
                JsonObject item = t.getAsJsonObject();
                long pubAt = item.has("publishedAt") ? item.get("publishedAt").getAsLong() : 0;
                out.printf("      <a href='%s' target='_blank' class='trending-item'>", esc(item.get("url").getAsString()));
                out.printf("<span class='trending-cat'>%s</span> %s", esc(item.get("category").getAsString()), esc(item.get("title").getAsString()));
                out.printf("<span class='trending-time' data-published='%d'>%s</span></a>%n",
                        pubAt, esc(item.get("time").getAsString()));
            }
        }
        out.println("    </div>");
        out.println("  </div>");
        out.println("</section>");

        // ========== MAIN CONTENT ==========
        out.println("<main class='main-content'>");
        out.println("<div class='content-grid'>");

        // --- Articles feed (live RSS merged with fallback static) ---
        out.println("<div class='feed'>");

        java.util.Map<String, JsonArray> liveArticles = NewsFeedService.getAllArticles();

        if (categories != null) {
            for (JsonElement catEl : categories) {
                JsonObject cat = catEl.getAsJsonObject();
                String catId = cat.get("id").getAsString();
                String catName = cat.get("name").getAsString();
                String catColor = cat.get("color").getAsString();
                String catUrl = cat.get("url").getAsString();

                // Prefer live articles, fall back to static
                JsonArray articles = liveArticles.containsKey(catId)
                        ? liveArticles.get(catId) : cat.getAsJsonArray("articles");

                out.printf("<section class='category-section' id='%s'>%n", esc(catId));
                out.println("  <div class='section-header'>");
                out.printf("    <h2><span class='section-dot' style='background:%s'></span>%s</h2>%n", esc(catColor), esc(catName));
                out.printf("    <a href='%s' target='_blank' class='see-all'>See all →</a>%n", esc(catUrl));
                out.println("  </div>");

                if (articles != null && articles.size() > 0) {
                    // First article is featured (large)
                    JsonObject featured = articles.get(0).getAsJsonObject();
                    out.printf("<a href='%s' target='_blank' class='article-featured'>%n", esc(featured.get("url").getAsString()));
                    out.printf("  <div class='article-img'><img src='%s' alt='' loading='lazy'></div>%n", esc(featured.get("image").getAsString()));
                    out.println("  <div class='article-body'>");
                    out.printf("    <span class='article-cat' style='color:%s'>%s</span>%n", esc(catColor), esc(cat.get("shortName").getAsString()));
                    out.printf("    <h3>%s</h3>%n", esc(featured.get("title").getAsString()));
                    out.printf("    <p>%s</p>%n", esc(featured.get("excerpt").getAsString()));
                    out.println("    <div class='article-meta'>");
                    out.printf("      <span>%s</span><span>%s</span><span>%s</span>%n",
                            esc(featured.get("author").getAsString()),
                            esc(featured.get("date").getAsString()),
                            esc(featured.get("readTime").getAsString()));
                    out.println("    </div>");
                    out.println("  </div>");
                    out.println("</a>");

                    // Remaining articles in a grid
                    if (articles.size() > 1) {
                        out.println("<div class='article-grid'>");
                        for (int i = 1; i < articles.size(); i++) {
                            JsonObject art = articles.get(i).getAsJsonObject();
                            out.printf("<a href='%s' target='_blank' class='article-card'>%n", esc(art.get("url").getAsString()));
                            out.printf("  <div class='article-card-img'><img src='%s' alt='' loading='lazy'></div>%n", esc(art.get("image").getAsString()));
                            out.printf("  <span class='article-cat' style='color:%s'>%s</span>%n", esc(catColor), esc(cat.get("shortName").getAsString()));
                            out.printf("  <h4>%s</h4>%n", esc(art.get("title").getAsString()));
                            out.printf("  <p>%s</p>%n", esc(art.get("excerpt").getAsString()));
                            out.println("  <div class='article-meta'>");
                            out.printf("    <span>%s</span><span>%s</span>%n",
                                    esc(art.get("date").getAsString()),
                                    esc(art.get("readTime").getAsString()));
                            out.println("  </div>");
                            out.println("</a>");
                        }
                        out.println("</div>");
                    }
                }
                out.println("</section>");
            }
        }
        out.println("</div>"); // end feed

        // ========== SIDEBAR ==========
        out.println("<aside class='sidebar'>");

        // Newsletter signup
        if (sidebar != null) {
            JsonObject newsletter = sidebar.getAsJsonObject("newsletters");
            out.println("<div class='sidebar-card newsletter-card'>");
            out.println("  <div class='newsletter-icon'>📬</div>");
            out.printf("  <h3>%s</h3>%n", esc(newsletter.get("title").getAsString()));
            out.printf("  <p>%s</p>%n", esc(newsletter.get("description").getAsString()));
            out.println("  <form class='newsletter-form' id='subscribeForm'>");
            out.println("    <input type='text' id='subName' placeholder='Your name' required>");
            out.println("    <input type='email' id='subEmail' placeholder='your@email.com' required>");
            out.println("    <button type='submit' id='subBtn'>Subscribe</button>");
            out.println("    <div id='subMsg' class='sub-msg'></div>");
            out.println("  </form>");
            out.println("</div>");

            // Popular Topics
            JsonArray topics = sidebar.getAsJsonArray("popularTopics");
            if (topics != null) {
                out.println("<div class='sidebar-card'>");
                out.println("  <h3>🔧 Popular Tools</h3>");
                out.println("  <div class='topic-tags'>");
                for (JsonElement topicEl : topics) {
                    JsonObject topic = topicEl.getAsJsonObject();
                    out.printf("    <a href='%s' target='_blank' class='topic-tag'>%s</a>%n",
                            esc(topic.get("url").getAsString()),
                            esc(topic.get("label").getAsString()));
                }
                out.println("  </div>");
                out.println("</div>");
            }
        }

        // Category quick links
        if (categories != null) {
            out.println("<div class='sidebar-card'>");
            out.println("  <h3>📂 Categories</h3>");
            out.println("  <div class='category-links'>");
            for (JsonElement catEl : categories) {
                JsonObject cat = catEl.getAsJsonObject();
                out.printf("    <a href='%s' target='_blank' class='cat-link'>", esc(cat.get("url").getAsString()));
                out.printf("<span class='cat-icon'>%s</span>", cat.get("icon").getAsString());
                out.printf("<span>%s</span>", esc(cat.get("shortName").getAsString()));
                out.printf("<span class='cat-count'>%d</span></a>%n", cat.getAsJsonArray("articles").size());
            }
            out.println("  </div>");
            out.println("</div>");
        }

        out.println("</aside>");

        out.println("</div>"); // end content-grid
        out.println("</main>");

        // ========== FOOTER ==========
        out.println("<footer class='site-footer'>");
        out.println("<div class='footer-inner'>");
        out.println("  <div class='footer-top'>");
        out.println("    <div class='footer-brand'>");
        out.printf("      <a href='/home' class='logo'><span class='logo-qa'>QA</span><span class='logo-auto'>Auto</span><span class='logo-plus'>+</span></a>%n");
        out.printf("      <p>%s</p>%n", esc(site.get("description").getAsString()));
        out.println("    </div>");

        if (footer != null) {
            JsonArray sections = footer.getAsJsonArray("sections");
            if (sections != null) {
                for (JsonElement secEl : sections) {
                    JsonObject sec = secEl.getAsJsonObject();
                    out.println("    <div class='footer-col'>");
                    out.printf("      <h4>%s</h4>%n", esc(sec.get("title").getAsString()));
                    out.println("      <ul>");
                    for (JsonElement linkEl : sec.getAsJsonArray("links")) {
                        JsonObject link = linkEl.getAsJsonObject();
                        out.printf("        <li><a href='%s' target='_blank'>%s</a></li>%n",
                                esc(link.get("url").getAsString()),
                                esc(link.get("label").getAsString()));
                    }
                    out.println("      </ul>");
                    out.println("    </div>");
                }
            }
        }
        out.println("  </div>"); // end footer-top

        out.println("  <div class='footer-bottom'>");
        out.printf("    <span>%s</span>%n", site.get("copyright").getAsString());
        out.println("    <div class='footer-social'>");
        if (footer != null) {
            JsonArray social = footer.getAsJsonArray("social");
            if (social != null) {
                for (JsonElement sEl : social) {
                    JsonObject s = sEl.getAsJsonObject();
                    out.printf("      <a href='%s' title='%s' class='social-link'>%s</a>%n",
                            esc(s.get("url").getAsString()),
                            esc(s.get("label").getAsString()),
                            s.get("icon").getAsString());
                }
            }
        }
        out.println("    </div>");
        out.println("  </div>");

        out.println("</div>"); // end footer-inner
        out.println("</footer>");

        // ========== BACK TO TOP ==========
        out.println("<button class='back-to-top' id='backToTop' title='Back to top'>↑</button>");

        out.println("<script src='/js/app.js'></script>");
        out.println("</body>");
        out.println("</html>");
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

