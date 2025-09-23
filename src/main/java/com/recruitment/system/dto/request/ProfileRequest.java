package com.recruitment.system.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO cho cập nhật hồ sơ cá nhân
 */
@Data
public class ProfileRequest {

    private LocalDate dateOfBirth;

    private String gender;

    private String address;

    private String city;

    private String country;

    private String summary;

    private String experience;

    private String education;

    private String skills;

    private String certifications;

    private String languages;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private Long desiredSalaryMin;

    private Long desiredSalaryMax;

    private String desiredJobType;

    private String desiredLocation;

    private String availability;

    private Boolean isPublic = false;
}