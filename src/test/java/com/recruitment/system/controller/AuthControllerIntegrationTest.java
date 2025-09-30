package com.recruitment.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.config.RateLimitConfig;
import com.recruitment.system.dto.request.ForgotPasswordRequest;
import com.recruitment.system.dto.request.ResetPasswordRequest;
import com.recruitment.system.dto.request.ResendVerificationRequest;
import com.recruitment.system.dto.request.VerifyEmailRequest;
import com.recruitment.system.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimitConfig rateLimitConfig;

    @MockBean
    private AuditLogger auditLogger;

    @BeforeEach
    void setup() {
        when(rateLimitConfig.tryConsumeRegister(anyString())).thenReturn(true);
        when(rateLimitConfig.tryConsumeLogin(anyString())).thenReturn(true);
        when(rateLimitConfig.tryConsumeRefresh(anyString())).thenReturn(true);
    }

    @Test
    void verifyEmail_happy() throws Exception {
        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setToken("123e4567-e89b-12d3-a456-426614174000");

        // authService.verifyEmail không ném lỗi => happy
        Mockito.doNothing().when(authService).verifyEmail(any());

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void verifyEmail_invalidToken() throws Exception {
        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setToken("00000000-0000-0000-0000-000000000000");

        doThrow(new RuntimeException("Token xác minh không hợp lệ hoặc đã hết hạn"))
            .when(authService).verifyEmail(any());

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resendVerification_genericResponse() throws Exception {
        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("someone@example.com");

        Mockito.doNothing().when(authService).resendVerificationEmail(any());

        mockMvc.perform(post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Nếu email tồn tại")));
    }

    @Test
    void resendVerification_rateLimited() throws Exception {
        when(rateLimitConfig.tryConsumeRegister(anyString())).thenReturn(false);
        when(rateLimitConfig.getWaitTimeRegister(anyString())).thenReturn(60L);

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("someone@example.com");

        mockMvc.perform(post("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void forgotPassword_genericSuccess_evenWhenServiceThrows() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("someone@example.com");

        // Mặc định rate limit OK
        when(rateLimitConfig.tryConsumeRegister(anyString())).thenReturn(true);
        // Service ném lỗi nhưng controller vẫn trả về OK để tránh lộ thông tin
        doThrow(new RuntimeException("some error")).when(authService).forgotPassword(any());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Nếu email của bạn tồn tại")));
    }

    @Test
    void forgotPassword_rateLimited() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("someone@example.com");

        when(rateLimitConfig.tryConsumeRegister(anyString())).thenReturn(false);
        when(rateLimitConfig.getWaitTimeRegister(anyString())).thenReturn(120L);

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_happy() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("good");
        req.setNewPassword("Valid@1234");

        Mockito.doNothing().when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_invalidToken_returnsBadRequest() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("bad");
        req.setNewPassword("Valid@1234");

        doThrow(new RuntimeException("Token không hợp lệ hoặc đã hết hạn")).when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}


