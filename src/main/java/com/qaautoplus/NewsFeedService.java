package com.qaautoplus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Fetches LIVE news from RSS feeds configured in application.properties.
 * Caches results and refreshes every N minutes (configurable).
 *
 * Feeds are mapped to category IDs from site-config.json:
 *   feeds.ai=https://techcrunch.com/category/artificial-intelligence/feed/
 *   feeds.ml=https://news.mit.edu/topic/machine-learning/feed
 *   etc.
 *
 * API endpoints served:
 *   GET /api/news          → all categories with live articles
 *   GET /api/news?cat=ai   → single category
 *   GET /api/trending      → latest articles across all feeds (for trending bar)
 */
public class NewsFeedService {

    private static volatile Map<String, JsonArray> cachedArticles = new HashMap<>();
    private static volatile JsonArray cachedTrending = new JsonArray();
    private static volatile long lastFetchTime = 0;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault());

    /**
     * Returns cached articles for a category, fetching if stale.
     */
    public static JsonArray getArticles(String categoryId) {
        ensureFresh();
        return cachedArticles.getOrDefault(categoryId, new JsonArray());
    }

    /**
     * Returns all categories with their live articles merged.
     */
    public static Map<String, JsonArray> getAllArticles() {
        ensureFresh();
        return Collections.unmodifiableMap(cachedArticles);
    }

    /**
     * Returns trending items (latest across all feeds) with real timestamps.
     */
    public static JsonArray getTrending() {
        ensureFresh();
        return cachedTrending;
    }

    /**
     * Returns the full news payload as a JSON string.
     */
    public static String getNewsJson(String categoryFilter) {
        ensureFresh();
        JsonObject result = new JsonObject();

        if (categoryFilter != null && !categoryFilter.isBlank()) {
            JsonArray arts = cachedArticles.getOrDefault(categoryFilter, new JsonArray());
            result.add(categoryFilter, arts);
        } else {
            for (var entry : cachedArticles.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
        }
        result.add("trending", cachedTrending);
        result.addProperty("lastUpdated", Instant.ofEpochMilli(lastFetchTime).toString());
        return result.toString();
    }

    // ---- Internal ----

    private static void ensureFresh() {
        Properties cfg = AppConfig.get();
        int ttl = 15;
        try { ttl = Integer.parseInt(cfg.getProperty("feeds.cacheTtlMinutes", "15")); } catch (Exception ignored) {}

        long now = System.currentTimeMillis();
        if (now - lastFetchTime > ttl * 60_000L) {
            synchronized (NewsFeedService.class) {
                if (now - lastFetchTime > ttl * 60_000L) {
                    fetchAll(cfg);
                    lastFetchTime = System.currentTimeMillis();
                }
            }
        }
    }

    private static void fetchAll(Properties cfg) {
        int maxPerCat = 3;
        try { maxPerCat = Integer.parseInt(cfg.getProperty("feeds.maxPerCategory", "3")); } catch (Exception ignored) {}

        Map<String, JsonArray> newArticles = new LinkedHashMap<>();
        List<TrendingItem> allItems = new ArrayList<>();

        // Find all feed URLs: feeds.<catId>=<url>
        for (String key : cfg.stringPropertyNames()) {
            if (key.startsWith("feeds.") && !key.equals("feeds.maxPerCategory")
                    && !key.equals("feeds.cacheTtlMinutes") && !key.equals("feeds.trending")) {
                String catId = key.substring(6); // "feeds.ai" → "ai"
                String feedUrl = cfg.getProperty(key);
                try {
                    List<SyndEntry> entries = fetchFeed(feedUrl);
                    JsonArray arr = new JsonArray();
                    int count = 0;
                    for (SyndEntry entry : entries) {
                        if (count >= maxPerCat) break;
                        JsonObject art = entryToJson(entry, catId);
                        arr.add(art);
                        count++;

                        // Collect for trending
                        if (entry.getPublishedDate() != null) {
                            allItems.add(new TrendingItem(
                                entry.getTitle(),
                                entry.getLink(),
                                catId.toUpperCase(),
                                entry.getPublishedDate().toInstant()
                            ));
                        }
                    }
                    if (arr.size() > 0) {
                        newArticles.put(catId, arr);
                    }
                    System.out.println("  [NewsFeed] " + catId + " → " + arr.size() + " articles from " + feedUrl);
                } catch (Exception e) {
                    System.err.println("  [NewsFeed] FAILED " + catId + " (" + feedUrl + "): " + e.getMessage());
                }
            }
        }

        // Also fetch the general trending feed
        String trendingUrl = cfg.getProperty("feeds.trending", "");
        if (!trendingUrl.isBlank()) {
            try {
                List<SyndEntry> entries = fetchFeed(trendingUrl);
                for (SyndEntry entry : entries) {
                    if (entry.getPublishedDate() != null) {
                        String cat = guessCategory(entry.getTitle());
                        allItems.add(new TrendingItem(
                            entry.getTitle(), entry.getLink(), cat, entry.getPublishedDate().toInstant()
                        ));
                    }
                }
            } catch (Exception e) {
                System.err.println("  [NewsFeed] FAILED trending feed: " + e.getMessage());
            }
        }

        // Build trending: sort by date desc, take top 8, compute relative time
        allItems.sort((a, b) -> b.publishedAt.compareTo(a.publishedAt));
        JsonArray trending = new JsonArray();
        Set<String> seen = new HashSet<>();
        for (TrendingItem item : allItems) {
            if (trending.size() >= 8) break;
            if (seen.contains(item.title)) continue;
            seen.add(item.title);
            JsonObject t = new JsonObject();
            t.addProperty("title", item.title);
            t.addProperty("url", item.url);
            t.addProperty("category", item.category);
            t.addProperty("time", relativeTime(item.publishedAt));
            t.addProperty("publishedAt", item.publishedAt.toEpochMilli());
            trending.add(t);
        }

        cachedArticles = newArticles;
        cachedTrending = trending;
        System.out.println("  [NewsFeed] Refresh complete: " + newArticles.size() + " categories, "
                + trending.size() + " trending items");
    }

    private static List<SyndEntry> fetchFeed(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "QAAutoPlus/2.0 RSS Reader")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(
                new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8))));
        return feed.getEntries();
    }

    private static JsonObject entryToJson(SyndEntry entry, String catId) {
        JsonObject art = new JsonObject();
        art.addProperty("title", entry.getTitle() != null ? entry.getTitle().trim() : "");
        art.addProperty("url", entry.getLink() != null ? entry.getLink() : "");

        // Extract excerpt from description
        String excerpt = "";
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            excerpt = entry.getDescription().getValue()
                    .replaceAll("<[^>]+>", "")   // strip HTML
                    .replaceAll("\\s+", " ")
                    .trim();
            if (excerpt.length() > 200) excerpt = excerpt.substring(0, 200) + "...";
        }
        art.addProperty("excerpt", excerpt);

        // Image: try to extract from enclosures or media
        String image = "";
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            String encUrl = entry.getEnclosures().get(0).getUrl();
            if (encUrl != null) image = encUrl;
        }
        if (image.isEmpty()) {
            // Try to find an img in the content
            String content = "";
            if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
                content = entry.getDescription().getValue();
            }
            int imgIdx = content.indexOf("src=\"");
            if (imgIdx > -1) {
                int start = imgIdx + 5;
                int end = content.indexOf("\"", start);
                if (end > start) image = content.substring(start, end);
            }
        }
        if (image.isEmpty()) {
            // Fallback placeholder based on category
            image = "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=600&q=80";
        }
        art.addProperty("image", image);

        // Author
        String author = "QAAutoPlus";
        if (entry.getAuthor() != null && !entry.getAuthor().isBlank()) {
            author = entry.getAuthor();
        }
        art.addProperty("author", author);

        // Date
        if (entry.getPublishedDate() != null) {
            art.addProperty("date", DATE_FMT.format(entry.getPublishedDate().toInstant()));
            art.addProperty("publishedAt", entry.getPublishedDate().toInstant().toEpochMilli());
        } else {
            art.addProperty("date", DATE_FMT.format(Instant.now()));
            art.addProperty("publishedAt", Instant.now().toEpochMilli());
        }

        // Read time estimate
        int words = excerpt.split("\\s+").length;
        int readMin = Math.max(2, words / 40);
        art.addProperty("readTime", readMin + " min read");

        return art;
    }

    /** Compute "2h ago", "1d ago", etc. from an Instant. */
    static String relativeTime(Instant publishedAt) {
        long diffSec = Duration.between(publishedAt, Instant.now()).getSeconds();
        if (diffSec < 0) diffSec = 0;
        if (diffSec < 60)        return diffSec + "s ago";
        if (diffSec < 3600)      return (diffSec / 60) + "m ago";
        if (diffSec < 86400)     return (diffSec / 3600) + "h ago";
        if (diffSec < 604800)    return (diffSec / 86400) + "d ago";
        return (diffSec / 604800) + "w ago";
    }

    /** Simple keyword-based category guesser for trending items. */
    private static String guessCategory(String title) {
        if (title == null) return "Tech";
        String t = title.toLowerCase();
        if (t.contains("ai") || t.contains("artificial intelligence") || t.contains("gpt") || t.contains("openai") || t.contains("deepmind"))
            return "AI";
        if (t.contains("machine learning") || t.contains("neural") || t.contains("model"))
            return "ML";
        if (t.contains("security") || t.contains("hack") || t.contains("cyber") || t.contains("vulnerability"))
            return "Security";
        if (t.contains("startup") || t.contains("funding") || t.contains("raises") || t.contains("valuation"))
            return "Startups";
        if (t.contains("robot") || t.contains("automat"))
            return "Robotics";
        if (t.contains("app") || t.contains("developer") || t.contains("tool") || t.contains("code"))
            return "Apps";
        if (t.contains("vision") || t.contains("image") || t.contains("camera"))
            return "Vision";
        return "Tech";
    }

    private record TrendingItem(String title, String url, String category, Instant publishedAt) {}
}

