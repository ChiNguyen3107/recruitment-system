package com.recruitment.system.dto.response;

import com.recruitment.system.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho phản hồi hồ sơ cá nhân
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Profile entity sang ProfileResponse
     */
    public static ProfileResponse fromProfile(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .summary(profile.getSummary())
                .experience(profile.getExperience())
                .education(profile.getEducation())
                .skills(profile.getSkills())
                .certifications(profile.getCertifications())
                .languages(profile.getLanguages())
                .resumeUrl(profile.getResumeUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .portfolioUrl(profile.getPortfolioUrl())
                .desiredSalaryMin(profile.getDesiredSalaryMin())
                .desiredSalaryMax(profile.getDesiredSalaryMax())
                .desiredJobType(profile.getDesiredJobType())
                .desiredLocation(profile.getDesiredLocation())
                .availability(profile.getAvailability())
                .isPublic(profile.getIsPublic())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}