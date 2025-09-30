package com.recruitment.system.service;

import com.recruitment.system.dto.request.ResendVerificationRequest;
import com.recruitment.system.dto.request.ForgotPasswordRequest;
import com.recruitment.system.dto.request.ResetPasswordRequest;
import com.recruitment.system.dto.request.VerifyEmailRequest;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserStatus;
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

    @Test
    void forgotPassword_masksWhenEmailNotExist() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("noone@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.forgotPassword(req));
        verify(mailService, never()).sendPasswordResetEmail(any(), anyString());
    }

    @Test
    void forgotPassword_inactiveUser_isSilentlyIgnored() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("inactive@example.com");

        User user = new User();
        user.setEmail("inactive@example.com");
        user.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findByEmail(eq("inactive@example.com"))).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.forgotPassword(req));
        verify(mailService, never()).sendPasswordResetEmail(any(), anyString());
    }

    @Test
    void forgotPassword_happyPath_setsToken_and_SendsEmail() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("user@example.com");

        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail(eq("user@example.com"))).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authService.forgotPassword(req));
        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetExpires());
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(mailService, times(1)).sendPasswordResetEmail(eq(user), anyString());
    }

    @Test
    void forgotPassword_mailSendingFails_rollsBackToken_andThrows() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("user2@example.com");

        User user = new User();
        user.setEmail("user2@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail(eq("user2@example.com"))).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("send fail")).when(mailService).sendPasswordResetEmail(any(User.class), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.forgotPassword(req));
        assertTrue(ex.getMessage().toLowerCase().contains("không thể gửi"));
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetExpires());
        verify(userRepository, atLeast(2)).save(any(User.class));
    }

    @Test
    void resetPassword_invalidToken_throws() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("badtoken");
        req.setNewPassword("Valid@1234");

        when(userRepository.findByValidPasswordResetToken(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.resetPassword(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_happy_updatesPassword_and_ClearsToken() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("goodtoken");
        req.setNewPassword("Valid@1234");

        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByValidPasswordResetToken(eq("goodtoken"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(eq("Valid@1234"))).thenReturn("hashed");

        assertDoesNotThrow(() -> authService.resetPassword(req));
        assertEquals("hashed", user.getPassword());
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetExpires());
        verify(userRepository, times(1)).save(any(User.class));
    }
}


