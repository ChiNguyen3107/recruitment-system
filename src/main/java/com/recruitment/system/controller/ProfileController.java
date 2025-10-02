package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ProfileResponse;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.ProfileRepository;
import com.recruitment.system.config.AuditLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

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
    private final com.recruitment.system.service.StorageService storageService;
    private final AuditLogger auditLogger;

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
     * Upload CV (PDF <= 5MB) cho hồ sơ của người dùng hiện tại
     * POST /api/profiles/my/resume
     */
    @PostMapping(value = "/my/resume", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadMyResume(
            @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest httpRequest) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Chưa xác thực người dùng"));
        }

        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Chỉ ứng viên mới có thể tải lên CV"));
        }

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            // Validate kích thước: <= 5MB
            long maxSize = 5L * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Kích thước file vượt quá 5MB"));
            }

            // Validate MIME: application/pdf
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ chấp nhận file PDF (application/pdf)"));
            }

            // Kiểm tra phần mở rộng file .pdf (nếu client gửi tên)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                String lower = originalFilename.toLowerCase();
                if (!lower.endsWith(".pdf")) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Chỉ cho phép tệp có đuôi .pdf"));
                }
            }

            // Kiểm tra magic number PDF: bytes đầu phải là "%PDF-"
            byte[] head = file.getInputStream().readNBytes(5);
            String signature = new String(head);
            if (!"%PDF-".equals(signature)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Nội dung tệp không phải PDF hợp lệ"));
            }

            // Tạo tên file an toàn: resume-{timestamp}.pdf
            String safeName = "resume-" + System.currentTimeMillis() + ".pdf";
            String directory = "resumes/" + user.getId();

            // Lưu file qua StorageService, trả về relative url bắt đầu bằng /uploads
            String resumeUrl = storageService.save(file, directory, safeName);

            // Lấy hoặc tạo profile
            Profile profile = profileRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Profile p = new Profile();
                        p.setUser(user);
                        p.setIsPublic(false);
                        return p;
                    });

            profile.setResumeUrl(resumeUrl);
            Profile saved = profileRepository.save(profile);

            ProfileResponse response = ProfileResponse.fromProfile(saved);

            // Audit
            String clientIp = getClientIpAddress(httpRequest);
            auditLogger.logResumeUploaded(user.getId(), user.getEmail(), resumeUrl, clientIp, httpRequest.getHeader("User-Agent"), true);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tải lên CV thành công", response));

        } catch (Exception e) {
            log.error("Lỗi khi upload CV cho user {}", user.getId(), e);
            // Audit failure (không log chi tiết lỗi)
            String clientIp = httpRequest != null ? getClientIpAddress(httpRequest) : "unknown";
            auditLogger.logResumeUploaded(user != null ? user.getId() : null, user != null ? user.getEmail() : "unknown", null, clientIp, httpRequest != null ? httpRequest.getHeader("User-Agent") : "unknown", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi tải lên CV"));
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

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
