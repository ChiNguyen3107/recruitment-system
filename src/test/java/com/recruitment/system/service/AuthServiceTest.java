package com.recruitment.system.service;

import com.recruitment.system.dto.request.ResendVerificationRequest;
import com.recruitment.system.dto.request.VerifyEmailRequest;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.CompanyRepository;
import com.recruitment.system.repository.UserRepository;
import com.recruitment.system.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // Set cấu hình qua reflection
        var requireField = AuthService.class.getDeclaredField("requireEmailVerifiedToLogin");
        requireField.setAccessible(true);
        requireField.set(authService, true);

        var expField = AuthService.class.getDeclaredField("verificationExpirationHours");
        expField.setAccessible(true);
        expField.set(authService, 24);
    }

    @Test
    void verifyEmail_happyPath() {
        String token = "11111111-1111-1111-1111-111111111111";
        User user = new User();
        user.setVerificationToken(token);
        user.setVerificationTokenIssuedAt(LocalDateTime.now().minusHours(1));
        user.setEmailVerified(false);

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setToken(token);

        assertDoesNotThrow(() -> authService.verifyEmail(req));
        assertTrue(user.getEmailVerified());
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenIssuedAt());
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(mailService, times(1)).sendVerificationSuccessEmail(any(User.class));
    }

    @Test
    void verifyEmail_invalidToken() {
        String token = "00000000-0000-0000-0000-000000000000";
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setToken(token);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verifyEmail(req));
        assertTrue(ex.getMessage().toLowerCase().contains("không hợp lệ") || ex.getMessage().toLowerCase().contains("hết hạn"));
        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendVerificationSuccessEmail(any());
    }

    @Test
    void verifyEmail_expiredToken() {
        String token = "22222222-2222-2222-2222-222222222222";
        User user = new User();
        user.setVerificationToken(token);
        user.setVerificationTokenIssuedAt(LocalDateTime.now().minusHours(48));
        user.setEmailVerified(false);

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setToken(token);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verifyEmail(req));
        assertTrue(ex.getMessage().toLowerCase().contains("hết hạn"));
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenIssuedAt());
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(mailService, never()).sendVerificationSuccessEmail(any());
    }

    @Test
    void resendVerification_masksExistence() {
        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("doesnotexist@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.resendVerificationEmail(req));
        verify(mailService, never()).sendVerificationEmail(any(), anyString());
    }
}


