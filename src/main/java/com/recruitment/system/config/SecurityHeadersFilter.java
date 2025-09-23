package com.recruitment.system.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter để thêm các security headers bảo vệ ứng dụng
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SecurityHeadersFilter implements Filter {

    @Value("${security.headers.enabled:true}")
    private boolean headersEnabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (headersEnabled) {
            addSecurityHeaders(httpRequest, httpResponse);
        }

        chain.doFilter(request, response);
    }

    /**
     * Thêm các security headers
     */
    private void addSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        // Ngăn chặn clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Ngăn chặn MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Bật XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Strict Transport Security (HTTPS only)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), " +
            "microphone=(), " +
            "camera=(), " +
            "payment=(), " +
            "usb=(), " +
            "magnetometer=(), " +
            "gyroscope=(), " +
            "speaker=(), " +
            "vibrate=(), " +
            "fullscreen=(self), " +
            "sync-xhr=()");
        
        // Cache Control cho sensitive endpoints
        if (isSensitiveEndpoint(request)) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
    }

    /**
     * Kiểm tra xem endpoint có chứa thông tin nhạy cảm không
     */
    private boolean isSensitiveEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/auth/") || 
               path.contains("/admin/") || 
               path.contains("/user/") ||
               path.contains("/profile/");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("SecurityHeadersFilter initialized with headers enabled: {}", headersEnabled);
    }

    @Override
    public void destroy() {
        log.info("SecurityHeadersFilter destroyed");
    }
}








