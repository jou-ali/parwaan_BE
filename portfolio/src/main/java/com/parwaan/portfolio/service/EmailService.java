package com.parwaan.portfolio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@example.com}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetLink(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Password Reset");
            message.setText(
                "You requested a password reset.\n\n" +
                "Click the link to reset your password:\n" +
                resetLink + "\n\n" +
                "If you did not request this, ignore this email."
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
