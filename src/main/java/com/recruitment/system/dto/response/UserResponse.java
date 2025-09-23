package com.recruitment.system.dto.response;

import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO response cho thông tin người dùng
 */
@Data
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private String avatarUrl;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    // Thông tin công ty (nếu có)
    private CompanyResponse company;
    
    // Thông tin hồ sơ (cho applicant)
    private ProfileResponse profile;
}