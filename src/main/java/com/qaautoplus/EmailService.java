package com.qaautoplus;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Sends subscription notification emails via Gmail SMTP.
 * Config is read from application.properties via AppConfig.
 */
public class EmailService {

    public static void sendSubscriptionNotification(String subscriberName, String subscriberEmail)
            throws MessagingException {

        Properties cfg = AppConfig.get();
        String host     = cfg.getProperty("mail.smtp.host", "smtp.gmail.com");
        String port     = cfg.getProperty("mail.smtp.port", "587");
        String fromAddr = cfg.getProperty("mail.from", "");
        String password = cfg.getProperty("mail.password", "");
        String toAddr   = cfg.getProperty("mail.notify.to", fromAddr);

        if (fromAddr.isBlank() || password.isBlank() || "your-gmail@gmail.com".equals(fromAddr)) {
            throw new MessagingException(
                "Email not configured. Set mail.from and mail.password in application.properties.");
        }

        Properties smtp = new Properties();
        smtp.put("mail.smtp.auth", "true");
        smtp.put("mail.smtp.starttls.enable", "true");
        smtp.put("mail.smtp.host", host);
        smtp.put("mail.smtp.port", port);
        smtp.put("mail.smtp.connectiontimeout", "10000");
        smtp.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(smtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromAddr, password);
            }
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(fromAddr, "QAAutoPlus Newsletter"));
        } catch (java.io.UnsupportedEncodingException e) {
            msg.setFrom(new InternetAddress(fromAddr));
        }
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddr));
        msg.setSubject("New QAAutoPlus Subscriber: " + subscriberName);

        String html = "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:560px;margin:0 auto;"
            + "border:1px solid #e0e0e0;border-radius:12px;overflow:hidden\">"
            + "<div style=\"background:linear-gradient(135deg,#4F46E5,#7C3AED);padding:28px 24px;color:#fff\">"
            + "<h1 style=\"margin:0;font-size:1.4rem\">New Newsletter Subscriber!</h1>"
            + "<p style=\"margin:8px 0 0;opacity:.85;font-size:.9rem\">Someone just subscribed to QAAutoPlus Weekly</p>"
            + "</div><div style=\"padding:24px\">"
            + "<table style=\"width:100%;border-collapse:collapse\">"
            + "<tr><td style=\"padding:10px 0;color:#888;font-size:.85rem;width:100px\">Name</td>"
            + "<td style=\"padding:10px 0;font-weight:600;font-size:1rem\">" + esc(subscriberName) + "</td></tr>"
            + "<tr><td style=\"padding:10px 0;color:#888;font-size:.85rem;border-top:1px solid #f0f0f0\">Email</td>"
            + "<td style=\"padding:10px 0;font-size:1rem;border-top:1px solid #f0f0f0\">"
            + "<a href=\"mailto:" + esc(subscriberEmail) + "\" style=\"color:#4F46E5\">" + esc(subscriberEmail) + "</a></td></tr>"
            + "<tr><td style=\"padding:10px 0;color:#888;font-size:.85rem;border-top:1px solid #f0f0f0\">Time</td>"
            + "<td style=\"padding:10px 0;font-size:.9rem;color:#666;border-top:1px solid #f0f0f0\">" + timestamp + "</td></tr>"
            + "</table></div>"
            + "<div style=\"background:#f9fafb;padding:16px 24px;font-size:.75rem;color:#999;"
            + "text-align:center;border-top:1px solid #e0e0e0\">Sent by QAAutoPlus Newsletter System</div></div>";

        msg.setContent(html, "text/html; charset=UTF-8");
        Transport.send(msg);
        System.out.println("  [EmailService] Notification sent to " + toAddr + " (subscriber: " + subscriberEmail + ")");
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}

