package com.recruitment.system.controller;

import com.recruitment.system.dto.request.LoginRequest;
import com.recruitment.system.dto.request.RegisterRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.AuthResponse;
import com.recruitment.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý authentication
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
        } catch (Exception e) {
            // Temporary debug: show actual error message
            return ResponseEntity.badRequest().body(ApiResponse.error("DEBUG: " + e.getMessage() + " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null")));
        }
    }
}