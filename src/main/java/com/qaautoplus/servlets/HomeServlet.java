package com.qaautoplus.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Home Servlet - Serves the main page
 */
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>QA Auto Plus - Home</title>");
        out.println("    <link rel='stylesheet' href='/css/style.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <header>");
        out.println("            <h1>Welcome to QA Auto Plus</h1>");
        out.println("            <p>Your comprehensive QA automation testing platform</p>");
        out.println("        </header>");
        out.println("        <main>");
        out.println("            <section class='features'>");
        out.println("                <div class='feature-card'>");
        out.println("                    <h2>Test Automation</h2>");
        out.println("                    <p>Automate your testing workflows with ease</p>");
        out.println("                </div>");
        out.println("                <div class='feature-card'>");
        out.println("                    <h2>API Testing</h2>");
        out.println("                    <p>Test and validate your APIs efficiently</p>");
        out.println("                </div>");
        out.println("                <div class='feature-card'>");
        out.println("                    <h2>Reporting</h2>");
        out.println("                    <p>Generate comprehensive test reports</p>");
        out.println("                </div>");
        out.println("            </section>");
        out.println("            <section class='actions'>");
        out.println("                <button onclick='testApi()'>Test API Connection</button>");
        out.println("            </section>");
        out.println("            <div id='result'></div>");
        out.println("        </main>");
        out.println("        <footer>");
        out.println("            <p>&copy; 2026 QA Auto Plus. All rights reserved.</p>");
        out.println("        </footer>");
        out.println("    </div>");
        out.println("    <script src='/js/app.js'></script>");
        out.println("</body>");
        out.println("</html>");
    }
}

