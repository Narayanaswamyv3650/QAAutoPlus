package com.qaautoplus.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * Blog Servlet — dynamically renders AI News cards from ai-news-links.properties.
 *
 * Just add a new block to the properties file:
 *   cat.newkey.url=https://...
 *   cat.newkey.icon=🔬
 *   cat.newkey.tag=Science
 *   cat.newkey.title=AI in Science
 *   cat.newkey.desc=Latest scientific AI breakthroughs.
 *   cat.newkey.color=ai          (ai|ml|rpa|llm|vis)
 *
 * ...and it automatically appears as a new card. No code changes needed.
 */
public class BlogServlet extends HttpServlet {

    // Tag color CSS classes
    private static final Map<String, String> TAG_COLORS = Map.of(
            "ai", "t-ai", "ml", "t-ml", "rpa", "t-rpa", "llm", "t-llm", "vis", "t-vis"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Prevent browser caching so changes show immediately
        resp.setContentType("text/html; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");

        Properties props = loadProperties();
        String mainUrl   = props.getProperty("main.url", "https://www.artificialintelligence-news.com/");
        String mainTitle = props.getProperty("main.title", "Artificial Intelligence News");
        String mainDesc  = props.getProperty("main.description", "Latest AI & automation news");

        // Discover all category keys (cat.XXX.url)
        List<String> catKeys = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("cat.") && key.endsWith(".url")) {
                String catId = key.substring(4, key.length() - 4); // e.g. "ai", "ml"
                catKeys.add(catId);
            }
        }
        // Sort by the order they appear (alphabetical by key, or use an order property)
        catKeys.sort((a, b) -> {
            int oa = parseInt(props.getProperty("cat." + a + ".order", "99"));
            int ob = parseInt(props.getProperty("cat." + b + ".order", "99"));
            if (oa != ob) return Integer.compare(oa, ob);
            return a.compareTo(b);
        });

        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width,initial-scale=1.0'>");
        out.println("<title>AutomationPulse — AI News for QA Engineers</title>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=Syne:wght@400;600;700;800&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println(CSS);
        out.println("</style></head><body>");

        // ---- Nav ----
        out.println("<nav>");
        out.println("  <div class='nav-brand'>");
        out.println("    <a href='#' class='logo'>Automation<span>Pulse</span></a>");
        out.println("    <div class='nav-ctx'>");
        out.println("      <b><em>AI News</em> &middot; QA Engineers &middot; Automation Pulse</b>");
        out.println("      <small>Latest AI &amp; automation news curated for QA engineers</small>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("  <div class='nav-right'>");
        out.println("    <button class='theme-btn' id='themeToggle' title='Toggle Dark Mode'>&#9684;</button>");
        out.println("  </div>");
        out.println("</nav>");

        // ---- Hero banner ----
        out.println("<div class='page'>");
        out.printf("<a href='%s' target='_blank' class='hero'>%n", esc(mainUrl));
        out.println("  <div class='hero-badge'>LIVE</div>");
        out.println("  <div class='hero-body'>");
        out.printf("    <h1>%s</h1>%n", esc(mainTitle));
        out.printf("    <p>%s &mdash; click to open &rarr;</p>%n", esc(mainDesc));
        out.println("  </div>");
        out.println("  <div class='hero-arrow'>&nearr;</div>");
        out.println("</a>");

        // ---- Cards grid ----
        out.println("<section class='cards-wrap'><div class='grid'>");
        for (String catId : catKeys) {
            String pre   = "cat." + catId + ".";
            String url   = props.getProperty(pre + "url", "#");
            String icon  = props.getProperty(pre + "icon", "📰");
            String tag   = props.getProperty(pre + "tag", catId.toUpperCase());
            String title = props.getProperty(pre + "title", catId);
            String desc  = props.getProperty(pre + "desc", "");
            String color = props.getProperty(pre + "color", "ai");
            String tagCls = TAG_COLORS.getOrDefault(color, "t-ai");

            out.printf("<a href='%s' target='_blank' class='card'>%n", esc(url));
            out.printf("  <div class='card-ico'>%s</div>%n", icon);
            out.printf("  <span class='tag %s'>%s</span>%n", tagCls, esc(tag));
            out.printf("  <h3>%s</h3>%n", esc(title));
            out.printf("  <p>%s</p>%n", esc(desc));
            out.println("  <span class='card-go'>Read articles &rarr;</span>");
            out.println("</a>");
        }
        out.println("</div></section></div>");

        // ---- Footer ----
        out.println("<footer>");
        out.println("  <a href='#' class='logo'>Automation<span>Pulse</span></a>");
        out.printf("  <small>&copy; 2025 QA Auto Plus &mdash; AI news sourced from <a href='%s' target='_blank'>multiple sources</a></small>%n", esc(mainUrl));
        out.println("</footer>");

        // ---- JS ----
        out.println("<script>");
        out.println(JS);
        out.println("</script>");
        out.println("</body></html>");
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ai-news-links.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load ai-news-links.properties: " + e.getMessage());
        }
        return props;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 99; }
    }

    // ==================== INLINE CSS ====================
    private static final String CSS = """
:root{--bg:#fff;--surface:#f7f7f8;--border:#e0e0e0;--accent:#00b876;--accent2:#7b61ff;--text:#111;--muted:#666;--danger:#ff4466}
body.bw{--bg:#0a0c10;--surface:#13161e;--border:#1e2230;--accent:#00ffa3;--accent2:#7b61ff;--text:#e8eaf0;--muted:#6b7080;--danger:#ff4466}
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
html{scroll-behavior:smooth}
body{background:var(--bg);color:var(--text);font-family:'Syne',sans-serif;min-height:100vh;overflow-x:hidden;transition:background .3s,color .3s}
nav{position:fixed;top:0;left:0;right:0;z-index:100;display:flex;align-items:center;justify-content:space-between;padding:0 1.5rem;height:54px;background:var(--bg);border-bottom:1px solid var(--border);transition:background .3s}
body.bw nav{background:rgba(10,12,16,.95)}
.nav-brand{display:flex;align-items:center;gap:1rem}
.logo{font-family:'Space Mono',monospace;font-size:1rem;font-weight:700;color:var(--accent);text-decoration:none;white-space:nowrap}
.logo span{color:var(--text)}
.nav-ctx{display:flex;flex-direction:column;gap:1px;border-left:2px solid var(--accent);padding-left:.8rem}
.nav-ctx b{font-family:'Syne',sans-serif;font-size:.75rem;font-weight:700;color:var(--text)}
.nav-ctx b em{font-style:normal;color:var(--accent)}
.nav-ctx small{font-family:'Space Mono',monospace;font-size:.55rem;color:var(--muted)}
.nav-right{display:flex;align-items:center;gap:.6rem}
.theme-btn{background:0 0;border:1px solid var(--border);color:var(--text);font-size:1.1rem;width:34px;height:34px;border-radius:6px;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:border-color .2s,color .2s}
.theme-btn:hover{border-color:var(--accent);color:var(--accent)}
.page{padding-top:54px}
.hero{display:flex;align-items:center;justify-content:center;min-height:280px;padding:3rem 2rem;background:linear-gradient(135deg,#0a0c10,#111827 50%,#1a1a2e);color:#fff;text-decoration:none;position:relative;overflow:hidden;cursor:pointer;transition:filter .3s}
.hero:hover{filter:brightness(1.15)}
.hero::before{content:'';position:absolute;inset:0;background-image:linear-gradient(rgba(0,255,163,.04) 1px,transparent 1px),linear-gradient(90deg,rgba(0,255,163,.04) 1px,transparent 1px);background-size:40px 40px;pointer-events:none}
.hero::after{content:'';position:absolute;top:-50%;right:-20%;width:500px;height:500px;background:radial-gradient(circle,rgba(0,184,118,.15),transparent 70%);pointer-events:none}
.hero-badge{position:absolute;top:1rem;left:1.5rem;font-family:'Space Mono',monospace;font-size:.6rem;font-weight:700;letter-spacing:.15em;padding:.25rem .6rem;background:#ff4466;color:#fff;border-radius:3px;animation:pulse 2s ease-in-out infinite}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.5}}
.hero-body{position:relative;z-index:1;text-align:center;max-width:700px}
.hero-body h1{font-size:clamp(1.6rem,4vw,2.8rem);font-weight:800;line-height:1.1;letter-spacing:-.03em;margin-bottom:.6rem;background:linear-gradient(135deg,#fff,#00ffa3);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text}
.hero-body p{color:rgba(255,255,255,.6);font-family:'Space Mono',monospace;font-size:.72rem;line-height:1.5}
.hero-arrow{position:absolute;right:2rem;top:50%;transform:translateY(-50%);font-size:2.5rem;color:#00ffa3;opacity:.35;transition:opacity .3s,transform .3s;z-index:1}
.hero:hover .hero-arrow{opacity:1;transform:translateY(-50%) translateX(5px)}
.cards-wrap{padding:2.5rem 2rem 3rem;max-width:1200px;margin:0 auto}
.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:1rem}
.card{display:flex;flex-direction:column;gap:.5rem;padding:1.6rem;background:var(--surface);border:1px solid var(--border);border-radius:10px;text-decoration:none;color:inherit;position:relative;overflow:hidden;transition:transform .2s,box-shadow .2s,border-color .2s}
.card:hover{transform:translateY(-4px);box-shadow:0 8px 30px rgba(0,0,0,.08);border-color:var(--accent)}
body.bw .card:hover{box-shadow:0 8px 30px rgba(0,255,163,.06)}
.card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:var(--accent);transform:scaleX(0);transform-origin:left;transition:transform .3s}
.card:hover::before{transform:scaleX(1)}
.card-ico{font-size:2rem;line-height:1}
.tag{display:inline-block;align-self:flex-start;font-family:'Space Mono',monospace;font-size:.55rem;font-weight:700;text-transform:uppercase;letter-spacing:.1em;padding:.2rem .5rem;border:1px solid;border-radius:3px}
.t-ai{color:var(--accent);border-color:rgba(0,184,118,.3);background:rgba(0,184,118,.06)}
.t-ml{color:var(--accent2);border-color:rgba(123,97,255,.3);background:rgba(123,97,255,.06)}
.t-rpa{color:#ff9f43;border-color:rgba(255,159,67,.3);background:rgba(255,159,67,.06)}
.t-llm{color:#00d2ff;border-color:rgba(0,210,255,.3);background:rgba(0,210,255,.06)}
.t-vis{color:var(--danger);border-color:rgba(255,68,102,.3);background:rgba(255,68,102,.06)}
.card h3{font-size:1.05rem;font-weight:800;line-height:1.2}
.card p{color:var(--muted);font-size:.8rem;line-height:1.5;flex:1}
.card-go{font-family:'Space Mono',monospace;font-size:.68rem;font-weight:700;color:var(--accent);transition:letter-spacing .2s}
.card:hover .card-go{letter-spacing:.06em}
footer{border-top:1px solid var(--border);padding:1.2rem 2rem;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:.8rem}
footer small{font-family:'Space Mono',monospace;font-size:.6rem;color:var(--muted)}
footer a{color:var(--accent);text-decoration:none}
footer a:hover{text-decoration:underline}
@media(max-width:900px){.grid{grid-template-columns:repeat(2,1fr)}}
@media(max-width:600px){.nav-ctx{display:none}.hero{min-height:200px;padding:2rem 1.5rem}.hero-body h1{font-size:1.4rem}.hero-arrow{display:none}.cards-wrap{padding:1.5rem 1rem}.grid{grid-template-columns:1fr}footer{flex-direction:column;text-align:center}}
""";

    // ==================== INLINE JS ====================
    private static final String JS = """
(function(){
  var btn=document.getElementById('themeToggle');
  var s=localStorage.getItem('theme');
  if(s&&s!=='dark'&&s!=='light')localStorage.removeItem('theme');
  if(localStorage.getItem('theme')==='dark')document.body.classList.add('bw');
  btn.addEventListener('click',function(){
    document.body.classList.toggle('bw');
    localStorage.setItem('theme',document.body.classList.contains('bw')?'dark':'light');
  });
})();
""";
}

