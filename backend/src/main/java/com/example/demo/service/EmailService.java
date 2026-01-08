package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Project Tracker}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String firstName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify your " + appName + " account");

            String htmlContent = buildVerificationEmailHtml(firstName, code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String firstName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset your " + appName + " password");

            String resetLink = frontendUrl + "/reset-password?email=" + to + "&code=" + code;
            String htmlContent = buildPasswordResetEmailHtml(firstName, code, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildVerificationEmailHtml(String firstName, String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #ffffff; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                            <div style="text-align: center; margin-bottom: 30px;">
                                <h1 style="color: #2563eb; margin: 0;">%s</h1>
                            </div>
                            
                            <h2 style="color: #333333; margin-bottom: 20px;">Verify Your Email Address</h2>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                Hi %s,
                            </p>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                Thank you for registering! Please use the verification code below to complete your registration:
                            </p>
                            
                            <div style="background-color: #f0f7ff; border: 2px dashed #2563eb; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;">
                                <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #2563eb;">%s</span>
                            </div>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                This code will expire in <strong>15 minutes</strong>.
                            </p>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                If you didn't create an account with us, please ignore this email.
                            </p>
                            
                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
                            
                            <p style="color: #999999; font-size: 12px; text-align: center;">
                                This is an automated message from %s. Please do not reply to this email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, firstName, code, appName);
    }

    private String buildPasswordResetEmailHtml(String firstName, String code, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #ffffff; border-radius: 10px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                            <div style="text-align: center; margin-bottom: 30px;">
                                <h1 style="color: #2563eb; margin: 0;">%s</h1>
                            </div>
                            
                            <h2 style="color: #333333; margin-bottom: 20px;">Reset Your Password</h2>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                Hi %s,
                            </p>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                We received a request to reset your password. Use the code below or click the button to reset your password:
                            </p>
                            
                            <div style="background-color: #fff3cd; border: 2px dashed #ffc107; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;">
                                <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #856404;">%s</span>
                            </div>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 5px; font-weight: bold; font-size: 16px;">
                                    Reset Password
                                </a>
                            </div>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                This code and link will expire in <strong>15 minutes</strong>.
                            </p>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                If you didn't request a password reset, please ignore this email. Your password will remain unchanged.
                            </p>
                            
                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
                            
                            <p style="color: #999999; font-size: 12px; text-align: center;">
                                This is an automated message from %s. Please do not reply to this email.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, firstName, code, resetLink, appName);
    }
}