package com.qaautoplus.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for articles.
 */
public class ArticleDAO {

    /**
     * Insert a new article and return its generated id.
     */
    public static Map<String, Object> create(String title, String excerpt, String content,
                                              String author, String categorySlug, boolean featured) {
        if (!DatabaseConfig.isAvailable()) return null;
        String readTime = Math.max(2, (int) Math.ceil(content.split("\\s+").length / 200.0)) + " min read";
        String sql = """
            INSERT INTO articles (title, excerpt, content, author, category_slug, featured, read_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id, title, excerpt, content, author, category_slug, featured, read_time, created_at
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, excerpt);
            ps.setString(3, content);
            ps.setString(4, author);
            ps.setString(5, categorySlug);
            ps.setBoolean(6, featured);
            ps.setString(7, readTime);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rowToMap(rs);
        } catch (SQLException e) {
            System.err.println("Error creating article: " + e.getMessage());
        }
        return null;
    }

    /**
     * List all articles, optionally filtered by category.
     */
    public static List<Map<String, Object>> list(String categorySlug) {
        List<Map<String, Object>> articles = new ArrayList<>();
        if (!DatabaseConfig.isAvailable()) return articles;
        String sql;
        if (categorySlug != null && !categorySlug.isBlank() && !categorySlug.equals("all")) {
            sql = "SELECT a.*, c.label as category_label FROM articles a " +
                  "JOIN categories c ON a.category_slug = c.slug " +
                  "WHERE a.category_slug = ? ORDER BY a.created_at DESC";
        } else {
            sql = "SELECT a.*, c.label as category_label FROM articles a " +
                  "JOIN categories c ON a.category_slug = c.slug " +
                  "ORDER BY a.created_at DESC";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (categorySlug != null && !categorySlug.isBlank() && !categorySlug.equals("all")) {
                ps.setString(1, categorySlug);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) articles.add(rowToMap(rs));
        } catch (SQLException e) {
            System.err.println("Error listing articles: " + e.getMessage());
        }
        return articles;
    }

    /**
     * Get a single article by id.
     */
    public static Map<String, Object> getById(int id) {
        if (!DatabaseConfig.isAvailable()) return null;
        String sql = "SELECT a.*, c.label as category_label FROM articles a " +
                     "JOIN categories c ON a.category_slug = c.slug WHERE a.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rowToMap(rs);
        } catch (SQLException e) {
            System.err.println("Error getting article: " + e.getMessage());
        }
        return null;
    }

    /**
     * Delete an article by id.
     */
    public static boolean delete(int id) {
        if (!DatabaseConfig.isAvailable()) return false;
        String sql = "DELETE FROM articles WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting article: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get article count.
     */
    public static int count() {
        if (!DatabaseConfig.isAvailable()) return 0;
        String sql = "SELECT COUNT(*) FROM articles";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting articles: " + e.getMessage());
        }
        return 0;
    }

    /**
     * List all categories.
     */
    public static List<Map<String, Object>> listCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        if (!DatabaseConfig.isAvailable()) return categories;
        String sql = "SELECT * FROM categories ORDER BY id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", rs.getInt("id"));
                cat.put("slug", rs.getString("slug"));
                cat.put("label", rs.getString("label"));
                categories.add(cat);
            }
        } catch (SQLException e) {
            System.err.println("Error listing categories: " + e.getMessage());
        }
        return categories;
    }

    private static Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rs.getInt("id"));
        map.put("title", rs.getString("title"));
        map.put("excerpt", rs.getString("excerpt"));
        map.put("content", rs.getString("content"));
        map.put("author", rs.getString("author"));
        map.put("categorySlug", rs.getString("category_slug"));
        map.put("categoryLabel", rs.getString("category_label"));
        map.put("featured", rs.getBoolean("featured"));
        map.put("readTime", rs.getString("read_time"));
        map.put("createdAt", rs.getTimestamp("created_at").toString());
        return map;
    }
}

