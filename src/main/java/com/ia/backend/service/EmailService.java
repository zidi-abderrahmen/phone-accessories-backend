package com.ia.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String to, String firstName, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email Address");
            helper.setText(buildVerificationHtml(firstName, verificationLink), true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationHtml(String firstName, String link) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }
                    .btn { display: inline-block; padding: 12px 24px; background: #4CAF50; color: white; 
                           text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { color: #888; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Welcome, %s!</h2>
                    <p>Please verify your email by clicking the button below:</p>
                    <a href="%s" class="btn">Verify Email</a>
                    <p>Or copy this link:</p>
                    <p>%s</p>
                    <p>This link expires in 15 minutes.</p>
                    <div class="footer">
                        <p>If you didn't register, ignore this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, link, link);
    }
}