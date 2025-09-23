package com.recruitment.system.service;

import com.recruitment.system.config.JwtUtil;
import com.recruitment.system.dto.request.LoginRequest;
import com.recruitment.system.dto.request.RegisterRequest;
import com.recruitment.system.dto.response.AuthResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import com.recruitment.system.repository.CompanyRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service xử lý authentication và authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    
    @org.springframework.beans.factory.annotation.Value("${jwt.access.expiration:900000}")
    private long accessExpirationMs;
    
    @org.springframework.beans.factory.annotation.Value("${jwt.refresh.expiration:2592000000}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Tạo user mới
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);  // Cho phép đăng nhập ngay sau khi đăng ký
        user.setEmailVerified(true);        // Tạm thời set true để test
        user.setVerificationToken(UUID.randomUUID().toString());

        // Nếu là employer/recruiter, tạo company
        if (request.getRole() == UserRole.EMPLOYER || request.getRole() == UserRole.RECRUITER) {
            if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
                Company company = new Company();
                company.setName(request.getCompanyName());
                company.setDescription(request.getCompanyDescription());
                company.setWebsite(request.getCompanyWebsite());
                company.setIndustry(request.getCompanyIndustry());
                company.setAddress(request.getCompanyAddress());
                company = companyRepository.save(company);
                user.setCompany(company);
            }
        }

        // Nếu là applicant, tạo profile trống
        if (request.getRole() == UserRole.APPLICANT) {
            Profile profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        user = userRepository.save(user);

        // Tạo token truy cập và làm mới
        String accessToken = jwtUtil.generateAccessToken(user);
        var refresh = refreshTokenService.issueRefreshToken(user);

        // Convert to response
        UserResponse userResponse = convertToUserResponse(user);

        return AuthResponse.of(accessToken, refresh.getToken(), accessExpirationMs, refreshExpirationMs, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user);
        
        // Try to create refresh token, if fails, use access token as fallback
        String refreshToken;
        try {
            var refresh = refreshTokenService.issueRefreshToken(user);
            refreshToken = refresh.getToken();
        } catch (Exception e) {
            log.warn("Failed to create refresh token, using access token as fallback: {}", e.getMessage());
            refreshToken = accessToken; // Fallback
        }
        
        UserResponse userResponse = convertToUserResponse(user);

        return AuthResponse.of(accessToken, refreshToken, accessExpirationMs, refreshExpirationMs, userResponse);
    }

    public AuthResponse refresh(String refreshToken) {
        var stored = refreshTokenService.validateActiveToken(refreshToken);
        var user = stored.getUser();
        String newAccess = jwtUtil.generateAccessToken(user);
        // rotate refresh token
        var newRefresh = refreshTokenService.issueRefreshToken(user);
        refreshTokenService.revokeToken(stored, newRefresh.getToken());
        UserResponse userResponse = convertToUserResponse(user);
        return AuthResponse.of(newAccess, newRefresh.getToken(), accessExpirationMs, refreshExpirationMs, userResponse);
    }

    public void logout(User user) {
        refreshTokenService.revokeAll(user);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setEmailVerified(user.getEmailVerified());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}