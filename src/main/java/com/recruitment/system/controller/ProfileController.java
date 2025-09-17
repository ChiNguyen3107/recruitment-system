package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ProfileResponse;
import com.recruitment.system.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý quản lý hồ sơ người dùng
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping
    @PreAuthorize("hasRole('APPLICANT') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            ProfileResponse profile = profileService.getCurrentUserProfile(email);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin profile thành công", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin profile
     */
    @PutMapping
    @PreAuthorize("hasRole('APPLICANT') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ProfileResponse profile = profileService.updateProfile(email, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật profile thành công", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa profile
     */
    @DeleteMapping
    @PreAuthorize("hasRole('APPLICANT') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            profileService.deleteProfile(email);
            return ResponseEntity.ok(ApiResponse.success("Xóa profile thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xem profile công khai của user khác
     */
    @GetMapping("/public/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getPublicProfile(@PathVariable Long userId) {
        try {
            ProfileResponse profile = profileService.getPublicProfile(userId);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin profile công khai thành công", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đặt profile thành công khai/riêng tư
     */
    @PatchMapping("/visibility")
    @PreAuthorize("hasRole('APPLICANT') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfileVisibility(
            @RequestParam Boolean isPublic,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ProfileRequest request = new ProfileRequest();
            request.setIsPublic(isPublic);
            ProfileResponse profile = profileService.updateProfile(email, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái công khai thành công", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}