package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ApplicationStatusUpdateRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.ApplicationResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.Application;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.ApplicationStatus;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/applications/manage")
@RequiredArgsConstructor
@Slf4j
public class ManageApplicationController {

    private final ApplicationRepository applicationRepository;
    private final MailService mailService;

    private boolean isEmployerOfCompany(User user) {
        return user != null && (user.getRole() == UserRole.EMPLOYER || user.getRole() == UserRole.RECRUITER) && user.getCompany() != null;
    }

    private boolean sameCompany(User user, Application application) {
        return application.getJobPosting() != null && application.getJobPosting().getCompany() != null
                && user.getCompany() != null
                && application.getJobPosting().getCompany().getId().equals(user.getCompany().getId());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long jobPostingId
    ) {
        if (!isEmployerOfCompany(currentUser)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Chỉ employer/recruiter mới được truy cập"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Application> result;
        Long companyId = currentUser.getCompany().getId();

        if (jobPostingId != null && status != null) {
            result = applicationRepository.findByCompanyIdAndJobPostingIdAndStatus(companyId, jobPostingId, status, pageable);
        } else if (jobPostingId != null) {
            result = applicationRepository.findByCompanyIdAndJobPostingId(companyId, jobPostingId, pageable);
        } else if (status != null) {
            result = applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            result = applicationRepository.findByCompanyId(companyId, pageable);
        }

        Page<ApplicationResponse> mapped = result.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(mapped));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> detail(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id
    ) {
        if (!isEmployerOfCompany(currentUser)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Chỉ employer/recruiter mới được truy cập"));
        }

        Optional<Application> optional = applicationRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Đơn ứng tuyển không tồn tại"));
        }
        Application app = optional.get();
        if (!sameCompany(currentUser, app)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền truy cập đơn này"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToResponse(app)));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest req
    ) {
        if (!isEmployerOfCompany(currentUser)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Chỉ employer/recruiter mới được cập nhật"));
        }

        Application application = applicationRepository.findById(id)
                .orElse(null);
        if (application == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Đơn ứng tuyển không tồn tại"));
        }
        if (!sameCompany(currentUser, application)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền cập nhật đơn này"));
        }

        ApplicationStatus current = application.getStatus();
        ApplicationStatus next = req.getStatus();

        if (!isValidTransition(current, next)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Trạng thái không hợp lệ theo flow"));
        }

        application.updateStatus(next, req.getNotes());
        application.setReviewedBy(currentUser.getId());

        Application saved = applicationRepository.save(application);

        // Gửi email cho ứng viên
        try {
            User applicant = saved.getApplicant();
            String jobTitle = saved.getJobPosting() != null ? saved.getJobPosting().getTitle() : "Vị trí";
            mailService.sendApplicationStatusChangedEmail(applicant, jobTitle, next.getDisplayName(), req.getNotes());
        } catch (Exception e) {
            log.warn("Không thể gửi email cập nhật trạng thái: {}", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", convertToResponse(saved)));
    }

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) return true; // cho phép idempotent
        switch (current) {
            case RECEIVED:
                return next == ApplicationStatus.REVIEWED || next == ApplicationStatus.REJECTED;
            case REVIEWED:
                return next == ApplicationStatus.INTERVIEW || next == ApplicationStatus.REJECTED;
            case INTERVIEW:
                return next == ApplicationStatus.OFFER || next == ApplicationStatus.REJECTED;
            case OFFER:
                return next == ApplicationStatus.HIRED || next == ApplicationStatus.REJECTED;
            case HIRED:
            case REJECTED:
                return false;
            default:
                return false;
        }
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



