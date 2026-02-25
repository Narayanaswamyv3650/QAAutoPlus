package com.qaautoplus.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qaautoplus.db.ArticleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Servlet – handles /api/* requests for articles and status.
 */
public class ApiServlet extends HttpServlet {

    private final ObjectMapper mapper;

    public ApiServlet() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        if (path == null || path.equals("/") || path.equals("/status")) {
            sendJson(resp, 200, Map.of("status", "OK", "version", "1.0.0"));
            return;
        }

        if (path.equals("/articles")) {
            String category = req.getParameter("category");
            List<Map<String, Object>> articles = ArticleDAO.list(category);
            sendJson(resp, 200, Map.of("articles", articles, "count", articles.size()));
            return;
        }

        if (path.startsWith("/articles/")) {
            try {
                int id = Integer.parseInt(path.substring("/articles/".length()));
                Map<String, Object> article = ArticleDAO.getById(id);
                if (article != null) { sendJson(resp, 200, article); return; }
                sendError(resp, 404, "Article not found");
            } catch (NumberFormatException e) {
                sendError(resp, 400, "Invalid article id");
            }
            return;
        }

        if (path.equals("/categories")) {
            sendJson(resp, 200, Map.of("categories", ArticleDAO.listCategories()));
            return;
        }

        sendError(resp, 404, "Endpoint not found");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        if (path != null && path.equals("/articles")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = mapper.readValue(req.getReader(), Map.class);

            String title = str(body, "title");
            String excerpt = str(body, "excerpt");
            String content = str(body, "content");
            String author = str(body, "author");
            String category = str(body, "category");
            boolean featured = Boolean.TRUE.equals(body.get("featured"));

            if (title.isBlank() || excerpt.isBlank() || content.isBlank() || author.isBlank() || category.isBlank()) {
                sendError(resp, 400, "title, excerpt, content, author, and category are required");
                return;
            }

            Map<String, Object> created = ArticleDAO.create(title, excerpt, content, author, category, featured);
            if (created != null) {
                sendJson(resp, 201, created);
            } else {
                sendError(resp, 500, "Failed to create article");
            }
            return;
        }

        sendError(resp, 404, "Endpoint not found");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        if (path != null && path.startsWith("/articles/")) {
            try {
                int id = Integer.parseInt(path.substring("/articles/".length()));
                if (ArticleDAO.delete(id)) {
                    sendJson(resp, 200, Map.of("deleted", true));
                } else {
                    sendError(resp, 404, "Article not found");
                }
            } catch (NumberFormatException e) {
                sendError(resp, 400, "Invalid article id");
            }
            return;
        }

        sendError(resp, 404, "Endpoint not found");
    }

    private void sendJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), data);
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("error", true);
        err.put("status", status);
        err.put("message", message);
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), err);
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString().trim() : "";
    }
}
