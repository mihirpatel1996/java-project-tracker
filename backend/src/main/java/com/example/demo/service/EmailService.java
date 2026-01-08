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

    @Async
    public void sendStatusUpdateEmail(String to, String projectName, String oldStatus, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Project Status Update - " + projectName);

            String htmlContent = buildStatusUpdateEmailHtml(projectName, oldStatus, newStatus);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Status update email sent successfully to: {} for project: {}", to, projectName);
        } catch (MessagingException e) {
            logger.error("Failed to send status update email to: {} for project: {}", to, projectName, e);
            // Don't throw exception - status update email is not critical
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
                                We received a request to reset your password. Use the code below:
                            </p>
                            
                            <div style="background-color: #fff7ed; border: 2px dashed #f59e0b; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;">
                                <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #f59e0b;">%s</span>
                            </div>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                This code will expire in <strong>15 minutes</strong>.
                            </p>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                If you didn't request a password reset, please ignore this email or contact support if you have concerns.
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

    private String buildStatusUpdateEmailHtml(String projectName, String oldStatus, String newStatus) {
        String statusColor = getStatusColor(newStatus);
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
                            
                            <h2 style="color: #333333; margin-bottom: 20px;">Project Status Update</h2>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                The status of your project has been updated.
                            </p>
                            
                            <div style="background-color: #f8fafc; border-radius: 8px; padding: 20px; margin: 30px 0;">
                                <p style="color: #333333; font-size: 18px; font-weight: bold; margin: 0 0 15px 0;">
                                    %s
                                </p>
                                
                                <div style="display: flex; align-items: center; gap: 10px;">
                                    <span style="background-color: #e5e7eb; color: #6b7280; padding: 6px 12px; border-radius: 20px; font-size: 14px; font-weight: 500;">
                                        %s
                                    </span>
                                    <span style="color: #9ca3af; font-size: 20px;">→</span>
                                    <span style="background-color: %s; color: white; padding: 6px 12px; border-radius: 20px; font-size: 14px; font-weight: 500;">
                                        %s
                                    </span>
                                </div>
                            </div>
                            
                            <p style="color: #666666; font-size: 14px; line-height: 1.5;">
                                Log in to <a href="%s" style="color: #2563eb; text-decoration: none;">%s</a> to view more details about your project.
                            </p>
                            
                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
                            
                            <p style="color: #999999; font-size: 12px; text-align: center;">
                                You received this email because you have email notifications enabled for this project.
                                <br>To disable notifications, edit your project settings in %s.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, projectName, oldStatus, statusColor, newStatus, frontendUrl, appName, appName);
    }

    private String getStatusColor(String status) {
        if (status == null) return "#6b7280"; // gray
        return switch (status.toLowerCase()) {
            case "active" -> "#22c55e"; // green
            case "on hold" -> "#f59e0b"; // yellow/amber
            case "completed" -> "#3b82f6"; // blue
            case "cancelled" -> "#ef4444"; // red
            default -> "#6b7280"; // gray
        };
    }
}