package com.recruitment.system.dto.request;

import com.recruitment.system.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho đăng ký tài khoản
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6-50 ký tự")
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ không được quá 50 ký tự")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự")
    private String lastName;

    @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
    private String phoneNumber;

    @NotNull(message = "Vai trò không được để trống")
    private UserRole role;

    // Thông tin công ty (chỉ cho employer/recruiter)
    private String companyName;
    private String companyDescription;
    private String companyWebsite;
    private String companyIndustry;
    private String companyAddress;
}