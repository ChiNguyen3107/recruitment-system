package com.recruitment.system.exception;

import com.recruitment.system.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler để xử lý các lỗi một cách thống nhất và bảo mật
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error from IP: {} - Fields: {}", getClientIp(request), errors.keySet());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Thông tin đầu vào không hợp lệ", errors));
    }

    /**
     * Xử lý authentication errors
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        
        log.warn("Authentication failed from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Thông tin xác thực không chính xác"));
    }

    /**
     * Xử lý authorization errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Không có quyền truy cập tài nguyên này"));
    }

    /**
     * Xử lý rate limit errors
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        
        log.warn("Rate limit exceeded for {} from IP: {}", ex.getOperation(), getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Xử lý JWT token errors
     */
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(
            io.jsonwebtoken.ExpiredJwtException ex, HttpServletRequest request) {
        
        log.warn("Expired JWT token from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token đã hết hạn. Vui lòng đăng nhập lại."));
    }

    @ExceptionHandler(io.jsonwebtoken.MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJwtException(
            io.jsonwebtoken.MalformedJwtException ex, HttpServletRequest request) {
        
        log.warn("Malformed JWT token from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token không hợp lệ."));
    }

    @ExceptionHandler(io.jsonwebtoken.SignatureException.class)
    public ResponseEntity<ApiResponse<Void>> handleSignatureException(
            io.jsonwebtoken.SignatureException ex, HttpServletRequest request) {
        
        log.warn("Invalid JWT signature from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token không hợp lệ."));
    }

    /**
     * Xử lý runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("Runtime error from IP: {} - Message: {}", getClientIp(request), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
    }

    /**
     * Xử lý tất cả các exception khác
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error from IP: {} - Message: {}", getClientIp(request), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
    }

    /**
     * Lấy địa chỉ IP thực của client
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}








