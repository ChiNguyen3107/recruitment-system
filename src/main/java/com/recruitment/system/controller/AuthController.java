package com.recruitment.system.controller;

import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.config.RateLimitConfig;
import com.recruitment.system.dto.request.LoginRequest;
import com.recruitment.system.dto.request.RegisterRequest;
import com.recruitment.system.dto.request.VerifyEmailRequest;
import com.recruitment.system.dto.request.ResendVerificationRequest;
import com.recruitment.system.dto.request.ForgotPasswordRequest;
import com.recruitment.system.dto.request.ResetPasswordRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.AuthResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.User;
import com.recruitment.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý authentication
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RateLimitConfig rateLimitConfig;
    private final AuditLogger auditLogger;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String rateLimitKey = "register:" + clientIp;
        
        // Kiểm tra rate limit
        if (!rateLimitConfig.tryConsumeRegister(rateLimitKey)) {
            long waitTime = rateLimitConfig.getWaitTimeRegister(rateLimitKey);
            log.warn("Rate limit exceeded for register from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu đăng ký. Vui lòng thử lại sau " + waitTime + " giây."));
        }
        
        try {
            AuthResponse response = authService.register(request);
            auditLogger.logRegistrationSuccess(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), request.getRole().name());
            return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", response));
        } catch (Exception e) {
            auditLogger.logRegistrationFailure(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Thông tin đăng ký không hợp lệ"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String rateLimitKey = "login:" + clientIp;
        
        // Kiểm tra rate limit
        if (!rateLimitConfig.tryConsumeLogin(rateLimitKey)) {
            long waitTime = rateLimitConfig.getWaitTimeLogin(rateLimitKey);
            log.warn("Rate limit exceeded for login from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều lần đăng nhập sai. Vui lòng thử lại sau " + waitTime + " giây."));
        }
        
        try {
            AuthResponse response = authService.login(request);
            auditLogger.logLoginSuccess(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"));
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
        } catch (Exception e) {
            auditLogger.logLoginFailure(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Thông tin đăng nhập không chính xác"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestParam("refresh_token") String refreshToken,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String rateLimitKey = "refresh:" + clientIp;
        String userAgent = httpRequest.getHeader("User-Agent");

        // Rate limit cho refresh token
        if (!rateLimitConfig.tryConsumeRefresh(rateLimitKey)) {
            long waitTime = rateLimitConfig.getWaitTimeRefresh(rateLimitKey);
            auditLogger.logRateLimitExceeded("TOKEN_REFRESH", clientIp, userAgent);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu làm mới token. Vui lòng thử lại sau " + waitTime + " giây."));
        }
        
        try {
            AuthResponse response = authService.refresh(refreshToken);
            auditLogger.logTokenRefresh(response.getUser().getEmail(), clientIp, userAgent, true);
            return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
        } catch (Exception e) {
            auditLogger.logTokenRefresh("unknown", clientIp, userAgent, false);
            return ResponseEntity.badRequest().body(ApiResponse.error("Token không hợp lệ hoặc đã hết hạn"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        
        try {
            if (user != null) {
                authService.logout(user);
                auditLogger.logLogout(user.getEmail(), clientIp, httpRequest.getHeader("User-Agent"));
            }
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Có lỗi xảy ra khi đăng xuất"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa xác thực"));
        }
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setFullName(user.getFullName());
        resp.setPhoneNumber(user.getPhoneNumber());
        resp.setRole(user.getRole());
        resp.setStatus(user.getStatus());
        resp.setEmailVerified(user.getEmailVerified());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setLastLogin(user.getLastLogin());
        resp.setCreatedAt(user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success("Thông tin người dùng", resp));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        
        try {
            authService.verifyEmail(request);
            auditLogger.logEmailVerification(request.getToken(), clientIp, httpRequest.getHeader("User-Agent"), true);
            return ResponseEntity.ok(ApiResponse.success("Email đã được xác minh thành công", null));
        } catch (Exception e) {
            auditLogger.logEmailVerification(request.getToken(), clientIp, httpRequest.getHeader("User-Agent"), false);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String rateLimitKey = "resend-verification:" + clientIp;
        
        // Kiểm tra rate limit cho resend verification (5 requests per hour)
        if (!rateLimitConfig.tryConsumeRegister(rateLimitKey)) {
            long waitTime = rateLimitConfig.getWaitTimeRegister(rateLimitKey);
            log.warn("Rate limit exceeded for resend verification from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu gửi lại email. Vui lòng thử lại sau " + waitTime + " giây."));
        }
        
        try {
            authService.resendVerificationEmail(request);
            auditLogger.logResendVerification(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), true);
            return ResponseEntity.ok(ApiResponse.success("Email xác minh đã được gửi lại", null));
        } catch (Exception e) {
            auditLogger.logResendVerification(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), false);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String rateLimitKey = "forgot-password:" + clientIp;
        
        // Kiểm tra rate limit cho forgot password (3 requests per hour)
        if (!rateLimitConfig.tryConsumeRegister(rateLimitKey)) {
            long waitTime = rateLimitConfig.getWaitTimeRegister(rateLimitKey);
            log.warn("Rate limit exceeded for forgot password from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Quá nhiều yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau " + waitTime + " giây."));
        }
        
        try {
            authService.forgotPassword(request);
            auditLogger.logPasswordResetRequest(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), true);
            // Luôn trả về thành công để không tiết lộ thông tin về email có tồn tại hay không
            return ResponseEntity.ok(ApiResponse.success("Nếu email của bạn tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu", null));
        } catch (Exception e) {
            auditLogger.logPasswordResetRequest(request.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), false);
            // Vẫn trả về thành công để bảo mật
            return ResponseEntity.ok(ApiResponse.success("Nếu email của bạn tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu", null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        
        try {
            authService.resetPassword(request);
            auditLogger.logPasswordReset(request.getToken(), clientIp, httpRequest.getHeader("User-Agent"), true);
            return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại thành công", null));
        } catch (Exception e) {
            auditLogger.logInvalidPasswordResetToken(request.getToken(), clientIp, httpRequest.getHeader("User-Agent"), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Lấy địa chỉ IP thực của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}