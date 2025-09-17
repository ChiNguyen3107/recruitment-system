package com.recruitment.system.service;

import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.dto.response.ProfileResponse;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.ProfileRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service xử lý quản lý hồ sơ người dùng
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    public ProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = user.getProfile();
        if (profile == null) {
            // Tạo profile mới nếu chưa có
            profile = createEmptyProfile(user);
        }
        
        return convertToProfileResponse(profile);
    }

    /**
     * Cập nhật thông tin profile
     */
    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = createEmptyProfile(user);
        }

        // Cập nhật thông tin profile
        updateProfileFromRequest(profile, request);
        profile.setUpdatedAt(LocalDateTime.now());
        
        profile = profileRepository.save(profile);
        return convertToProfileResponse(profile);
    }

    /**
     * Xóa profile (đặt về null các field)
     */
    @Transactional
    public void deleteProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = user.getProfile();
        if (profile != null) {
            profileRepository.delete(profile);
            user.setProfile(null);
            userRepository.save(user);
        }
    }

    /**
     * Lấy profile của user khác (public view)
     */
    public ProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = user.getProfile();
        if (profile == null || !profile.getIsPublic()) {
            throw new RuntimeException("Profile not found or not public");
        }
        
        return convertToProfileResponse(profile);
    }

    /**
     * Tạo profile trống cho user
     */
    private Profile createEmptyProfile(User user) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setIsPublic(false);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        profile = profileRepository.save(profile);
        user.setProfile(profile);
        userRepository.save(user);
        
        return profile;
    }

    /**
     * Cập nhật profile từ request
     */
    private void updateProfileFromRequest(Profile profile, ProfileRequest request) {
        if (request.getSummary() != null) {
            profile.setSummary(request.getSummary());
        }
        if (request.getExperience() != null) {
            profile.setExperience(request.getExperience());
        }
        if (request.getEducation() != null) {
            profile.setEducation(request.getEducation());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        if (request.getCertifications() != null) {
            profile.setCertifications(request.getCertifications());
        }
        if (request.getLanguages() != null) {
            profile.setLanguages(request.getLanguages());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            profile.setGithubUrl(request.getGithubUrl());
        }
        if (request.getPortfolioUrl() != null) {
            profile.setPortfolioUrl(request.getPortfolioUrl());
        }
        if (request.getDesiredJobType() != null) {
            profile.setDesiredJobType(request.getDesiredJobType());
        }
        if (request.getDesiredLocation() != null) {
            profile.setDesiredLocation(request.getDesiredLocation());
        }
        if (request.getDesiredSalaryMin() != null) {
            profile.setDesiredSalaryMin(request.getDesiredSalaryMin());
        }
        if (request.getDesiredSalaryMax() != null) {
            profile.setDesiredSalaryMax(request.getDesiredSalaryMax());
        }
        if (request.getAvailability() != null) {
            profile.setAvailability(request.getAvailability());
        }
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }
    }

    /**
     * Convert Profile entity to ProfileResponse
     */
    private ProfileResponse convertToProfileResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setSummary(profile.getSummary());
        response.setExperience(profile.getExperience());
        response.setEducation(profile.getEducation());
        response.setSkills(profile.getSkills());
        response.setCertifications(profile.getCertifications());
        response.setLanguages(profile.getLanguages());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setGender(profile.getGender());
        response.setAddress(profile.getAddress());
        response.setCity(profile.getCity());
        response.setCountry(profile.getCountry());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setGithubUrl(profile.getGithubUrl());
        response.setPortfolioUrl(profile.getPortfolioUrl());
        response.setResumeUrl(profile.getResumeUrl());
        response.setDesiredJobType(profile.getDesiredJobType());
        response.setDesiredLocation(profile.getDesiredLocation());
        response.setDesiredSalaryMin(profile.getDesiredSalaryMin());
        response.setDesiredSalaryMax(profile.getDesiredSalaryMax());
        response.setAvailability(profile.getAvailability());
        response.setIsPublic(profile.getIsPublic());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        
        // Thêm thông tin user
        User user = profile.getUser();
        response.setUserFullName(user.getFirstName() + " " + user.getLastName());
        response.setUserEmail(user.getEmail());
        response.setUserPhone(user.getPhoneNumber());
        
        return response;
    }
}