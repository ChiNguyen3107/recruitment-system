package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ProfileResponse;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.ProfileRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller quản lý hồ sơ ứng viên
 */
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileRepository profileRepository;

    /**
     * Lấy hồ sơ của người dùng hiện tại
     * GET /api/profiles/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Chưa xác thực người dùng"));
        }

        // Chỉ cho phép APPLICANT truy cập
        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Chỉ ứng viên mới có thể truy cập hồ sơ"));
        }

        try {
            Optional<Profile> profileOpt = profileRepository.findByUserId(user.getId());
            
            if (profileOpt.isEmpty()) {
                // Tạo hồ sơ mới nếu chưa tồn tại
                Profile newProfile = new Profile();
                newProfile.setUser(user);
                newProfile.setIsPublic(false);
                Profile savedProfile = profileRepository.save(newProfile);
                log.info("Tạo hồ sơ mới cho user ID: {}", user.getId());
                
                ProfileResponse response = ProfileResponse.fromProfile(savedProfile);
                return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ thành công", response));
            }

            Profile profile = profileOpt.get();
            ProfileResponse response = ProfileResponse.fromProfile(profile);
            log.info("Lấy hồ sơ thành công cho user ID: {}", user.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ thành công", response));
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy hồ sơ cho user ID: {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi lấy hồ sơ"));
        }
    }

    /**
     * Cập nhật hồ sơ của người dùng hiện tại
     * PUT /api/profiles/my
     */
    @PutMapping("/my")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileRequest request) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Chưa xác thực người dùng"));
        }

        // Chỉ cho phép APPLICANT cập nhật
        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Chỉ ứng viên mới có thể cập nhật hồ sơ"));
        }

        try {
            Optional<Profile> profileOpt = profileRepository.findByUserId(user.getId());
            Profile profile;
            
            if (profileOpt.isEmpty()) {
                // Tạo hồ sơ mới nếu chưa tồn tại
                profile = new Profile();
                profile.setUser(user);
                log.info("Tạo hồ sơ mới cho user ID: {}", user.getId());
            } else {
                profile = profileOpt.get();
                log.info("Cập nhật hồ sơ cho user ID: {}", user.getId());
            }

            // Cập nhật các trường từ request
            updateProfileFromRequest(profile, request);
            
            Profile savedProfile = profileRepository.save(profile);
            ProfileResponse response = ProfileResponse.fromProfile(savedProfile);
            
            log.info("Cập nhật hồ sơ thành công cho user ID: {}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật hồ sơ cho user ID: {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi cập nhật hồ sơ"));
        }
    }

    /**
     * Cập nhật profile từ request, bảo toàn resumeUrl nếu không được cung cấp
     */
    private void updateProfileFromRequest(Profile profile, ProfileRequest request) {
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
        
        // Bảo toàn resumeUrl - chỉ cập nhật khi được cung cấp trong request
        // (Trong tương lai có thể thêm logic upload file để cập nhật resumeUrl)
        
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(request.getLinkedinUrl());
        }
        
        if (request.getGithubUrl() != null) {
            profile.setGithubUrl(request.getGithubUrl());
        }
        
        if (request.getPortfolioUrl() != null) {
            profile.setPortfolioUrl(request.getPortfolioUrl());
        }
        
        if (request.getDesiredSalaryMin() != null) {
            profile.setDesiredSalaryMin(request.getDesiredSalaryMin());
        }
        
        if (request.getDesiredSalaryMax() != null) {
            profile.setDesiredSalaryMax(request.getDesiredSalaryMax());
        }
        
        if (request.getDesiredJobType() != null) {
            profile.setDesiredJobType(request.getDesiredJobType());
        }
        
        if (request.getDesiredLocation() != null) {
            profile.setDesiredLocation(request.getDesiredLocation());
        }
        
        if (request.getAvailability() != null) {
            profile.setAvailability(request.getAvailability());
        }
        
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }
    }
}
