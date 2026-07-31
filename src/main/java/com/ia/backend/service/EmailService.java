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
    public void sendEmail(String to, String firstName, String verificationLink, boolean isResetPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = isResetPassword ? "Reset Your Password" : "Verify Your Email Address";
            String htmlContent = isResetPassword
                    ? buildResetPasswordHtml(firstName, verificationLink)
                    : buildVerificationHtml(firstName, verificationLink);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("{} email sent to: {}", isResetPassword ? "Reset password" : "Verification", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send " + (isResetPassword ? "reset password" : "verification") + " email", e);
        }
    }

    private String buildVerificationHtml(String firstName, String link) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                            background: #f4f4f7;
                            margin: 0;
                            padding: 20px;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: #ffffff;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
                        }
                        .header {
                            background: linear-gradient(135deg, #4CAF50, #43A047);
                            padding: 32px 40px;
                            text-align: center;
                        }
                        .header h1 {
                            color: #ffffff;
                            margin: 0;
                            font-size: 22px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px;
                        }
                        .content h2 {
                            margin-top: 0;
                            font-size: 20px;
                            color: #1a1a1a;
                        }
                        .content p {
                            font-size: 15px;
                            line-height: 1.6;
                            color: #555;
                        }
                        .btn-wrapper {
                            text-align: center;
                            margin: 32px 0;
                        }
                        .btn {
                            display: inline-block;
                            padding: 14px 32px;
                            background: #4CAF50;
                            color: #ffffff !important;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 15px;
                        }
                        .link-box {
                            background: #f7f7f9;
                            border: 1px solid #e5e5e8;
                            border-radius: 6px;
                            padding: 12px 16px;
                            font-size: 13px;
                            color: #4CAF50;
                            word-break: break-all;
                        }
                        .expiry-note {
                            font-size: 13px;
                            color: #999;
                            margin-top: 16px;
                        }
                        .footer {
                            background: #fafafa;
                            padding: 24px 40px;
                            text-align: center;
                            font-size: 12px;
                            color: #999;
                            border-top: 1px solid #eee;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Confirm Your Email</h1>
                        </div>
                        <div class="content">
                            <h2>Welcome, %s!</h2>
                            <p>Thanks for signing up. Please verify your email address to activate your account and get started.</p>
                            <div class="btn-wrapper">
                                <a href="%s" class="btn">Verify Email</a>
                            </div>
                            <p>Or copy and paste this link into your browser:</p>
                            <div class="link-box">%s</div>
                            <p class="expiry-note">⏱ This link will expire in 15 minutes.</p>
                        </div>
                        <div class="footer">
                            <p>If you didn't create an account, you can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(firstName, link, link);
    }

    private String buildResetPasswordHtml(String firstName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                            background: #f4f4f7;
                            margin: 0;
                            padding: 20px;
                            color: #333;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: #ffffff;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
                        }
                        .header {
                            background: linear-gradient(135deg, #E53935, #D32F2F);
                            padding: 32px 40px;
                            text-align: center;
                        }
                        .header h1 {
                            color: #ffffff;
                            margin: 0;
                            font-size: 22px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px;
                        }
                        .content h2 {
                            margin-top: 0;
                            font-size: 20px;
                            color: #1a1a1a;
                        }
                        .content p {
                            font-size: 15px;
                            line-height: 1.6;
                            color: #555;
                        }
                        .btn-wrapper {
                            text-align: center;
                            margin: 32px 0;
                        }
                        .btn {
                            display: inline-block;
                            padding: 14px 32px;
                            background: #E53935;
                            color: #ffffff !important;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 15px;
                        }
                        .link-box {
                            background: #f7f7f9;
                            border: 1px solid #e5e5e8;
                            border-radius: 6px;
                            padding: 12px 16px;
                            font-size: 13px;
                            color: #E53935;
                            word-break: break-all;
                        }
                        .expiry-note {
                            font-size: 13px;
                            color: #999;
                            margin-top: 16px;
                        }
                        .security-note {
                            background: #FFF8E1;
                            border: 1px solid #FFECB3;
                            border-radius: 6px;
                            padding: 14px 16px;
                            font-size: 13px;
                            color: #8a6d00;
                            margin-top: 24px;
                        }
                        .footer {
                            background: #fafafa;
                            padding: 24px 40px;
                            text-align: center;
                            font-size: 12px;
                            color: #999;
                            border-top: 1px solid #eee;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Reset Your Password</h1>
                        </div>
                        <div class="content">
                            <h2>Hi %s,</h2>
                            <p>We received a request to reset the password for your account. Click the button below to choose a new password.</p>
                
                            <div class="btn-wrapper">
                                <a href="%s" class="btn">Reset Password</a>
                            </div>
                
                            <p>Or copy and paste this link into your browser:</p>
                            <div class="link-box">%s</div>
                
                            <p class="expiry-note">⏱ This link will expire in 15 minutes.</p>
                
                            <div class="security-note">
                                🔒 If you didn't request a password reset, please ignore this email — your password will remain unchanged. Consider reviewing your account activity if you're unsure.
                            </div>
                        </div>
                        <div class="footer">
                            <p>This is an automated message, please don't reply directly to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(firstName, resetLink, resetLink);
    }
}