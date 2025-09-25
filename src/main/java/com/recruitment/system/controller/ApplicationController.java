package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ApplicationRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ApplicationResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.Application;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


/**
 * Endpoint cho Applicant nộp đơn ứng tuyển của chính mình
 */
@RestController
@RequestMapping("/api/applications/my")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final MailService mailService;

    /**
     * POST /api/applications/my
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyMy(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApplicationRequest request
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa xác thực"));
        }

        // Lấy job và kiểm tra trạng thái còn hạn và ACTIVE
        JobPosting jobPosting = jobPostingRepository.findById(request.getJobPostingId())
                .orElseThrow(() -> new RuntimeException("Tin tuyển dụng không tồn tại"));

        if (jobPosting.getStatus() == null || jobPosting.getApplicationDeadline() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tin tuyển dụng không hợp lệ hoặc chưa sẵn sàng nhận hồ sơ"));
        }
        if (!jobPosting.isActive()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tin tuyển dụng đã hết hạn hoặc không hoạt động"));
        }

        // Chống nộp trùng
        boolean alreadyApplied = applicationRepository.existsByApplicantIdAndJobPostingId(
                currentUser.getId(), jobPosting.getId()
        );
        if (alreadyApplied) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Bạn đã nộp đơn cho vị trí này trước đó"));
        }

        // Tạo Application
        Application application = new Application();
        application.setApplicant(currentUser);
        application.setJobPosting(jobPosting);
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setAdditionalDocuments(request.getAdditionalDocuments());

        // Lưu
        Application saved = applicationRepository.save(application);

        // Tăng bộ đếm ứng tuyển cho JobPosting
        jobPosting.incrementApplicationsCount();
        jobPostingRepository.save(jobPosting);

        // Gửi email thông báo tới employer (người tạo job)
        try {
            User employer = jobPosting.getCreatedBy();
            if (employer != null && employer.getEmail() != null) {
                String detailLink = null; // Có thể ghép link chi tiết nếu có trang quản trị
                mailService.sendNewApplicationEmail(
                        employer,
                        jobPosting.getTitle(),
                        currentUser.getFullName(),
                        detailLink
                );
            }
        } catch (Exception e) {
            log.warn("Không thể gửi email thông báo đơn ứng tuyển mới: {}", e.getMessage());
        }

        // Trả response
        ApplicationResponse response = convertToResponse(saved);
        return ResponseEntity.ok(ApiResponse.success("Nộp đơn thành công", response));
    }

    private ApplicationResponse convertToResponse(Application application) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(application.getId());
        resp.setStatus(application.getStatus());
        resp.setCoverLetter(application.getCoverLetter());
        resp.setResumeUrl(application.getResumeUrl());
        resp.setAdditionalDocuments(application.getAdditionalDocuments());
        resp.setInterviewDate(application.getInterviewDate());
        resp.setInterviewLocation(application.getInterviewLocation());
        resp.setInterviewNotes(application.getInterviewNotes());
        resp.setFeedback(application.getFeedback());
        resp.setRejectionReason(application.getRejectionReason());
        resp.setOfferDetails(application.getOfferDetails());
        resp.setReviewedAt(application.getReviewedAt());
        resp.setReviewedBy(application.getReviewedBy());
        resp.setCreatedAt(application.getCreatedAt());
        resp.setUpdatedAt(application.getUpdatedAt());

        if (application.getJobPosting() != null) {
            JobPosting jp = application.getJobPosting();
            JobPostingResponse jpr = new JobPostingResponse();
            jpr.setId(jp.getId());
            jpr.setTitle(jp.getTitle());
            jpr.setDescription(jp.getDescription());
            jpr.setRequirements(jp.getRequirements());
            jpr.setSkillsRequired(jp.getSkillsRequired());
            jpr.setLocation(jp.getLocation());
            jpr.setJobType(jp.getJobType());
            jpr.setSalaryMin(jp.getSalaryMin());
            jpr.setSalaryMax(jp.getSalaryMax());
            jpr.setApplicationDeadline(jp.getApplicationDeadline());
            jpr.setStatus(jp.getStatus());
            jpr.setCreatedAt(jp.getCreatedAt());
            jpr.setUpdatedAt(jp.getUpdatedAt());
            resp.setJobPosting(jpr);
        }

        if (application.getApplicant() != null) {
            User u = application.getApplicant();
            UserResponse ur = new UserResponse();
            ur.setId(u.getId());
            ur.setEmail(u.getEmail());
            ur.setFirstName(u.getFirstName());
            ur.setLastName(u.getLastName());
            ur.setFullName(u.getFullName());
            ur.setPhoneNumber(u.getPhoneNumber());
            ur.setRole(u.getRole());
            ur.setStatus(u.getStatus());
            ur.setEmailVerified(u.getEmailVerified());
            ur.setAvatarUrl(u.getAvatarUrl());
            ur.setLastLogin(u.getLastLogin());
            ur.setCreatedAt(u.getCreatedAt());
            resp.setApplicant(ur);
        }

        resp.setIsReviewed(application.isReviewed());
        resp.setIsInProgress(application.isInProgress());
        resp.setIsCompleted(application.isCompleted());

        return resp;
    }
}


