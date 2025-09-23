package com.recruitment.system.service;

import com.recruitment.system.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service xử lý gửi email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.verification.expiration:24}")
    private int verificationExpirationHours;

    /**
     * Gửi email xác minh địa chỉ email
     */
    public void sendVerificationEmail(User user, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Xác minh địa chỉ email - Hệ thống Tuyển dụng");

            // Tạo nội dung email bằng HTML template
            String htmlContent = createVerificationEmailContent(user, verificationToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Không thể gửi email xác minh", e);
        }
    }

    /**
     * Tạo nội dung HTML cho email xác minh
     */
    private String createVerificationEmailContent(User user, String verificationToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFullName());
        variables.put("verificationLink", frontendUrl + "/verify-email?token=" + verificationToken);
        variables.put("expirationHours", verificationExpirationHours);
        variables.put("currentYear", LocalDateTime.now().getYear());
        variables.put("appName", "Hệ thống Tuyển dụng");

        return createHtmlTemplate("verification-email", variables);
    }

    /**
     * Tạo template HTML từ Thymeleaf hoặc HTML thuần
     */
    private String createHtmlTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.warn("Template engine not available, using fallback HTML template");
            return createFallbackHtmlTemplate(variables);
        }
    }

    /**
     * Template HTML fallback khi không có Thymeleaf
     */
    private String createFallbackHtmlTemplate(Map<String, Object> variables) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác minh email</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Xác minh địa chỉ email</h1>
                    </div>
                    <div class="content">
                        <h2>Xin chào {userName}!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>{appName}</strong>.</p>
                        <p>Để hoàn tất quá trình đăng ký và kích hoạt tài khoản, vui lòng xác minh địa chỉ email của bạn bằng cách nhấp vào nút bên dưới:</p>
                        
                        <div style="text-align: center;">
                            <a href="{verificationLink}" class="button">Xác minh Email</a>
                        </div>
                        
                        <div class="warning">
                            <strong>Lưu ý:</strong> Link xác minh sẽ hết hạn sau {expirationHours} giờ. Nếu bạn không thực hiện xác minh trong thời gian này, bạn sẽ cần yêu cầu gửi lại email xác minh.
                        </div>
                        
                        <p>Nếu nút không hoạt động, bạn có thể sao chép và dán link sau vào trình duyệt:</p>
                        <p style="word-break: break-all; background: #e9ecef; padding: 10px; border-radius: 5px; font-family: monospace;">
                            {verificationLink}
                        </p>
                        
                        <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
                        <p>Trân trọng,<br>Đội ngũ {appName}</p>
                    </div>
                    <div class="footer">
                        <p>&copy; {currentYear} {appName}. Tất cả quyền được bảo lưu.</p>
                    </div>
                </div>
            </body>
            </html>
            """.replace("{userName}", (String) variables.get("userName"))
              .replace("{appName}", (String) variables.get("appName"))
              .replace("{verificationLink}", (String) variables.get("verificationLink"))
              .replace("{expirationHours}", String.valueOf(variables.get("expirationHours")))
              .replace("{currentYear}", String.valueOf(variables.get("currentYear")));
    }

    /**
     * Gửi email thông báo xác minh thành công
     */
    public void sendVerificationSuccessEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Email đã được xác minh thành công - Hệ thống Tuyển dụng");

            String htmlContent = createVerificationSuccessContent(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification success email sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send verification success email to: {}", user.getEmail(), e);
            // Không throw exception vì đây chỉ là thông báo
        }
    }

    /**
     * Tạo nội dung email xác minh thành công
     */
    private String createVerificationSuccessContent(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFullName());
        variables.put("loginLink", frontendUrl + "/login");
        variables.put("currentYear", LocalDateTime.now().getYear());
        variables.put("appName", "Hệ thống Tuyển dụng");

        return createFallbackHtmlTemplate(variables).replace(
            "Xác minh địa chỉ email",
            "Email đã được xác minh thành công"
        ).replace(
            "Để hoàn tất quá trình đăng ký và kích hoạt tài khoản, vui lòng xác minh địa chỉ email của bạn bằng cách nhấp vào nút bên dưới:",
            "Tài khoản của bạn đã được kích hoạt thành công! Bạn có thể đăng nhập và sử dụng tất cả tính năng của hệ thống."
        ).replace(
            "Xác minh Email",
            "Đăng nhập ngay"
        ).replace(
            "{verificationLink}",
            (String) variables.get("loginLink")
        ).replace(
            "Link xác minh sẽ hết hạn sau {expirationHours} giờ. Nếu bạn không thực hiện xác minh trong thời gian này, bạn sẽ cần yêu cầu gửi lại email xác minh.",
            "Chào mừng bạn đến với {appName}! Bạn có thể bắt đầu sử dụng tài khoản ngay bây giờ."
        );
    }
}
