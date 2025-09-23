package com.recruitment.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho request xác minh email
 */
@Data
public class VerifyEmailRequest {

    @NotBlank(message = "Token xác minh không được để trống")
    @Size(min = 36, max = 36, message = "Token xác minh phải có độ dài 36 ký tự")
    private String token;
}
