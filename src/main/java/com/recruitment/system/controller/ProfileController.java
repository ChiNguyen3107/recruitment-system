package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ProfileResponse;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.ProfileDocument;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.ProfileDocumentType;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.ProfileRepository;
import com.recruitment.system.repository.ProfileDocumentRepository;
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
    private final ProfileDocumentRepository profileDocumentRepository;

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
     * Upload tài liệu hồ sơ đa định dạng
     * POST /api/profiles/my/documents
     */
    @PostMapping(value = "/my/documents", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Object>> uploadDocument(
            @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentType") ProfileDocumentType documentType,
            HttpServletRequest httpRequest) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Chưa xác thực người dùng"));
        }
        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Chỉ ứng viên mới có thể tải lên tài liệu"));
        }
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File không được để trống"));
            }

            // Validate theo loại & magic number
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String lowerName = originalFilename.toLowerCase();
            long size = file.getSize();

            switch (documentType) {
                case RESUME:
                case COVER_LETTER: {
                    long max = 5L * 1024 * 1024;
                    if (size > max) return ResponseEntity.badRequest().body(ApiResponse.error("Kích thước tối đa 5MB"));
                    if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ chấp nhận PDF/DOC/DOCX"));
                    }
                    if (lowerName.endsWith(".pdf") && !isPdf(file)) return ResponseEntity.badRequest().body(ApiResponse.error("PDF không hợp lệ"));
                    if ((lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) && !isMsOffice(file)) return ResponseEntity.badRequest().body(ApiResponse.error("DOC/DOCX không hợp lệ"));
                    break;
                }
                case PORTFOLIO: {
                    long max = 10L * 1024 * 1024;
                    if (size > max) return ResponseEntity.badRequest().body(ApiResponse.error("Kích thước tối đa 10MB"));
                    if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png"))) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ chấp nhận JPG/PNG/PDF"));
                    }
                    if (lowerName.endsWith(".pdf") && !isPdf(file)) return ResponseEntity.badRequest().body(ApiResponse.error("PDF không hợp lệ"));
                    if ((lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) && !isJpeg(file)) return ResponseEntity.badRequest().body(ApiResponse.error("JPG không hợp lệ"));
                    if (lowerName.endsWith(".png") && !isPng(file)) return ResponseEntity.badRequest().body(ApiResponse.error("PNG không hợp lệ"));
                    break;
                }
                case CERTIFICATE: {
                    long max = 3L * 1024 * 1024;
                    if (size > max) return ResponseEntity.badRequest().body(ApiResponse.error("Kích thước tối đa 3MB"));
                    if (!(lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png"))) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ chấp nhận PDF/JPG/PNG"));
                    }
                    if (lowerName.endsWith(".pdf") && !isPdf(file)) return ResponseEntity.badRequest().body(ApiResponse.error("PDF không hợp lệ"));
                    if ((lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) && !isJpeg(file)) return ResponseEntity.badRequest().body(ApiResponse.error("JPG không hợp lệ"));
                    if (lowerName.endsWith(".png") && !isPng(file)) return ResponseEntity.badRequest().body(ApiResponse.error("PNG không hợp lệ"));
                    break;
                }
            }

            String extension = lowerName.contains(".") ? lowerName.substring(lowerName.lastIndexOf('.')) : "";
            String safeName = documentType.name().toLowerCase() + "-" + System.currentTimeMillis() + extension;
            String directory = "documents/" + user.getId() + "/" + documentType.name().toLowerCase();
            String path = storageService.save(file, directory, safeName);

            Profile profile = profileRepository.findByUserId(user.getId()).orElseGet(() -> {
                Profile p = new Profile();
                p.setUser(user);
                p.setIsPublic(false);
                return profileRepository.save(p);
            });

            ProfileDocument doc = new ProfileDocument();
            doc.setProfile(profile);
            doc.setType(documentType);
            doc.setFileName(safeName);
            doc.setFileExtension(extension);
            doc.setFileSize(size);
            doc.setPath(path);
            profileDocumentRepository.save(doc);

            String clientIp = getClientIpAddress(httpRequest);
            auditLogger.logProfileDocumentUploaded(user.getId(), user.getEmail(), documentType.name(), path, clientIp, httpRequest.getHeader("User-Agent"), true);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tải lên tài liệu thành công", null));
        } catch (Exception e) {
            String clientIp = httpRequest != null ? getClientIpAddress(httpRequest) : "unknown";
            auditLogger.logProfileDocumentUploaded(user != null ? user.getId() : null, user != null ? user.getEmail() : "unknown", documentType != null ? documentType.name() : "unknown", null, clientIp, httpRequest != null ? httpRequest.getHeader("User-Agent") : "unknown", false);
            log.error("Lỗi upload tài liệu", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Có lỗi xảy ra khi tải lên"));
        }
    }

    /**
     * Danh sách tài liệu của tôi, group theo loại
     */
    @GetMapping("/my/documents")
    public ResponseEntity<ApiResponse<Object>> listMyDocuments(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực người dùng"));
        }
        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Chỉ ứng viên mới có thể truy cập"));
        }
        Optional<Profile> profileOpt = profileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Danh sách tài liệu", java.util.Map.of()));
        }
        Long profileId = profileOpt.get().getId();
        java.util.List<ProfileDocument> docs = profileDocumentRepository.findByProfileId(profileId);
        java.util.Map<ProfileDocumentType, java.util.List<java.util.Map<String, Object>>> grouped = new java.util.EnumMap<>(ProfileDocumentType.class);
        for (ProfileDocumentType t : ProfileDocumentType.values()) {
            grouped.put(t, new java.util.ArrayList<>());
        }
        for (ProfileDocument d : docs) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", d.getId());
            m.put("filename", d.getFileName());
            m.put("size", d.getFileSize());
            m.put("uploadedAt", d.getUploadedAt());
            m.put("downloadUrl", d.getPath());
            grouped.get(d.getType()).add(m);
        }
        return ResponseEntity.ok(ApiResponse.success("Danh sách tài liệu", grouped));
    }

    /**
     * Xóa tài liệu của tôi theo id
     */
    @DeleteMapping("/my/documents/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteMyDocument(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id,
            HttpServletRequest httpRequest) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực người dùng"));
        }
        if (user.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Chỉ ứng viên mới có thể thao tác"));
        }
        Optional<Profile> profileOpt = profileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy hồ sơ"));
        }
        Long profileId = profileOpt.get().getId();
        return profileDocumentRepository.findByIdAndProfileId(id, profileId)
                .map(doc -> {
                    boolean deleted = storageService.delete(doc.getPath());
                    profileDocumentRepository.deleteById(doc.getId());
                    String clientIp = getClientIpAddress(httpRequest);
                    auditLogger.logProfileDocumentDeleted(user.getId(), user.getEmail(), doc.getId(), doc.getType().name(), doc.getPath(), clientIp, httpRequest.getHeader("User-Agent"), deleted);
                    return ResponseEntity.ok(ApiResponse.success("Xóa tài liệu thành công", null));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy tài liệu")));
    }

    // ===== Helpers: magic number validators =====
    private boolean isPdf(MultipartFile file) {
        try {
            byte[] head = file.getInputStream().readNBytes(5);
            return head.length == 5 && head[0] == '%'
                    && head[1] == 'P' && head[2] == 'D' && head[3] == 'F' && head[4] == '-';
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isJpeg(MultipartFile file) {
        try {
            byte[] head = file.getInputStream().readNBytes(3);
            return head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPng(MultipartFile file) {
        try {
            byte[] head = file.getInputStream().readNBytes(8);
            int[] sig = {137, 80, 78, 71, 13, 10, 26, 10};
            if (head.length < 8) return false;
            for (int i = 0; i < 8; i++) {
                if ((head[i] & 0xFF) != sig[i]) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMsOffice(MultipartFile file) {
        try {
            byte[] head = file.getInputStream().readNBytes(8);
            // DOC/DOCX: có thể là OLE (D0 CF 11 E0 A1 B1 1A E1) hoặc ZIP (50 4B 03 04)
            boolean ole = head.length >= 8 && (head[0] & 0xFF) == 0xD0 && (head[1] & 0xFF) == 0xCF && (head[2] & 0xFF) == 0x11 && (head[3] & 0xFF) == 0xE0
                    && (head[4] & 0xFF) == 0xA1 && (head[5] & 0xFF) == 0xB1 && (head[6] & 0xFF) == 0x1A && (head[7] & 0xFF) == 0xE1;
            boolean zip = head.length >= 4 && (head[0] & 0xFF) == 0x50 && (head[1] & 0xFF) == 0x4B && (head[2] & 0xFF) == 0x03 && (head[3] & 0xFF) == 0x04;
            return ole || zip;
        } catch (Exception e) {
            return false;
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
