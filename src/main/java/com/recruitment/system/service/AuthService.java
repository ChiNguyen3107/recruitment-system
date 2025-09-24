package com.recruitment.system.service;

import com.recruitment.system.config.JwtUtil;
import com.recruitment.system.dto.request.LoginRequest;
import com.recruitment.system.dto.request.RegisterRequest;
import com.recruitment.system.dto.request.VerifyEmailRequest;
import com.recruitment.system.dto.request.ResendVerificationRequest;
import com.recruitment.system.dto.request.ForgotPasswordRequest;
import com.recruitment.system.dto.request.ResetPasswordRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MailService mailService;
    
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
        user.setEmailVerified(false);       // Cần xác minh email
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

        // Gửi email xác minh
        try {
            mailService.sendVerificationEmail(user, user.getVerificationToken());
        } catch (Exception e) {
            log.error("Failed to send verification email during registration", e);
            // Không throw exception để không làm gián đoạn quá trình đăng ký
        }

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

        // Kiểm tra email đã được xác minh chưa
        if (!user.getEmailVerified()) {
            throw new RuntimeException("Vui lòng xác minh email trước khi đăng nhập. Kiểm tra hộp thư của bạn hoặc yêu cầu gửi lại email xác minh.");
        }

        // Cập nhật thời gian đăng nhập cuối
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

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

    /**
     * Xác minh email với token
     */
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token xác minh không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra token đã hết hạn chưa (24 giờ)
        if (user.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            // Xóa token hết hạn
            user.setVerificationToken(null);
            userRepository.save(user);
            throw new RuntimeException("Token xác minh đã hết hạn. Vui lòng yêu cầu gửi lại email xác minh.");
        }

        // Xác minh thành công
        user.setEmailVerified(true);
        user.setVerificationToken(null); // Xóa token sau khi xác minh
        userRepository.save(user);

        // Gửi email thông báo xác minh thành công
        try {
            mailService.sendVerificationSuccessEmail(user);
        } catch (Exception e) {
            log.error("Failed to send verification success email", e);
            // Không throw exception vì xác minh đã thành công
        }

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    /**
     * Gửi lại email xác minh
     */
    @Transactional
    public void resendVerificationEmail(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        // Kiểm tra email đã được xác minh chưa
        if (user.getEmailVerified()) {
            throw new RuntimeException("Email này đã được xác minh");
        }

        // Tạo token mới
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        // Gửi email xác minh mới
        try {
            mailService.sendVerificationEmail(user, newToken);
            log.info("Verification email resent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Không thể gửi email xác minh. Vui lòng thử lại sau.");
        }
    }

    /**
     * Xử lý yêu cầu quên mật khẩu
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Không tiết lộ thông tin về việc email có tồn tại hay không
        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        // Kiểm tra user có đang hoạt động không
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.info("Password reset requested for inactive user: {}", request.getEmail());
            return;
        }

        // Tạo token reset password với thời hạn 1 giờ
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        // Lưu token vào database
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(expiresAt);
        userRepository.save(user);

        // Gửi email reset password
        try {
            mailService.sendPasswordResetEmail(user, resetToken);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // Xóa token nếu gửi email thất bại
            user.setPasswordResetToken(null);
            user.setPasswordResetExpires(null);
            userRepository.save(user);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
        }
    }

    /**
     * Xử lý reset password với token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Tìm user với token hợp lệ và chưa hết hạn
        User user = userRepository.findByValidPasswordResetToken(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra user có đang hoạt động không
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản không thể đặt lại mật khẩu");
        }

        // Mã hóa mật khẩu mới
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        // Cập nhật mật khẩu và xóa token
        user.setPassword(hashedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
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