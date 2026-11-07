package com.smartlocal.service.impl;

import com.smartlocal.exception.EmailDeliveryException;
import com.smartlocal.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Production implementation of EmailService using JavaMailSender and SMTP.
 * Never prints or leaks OTPs in application logs.
 * Throws EmailDeliveryException when credentials are unconfigured or sending fails.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:no-reply@smartlocal.com}")
    private String mailFrom;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String recipientName) throws EmailDeliveryException {
        // Enforce that SMTP credentials must be configured
        if (mailUsername == null || mailUsername.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            log.error("Email dispatch failed: SMTP credentials (MAIL_USERNAME / MAIL_PASSWORD) are not configured in environment.");
            throw new EmailDeliveryException(
                    "SMTP email service is not configured. Please set the MAIL_USERNAME and MAIL_PASSWORD environment variables to send real verification emails."
            );
        }

        String greeting = (recipientName != null && !recipientName.isBlank()) ? recipientName.trim() : "Valued User";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String senderAddress = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom.trim() : mailUsername.trim();
            helper.setFrom(senderAddress, "SmartLocal Team");
            helper.setTo(toEmail.trim());
            helper.setSubject("SmartLocal Account Verification - Your One-Time Password");

            String htmlBody = buildVerificationEmailHtml(greeting, otpCode);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("Verification email successfully delivered to recipient [{}] via host [{}]", toEmail, mailHost);

        } catch (MessagingException | MailException ex) {
            log.error("SMTP delivery failure to [{}]: {}", toEmail, ex.getMessage());
            throw new EmailDeliveryException("Failed to send verification email: " + ex.getMessage(), ex);
        } catch (UnsupportedEncodingException ex) {
            log.error("Sender personal name encoding error: {}", ex.getMessage());
            throw new EmailDeliveryException("Email sender configuration error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Builds a clean, professional HTML verification email template.
     */
    private String buildVerificationEmailHtml(String greeting, String otpCode) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px;\">"
                + "<div style=\"max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);\">"
                + "  <div style=\"background: #2563eb; padding: 24px; text-align: center;\">"
                + "    <h1 style=\"color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;\">SmartLocal</h1>"
                + "    <p style=\"color: #dbeafe; margin: 4px 0 0 0; font-size: 13px;\">Hyperlocal Product Discovery & Inventory System</p>"
                + "  </div>"
                + "  <div style=\"padding: 32px 28px;\">"
                + "    <h2 style=\"color: #0f172a; margin-top: 0; font-size: 20px;\">Verify Your Email Address</h2>"
                + "    <p style=\"color: #475569; font-size: 15px; line-height: 1.5;\">Hello <strong>" + escapeHtml(greeting) + "</strong>,</p>"
                + "    <p style=\"color: #475569; font-size: 15px; line-height: 1.5;\">Thank you for creating an account on SmartLocal. Please enter the verification code below on the registration page to activate your account:</p>"
                + "    <div style=\"background-color: #f1f5f9; border: 2px dashed #cbd5e1; border-radius: 8px; padding: 18px; text-align: center; margin: 24px 0;\">"
                + "      <span style=\"font-size: 34px; font-weight: 800; letter-spacing: 8px; color: #1e293b; font-family: 'Courier New', monospace;\">" + otpCode + "</span>"
                + "    </div>"
                + "    <p style=\"color: #dc2626; font-size: 13px; margin: 16px 0 8px 0;\"><strong>⏳ Security Notice:</strong> This code is valid for <strong>5 minutes</strong> and can only be used once.</p>"
                + "    <p style=\"color: #64748b; font-size: 13px; line-height: 1.5; margin: 0;\">If you did not attempt to register on SmartLocal, please disregard this email.</p>"
                + "  </div>"
                + "  <div style=\"background: #f8fafc; padding: 16px; text-align: center; border-top: 1px solid #e2e8f0;\">"
                + "    <p style=\"color: #94a3b8; font-size: 12px; margin: 0;\">&copy; 2026 SmartLocal. All rights reserved.</p>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
