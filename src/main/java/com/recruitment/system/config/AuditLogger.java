package com.recruitment.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit Logger để ghi lại các hoạt động bảo mật quan trọng
 */
@Component
@Slf4j
public class AuditLogger {

    private static final String AUDIT_PREFIX = "[AUDIT]";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Ghi log đăng nhập thành công
     */
    public void logLoginSuccess(String email, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.info("{} {} LOGIN_SUCCESS - User: {}, IP: {}, UserAgent: {}", 
                AUDIT_PREFIX, timestamp, email, ipAddress, userAgent);
    }

    /**
     * Ghi log đăng nhập thất bại
     */
    public void logLoginFailure(String email, String ipAddress, String userAgent, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.warn("{} {} LOGIN_FAILURE - User: {}, IP: {}, UserAgent: {}, Reason: {}", 
                AUDIT_PREFIX, timestamp, email, ipAddress, userAgent, reason);
    }

    /**
     * Ghi log đăng ký thành công
     */
    public void logRegistrationSuccess(String email, String ipAddress, String userAgent, String role) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.info("{} {} REGISTRATION_SUCCESS - User: {}, IP: {}, UserAgent: {}, Role: {}", 
                AUDIT_PREFIX, timestamp, email, ipAddress, userAgent, role);
    }

    /**
     * Ghi log đăng ký thất bại
     */
    public void logRegistrationFailure(String email, String ipAddress, String userAgent, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.warn("{} {} REGISTRATION_FAILURE - User: {}, IP: {}, UserAgent: {}, Reason: {}", 
                AUDIT_PREFIX, timestamp, email, ipAddress, userAgent, reason);
    }

    /**
     * Ghi log đăng xuất
     */
    public void logLogout(String email, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.info("{} {} LOGOUT - User: {}, IP: {}, UserAgent: {}", 
                AUDIT_PREFIX, timestamp, email, ipAddress, userAgent);
    }

    /**
     * Ghi log refresh token
     */
    public void logTokenRefresh(String email, String ipAddress, String userAgent, boolean success) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String action = success ? "TOKEN_REFRESH_SUCCESS" : "TOKEN_REFRESH_FAILURE";
        if (success) {
            log.info("{} {} {} - User: {}, IP: {}, UserAgent: {}", 
                    AUDIT_PREFIX, timestamp, action, email, ipAddress, userAgent);
        } else {
            log.warn("{} {} {} - User: {}, IP: {}, UserAgent: {}", 
                    AUDIT_PREFIX, timestamp, action, email, ipAddress, userAgent);
        }
    }

    /**
     * Ghi log truy cập không được phép
     */
    public void logUnauthorizedAccess(String endpoint, String ipAddress, String userAgent, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.warn("{} {} UNAUTHORIZED_ACCESS - Endpoint: {}, IP: {}, UserAgent: {}, Reason: {}", 
                AUDIT_PREFIX, timestamp, endpoint, ipAddress, userAgent, reason);
    }

    /**
     * Ghi log rate limit exceeded
     */
    public void logRateLimitExceeded(String operation, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.warn("{} {} RATE_LIMIT_EXCEEDED - Operation: {}, IP: {}, UserAgent: {}", 
                AUDIT_PREFIX, timestamp, operation, ipAddress, userAgent);
    }

    /**
     * Ghi log token manipulation
     */
    public void logTokenManipulation(String tokenType, String ipAddress, String userAgent, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.error("{} {} TOKEN_MANIPULATION - Type: {}, IP: {}, UserAgent: {}, Reason: {}", 
                AUDIT_PREFIX, timestamp, tokenType, ipAddress, userAgent, reason);
    }

    /**
     * Ghi log thay đổi thông tin nhạy cảm
     */
    public void logSensitiveDataChange(String email, String changeType, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.info("{} {} SENSITIVE_DATA_CHANGE - User: {}, Change: {}, IP: {}, UserAgent: {}", 
                AUDIT_PREFIX, timestamp, email, changeType, ipAddress, userAgent);
    }

    /**
     * Ghi log bảo mật chung
     */
    public void logSecurityEvent(String eventType, String description, String ipAddress, String userAgent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        log.info("{} {} SECURITY_EVENT - Type: {}, Description: {}, IP: {}, UserAgent: {}", 
                AUDIT_PREFIX, timestamp, eventType, description, ipAddress, userAgent);
    }
}








