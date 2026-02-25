package com.qaautoplus.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API Servlet - Handles API requests
 */
public class ApiServlet extends HttpServlet {

    private final ObjectMapper objectMapper;

    public ApiServlet() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/status")) {
            handleStatusRequest(response);
        } else if (pathInfo.equals("/info")) {
            handleInfoRequest(response);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    private void handleStatusRequest(HttpServletResponse response) throws IOException {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("message", "QA Auto Plus API is running");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("version", "1.0.0");

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), status);
    }

    private void handleInfoRequest(HttpServletResponse response) throws IOException {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "QA Auto Plus");
        info.put("description", "A comprehensive QA automation testing platform");
        info.put("version", "1.0.0");
        info.put("endpoints", new String[]{"/api/status", "/api/info"});

        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), info);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().toString());

        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), error);
    }
}

