package com.recruitment.system.controller;

import com.recruitment.system.entity.User;
import com.recruitment.system.repository.UserRepository;
import com.recruitment.system.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug Controller để kiểm tra authentication issues
 */
@RestController
@RequestMapping("/api/debug/auth")
@RequiredArgsConstructor
@Slf4j
public class DebugAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @GetMapping("/user/{email}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("found", true);
                result.put("email", user.getEmail());
                result.put("status", user.getStatus());
                result.put("emailVerified", user.getEmailVerified());
                result.put("role", user.getRole());
                result.put("isEnabled", user.isEnabled());
                result.put("isAccountNonExpired", user.isAccountNonExpired());
                result.put("isAccountNonLocked", user.isAccountNonLocked());
                result.put("isCredentialsNonExpired", user.isCredentialsNonExpired());
                result.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
                result.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
            } else {
                result.put("found", false);
                result.put("message", "User not found");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error getting user info: ", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(
            @RequestParam String email,
            @RequestParam String password) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("userFound", true);
                result.put("email", user.getEmail());
                
                // Test password matching
                boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
                result.put("passwordMatches", passwordMatches);
                
                // Test Spring Security checks
                result.put("isEnabled", user.isEnabled());
                result.put("isAccountNonExpired", user.isAccountNonExpired());
                result.put("isAccountNonLocked", user.isAccountNonLocked());
                result.put("isCredentialsNonExpired", user.isCredentialsNonExpired());
                
                // Overall authentication result
                boolean canAuthenticate = passwordMatches && user.isEnabled() && 
                                        user.isAccountNonExpired() && user.isAccountNonLocked() && 
                                        user.isCredentialsNonExpired();
                result.put("canAuthenticate", canAuthenticate);
                
                // Hash the provided password to compare
                String newHash = passwordEncoder.encode(password);
                result.put("newHash", newHash);
                result.put("storedHash", user.getPassword());
                
            } else {
                result.put("userFound", false);
                result.put("message", "User not found with email: " + email);
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error testing password: ", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-refresh-token/{email}")
    public ResponseEntity<Map<String, Object>> testRefreshToken(@PathVariable String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("userFound", true);
                result.put("email", user.getEmail());
                
                // Test tạo refresh token
                try {
                    String refreshToken = jwtUtil.generateRefreshToken(user);
                    result.put("refreshTokenGenerated", true);
                    result.put("refreshToken", refreshToken);
                    result.put("refreshTokenLength", refreshToken.length());
                } catch (Exception e) {
                    result.put("refreshTokenGenerated", false);
                    result.put("refreshTokenError", e.getMessage());
                }
                
            } else {
                result.put("userFound", false);
                result.put("message", "User not found");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error testing refresh token: ", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if user exists
            if (userRepository.existsByEmail(email)) {
                result.put("success", false);
                result.put("message", "User already exists");
                return ResponseEntity.ok(result);
            }
            
            // Create test user
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(com.recruitment.system.enums.UserRole.APPLICANT);
            user.setStatus(com.recruitment.system.enums.UserStatus.ACTIVE);
            user.setEmailVerified(true);
            
            user = userRepository.save(user);
            
            result.put("success", true);
            result.put("userId", user.getId());
            result.put("email", user.getEmail());
            result.put("status", user.getStatus());
            result.put("emailVerified", user.getEmailVerified());
            result.put("isEnabled", user.isEnabled());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("Error creating test user: ", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/verification-token/{email}")
    public ResponseEntity<Map<String, Object>> getVerificationToken(@PathVariable String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("userFound", true);
                result.put("email", user.getEmail());
                result.put("emailVerified", user.getEmailVerified());
                result.put("verificationToken", user.getVerificationToken());
                result.put("hasVerificationToken", user.getVerificationToken() != null);
                result.put("createdAt", user.getCreatedAt());
                
                // Check if token is expired (24 hours)
                if (user.getVerificationToken() != null && user.getCreatedAt() != null) {
                    boolean isExpired = user.getCreatedAt().plusHours(24).isBefore(java.time.LocalDateTime.now());
                    result.put("tokenExpired", isExpired);
                }
                
            } else {
                result.put("userFound", false);
                result.put("message", "User not found");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error getting verification token: ", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/email-status/{email}")
    public ResponseEntity<Map<String, Object>> getEmailStatus(@PathVariable String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                result.put("userFound", true);
                result.put("email", user.getEmail());
                result.put("emailVerified", user.getEmailVerified());
                result.put("status", user.getStatus());
                result.put("canLogin", user.getEmailVerified() && user.isEnabled());
                result.put("createdAt", user.getCreatedAt());
                result.put("lastLogin", user.getLastLogin());
                
                // Verification details
                result.put("hasVerificationToken", user.getVerificationToken() != null);
                if (user.getVerificationToken() != null) {
                    result.put("verificationToken", user.getVerificationToken());
                    boolean isExpired = user.getCreatedAt().plusHours(24).isBefore(java.time.LocalDateTime.now());
                    result.put("tokenExpired", isExpired);
                }
                
            } else {
                result.put("userFound", false);
                result.put("message", "User not found");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error getting email status: ", e);
        }
        
        return ResponseEntity.ok(result);
    }
}
