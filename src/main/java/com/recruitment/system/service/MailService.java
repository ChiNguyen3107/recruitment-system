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
     * Gửi email thông báo thay đổi trạng thái đơn ứng tuyển cho ứng viên
     */
    public void sendApplicationStatusChangedEmail(User applicant,
                                                  String jobTitle,
                                                  String newStatusDisplay,
                                                  String notes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(applicant.getEmail());
            helper.setSubject("Cập nhật trạng thái đơn ứng tuyển - " + jobTitle);

            String html = buildStatusChangedHtml(applicant.getFullName(), jobTitle, newStatusDisplay, notes);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Status change email sent to applicant: {} for job '{}'", applicant.getEmail(), jobTitle);
        } catch (MessagingException e) {
            log.error("Failed to send status change email to applicant: {}", applicant.getEmail(), e);
            // Không throw để không chặn luồng xử lý
        }
    }

    private String buildStatusChangedHtml(String userName, String jobTitle, String statusDisplay, String notes) {
        String appName = "Hệ thống Tuyển dụng";
        int year = LocalDateTime.now().getYear();
        String notesBlock = (notes != null && !notes.isBlank())
            ? ("<div style=\"background:#f1f3f5;padding:12px;border-radius:6px;margin-top:12px;\"><strong>Ghi chú:</strong> " +
               notes.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</div>")
            : "";
        return (
            "<!DOCTYPE html>" +
            "<html lang=\"vi\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Cập nhật trạng thái</title>" +
            "<style>body{font-family:Arial,sans-serif;color:#333}.container{max-width:600px;margin:0 auto;padding:20px}.header{background:#0069d9;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}.content{background:#f8f9fa;padding:30px;border-radius:0 0 8px 8px}.footer{text-align:center;margin-top:30px;color:#666;font-size:12px}</style></head>" +
            "<body><div class=\"container\"><div class=\"header\"><h1>📣 Cập nhật trạng thái</h1></div>" +
            "<div class=\"content\"><p>Xin chào <strong>" + userName + "</strong>,</p>" +
            "<p>Trạng thái đơn ứng tuyển cho vị trí <strong>" + jobTitle + "</strong> đã được cập nhật thành: <strong>" + statusDisplay + "</strong>.</p>" +
            notesBlock +
            "<p>Vui lòng đăng nhập hệ thống để xem chi tiết.</p>" +
            "<p>Trân trọng,<br>Đội ngũ " + appName + "</p></div>" +
            "<div class=\"footer\">&copy; " + year + " " + appName + ". Tất cả quyền được bảo lưu.</div>" +
            "</div></body></html>"
        );
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

    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Đặt lại mật khẩu - Hệ thống Tuyển dụng");

            // Tạo nội dung email bằng HTML template
            String htmlContent = createPasswordResetEmailContent(user, resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        }
    }

    /**
     * Tạo nội dung HTML cho email reset password
     */
    private String createPasswordResetEmailContent(User user, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFullName());
        variables.put("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
        variables.put("expirationHours", 1); // Token hết hạn sau 1 giờ
        variables.put("currentYear", LocalDateTime.now().getYear());
        variables.put("appName", "Hệ thống Tuyển dụng");

        return createPasswordResetHtmlTemplate(variables);
    }

    /**
     * Template HTML cho email reset password
     */
    private String createPasswordResetHtmlTemplate(Map<String, Object> variables) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #dc3545; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .security { background: #d1ecf1; border: 1px solid #bee5eb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 Đặt lại mật khẩu</h1>
                    </div>
                    <div class="content">
                        <h2>Xin chào {userName}!</h2>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại <strong>{appName}</strong>.</p>
                        
                        <div style="text-align: center;">
                            <a href="{resetLink}" class="button">Đặt lại mật khẩu</a>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Lưu ý quan trọng:</strong> Link đặt lại mật khẩu sẽ hết hạn sau {expirationHours} giờ. Vui lòng thực hiện đặt lại mật khẩu trong thời gian này.
                        </div>
                        
                        <div class="security">
                            <strong>🔐 Bảo mật:</strong> Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Mật khẩu hiện tại của bạn sẽ không bị thay đổi.
                        </div>
                        
                        <p>Nếu nút không hoạt động, bạn có thể sao chép và dán link sau vào trình duyệt:</p>
                        <p style="word-break: break-all; background: #e9ecef; padding: 10px; border-radius: 5px; font-family: monospace;">
                            {resetLink}
                        </p>
                        
                        <p>Nếu bạn gặp vấn đề hoặc cần hỗ trợ, vui lòng liên hệ với chúng tôi.</p>
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
              .replace("{resetLink}", (String) variables.get("resetLink"))
              .replace("{expirationHours}", String.valueOf(variables.get("expirationHours")))
              .replace("{currentYear}", String.valueOf(variables.get("currentYear")));
    }

    /**
     * Gửi email thông báo cho nhà tuyển dụng khi có đơn ứng tuyển mới
     */
    public void sendNewApplicationEmail(User employer, String jobTitle, String applicantName, String applicationDetailLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(employer.getEmail());
            helper.setSubject("Đã nhận đơn ứng tuyển mới cho vị trí: " + jobTitle);

            String html = createNewApplicationHtml(jobTitle, applicantName, applicationDetailLink);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("New application email sent to employer: {} for job '{}'", employer.getEmail(), jobTitle);
        } catch (MessagingException e) {
            log.error("Failed to send new application email to employer: {}", employer.getEmail(), e);
            // Không throw để không chặn luồng nộp đơn
        }
    }

    private String createNewApplicationHtml(String jobTitle, String applicantName, String detailLink) {
        String appName = "Hệ thống Tuyển dụng";
        int year = LocalDateTime.now().getYear();
        return (
            "<!DOCTYPE html>" +
            "<html lang=\"vi\">" +
            "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Đơn ứng tuyển mới</title>" +
            "<style>body{font-family:Arial,sans-serif;color:#333}.container{max-width:600px;margin:0 auto;padding:20px}.header{background:#28a745;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0}.content{background:#f8f9fa;padding:30px;border-radius:0 0 8px 8px}.button{display:inline-block;background:#28a745;color:#fff;padding:12px 24px;text-decoration:none;border-radius:5px;margin:20px 0}.footer{text-align:center;margin-top:30px;color:#666;font-size:12px}</style></head>" +
            "<body><div class=\"container\"><div class=\"header\"><h1>📥 Đơn ứng tuyển mới</h1></div>" +
            "<div class=\"content\"><p>Bạn vừa nhận một đơn ứng tuyển mới cho vị trí <strong>" + jobTitle + "</strong>.</p>" +
            "<p><strong>Ứng viên:</strong> " + applicantName + "</p>" +
            (detailLink != null && !detailLink.isEmpty() ? ("<div style=\"text-align:center;\"><a class=\"button\" href=\"" + detailLink + "\">Xem chi tiết đơn</a></div>") : "") +
            "<p>Vui lòng đăng nhập hệ thống để xem và xử lý hồ sơ ứng viên.</p>" +
            "<p>Trân trọng,<br>Đội ngũ " + appName + "</p></div>" +
            "<div class=\"footer\">&copy; " + year + " " + appName + ". Tất cả quyền được bảo lưu.</div>" +
            "</div></body></html>"
        );
    }
}
