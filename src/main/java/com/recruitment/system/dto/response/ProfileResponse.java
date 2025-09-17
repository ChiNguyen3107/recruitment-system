package com.recruitment.system.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO response cho hồ sơ cá nhân
 */
@Data
public class ProfileResponse {

    private Long id;
    private Long userId;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String country;
    private String fullAddress;
    private String summary;
    private String experience;
    private String education;
    private String skills;
    private String certifications;
    private String languages;
    private String resumeUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private Long desiredSalaryMin;
    private Long desiredSalaryMax;
    private String desiredJobType;
    private String desiredLocation;
    private String availability;
    private Boolean isPublic;
    private Boolean isComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin user
    private String userFullName;
    private String userEmail;
    private String userPhone;
}