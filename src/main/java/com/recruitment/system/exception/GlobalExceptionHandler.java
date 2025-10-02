package com.recruitment.system.exception;

import com.recruitment.system.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// import org.springframework.web.context.request.WebRequest;

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
                .body(ApiResponse.error(
                        "VALIDATION_ERROR",
                        "Thông tin đầu vào không hợp lệ",
                        "Request body không đạt ràng buộc",
                        errors,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý authentication errors
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        
        log.warn("Authentication failed from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "UNAUTHORIZED",
                        "Thông tin xác thực không chính xác",
                        "Sai thông tin đăng nhập hoặc token",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý authorization errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        "FORBIDDEN",
                        "Không có quyền truy cập tài nguyên này",
                        "Thiếu quyền hoặc phạm vi",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý rate limit errors
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        
        log.warn("Rate limit exceeded for {} from IP: {}", ex.getOperation(), getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(
                        "RATE_LIMIT_EXCEEDED",
                        ex.getMessage(),
                        "Vượt quá giới hạn tần suất",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod(),
                                "operation", ex.getOperation()
                        )
                ));
    }

    /**
     * Xử lý JWT token errors
     */
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(
            io.jsonwebtoken.ExpiredJwtException ex, HttpServletRequest request) {
        
        log.warn("Expired JWT token from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "TOKEN_EXPIRED",
                        "Token đã hết hạn. Vui lòng đăng nhập lại.",
                        "JWT expired",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    @ExceptionHandler(io.jsonwebtoken.MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJwtException(
            io.jsonwebtoken.MalformedJwtException ex, HttpServletRequest request) {
        
        log.warn("Malformed JWT token from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "TOKEN_MALFORMED",
                        "Token không hợp lệ.",
                        "JWT malformed",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý lỗi vi phạm ràng buộc dữ liệu (DB constraints)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String rootMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("Data integrity violation from IP: {} - {}", getClientIp(request), rootMessage);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(
                        "DATA_INTEGRITY_VIOLATION",
                        "Dữ liệu không hợp lệ",
                        "Vi phạm ràng buộc dữ liệu",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    @ExceptionHandler(io.jsonwebtoken.SignatureException.class)
    public ResponseEntity<ApiResponse<Void>> handleSignatureException(
            io.jsonwebtoken.SignatureException ex, HttpServletRequest request) {
        
        log.warn("Invalid JWT signature from IP: {}", getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        "TOKEN_SIGNATURE_INVALID",
                        "Token không hợp lệ.",
                        "JWT signature invalid",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("Runtime error from IP: {} - Message: {}", getClientIp(request), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "INTERNAL_SERVER_ERROR",
                        "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                        "Runtime exception",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
    }

    /**
     * Xử lý tất cả các exception khác
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error from IP: {} - Message: {}", getClientIp(request), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "UNEXPECTED_ERROR",
                        "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.",
                        "Unhandled exception",
                        null,
                        Map.of(
                                "path", request.getRequestURI(),
                                "method", request.getMethod()
                        )
                ));
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








