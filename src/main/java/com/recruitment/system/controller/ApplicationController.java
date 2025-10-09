package com.recruitment.system.controller;

import com.recruitment.system.dto.request.ApplicationRequest;
import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.recruitment.system.config.PaginationValidator;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.enums.ApplicationStatus;
import com.recruitment.system.entity.ApplicationTimeline;
import com.recruitment.system.repository.ApplicationTimelineRepository;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;


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
    private final AuditLogger auditLogger;
    private final ApplicationTimelineRepository applicationTimelineRepository;

    private final NotificationService notificationService;

    /**
     * POST /api/applications/my
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyMy(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApplicationRequest request,
            HttpServletRequest httpRequest
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa xác thực"));
        }

        // Sanitize input để tránh XSS và các vấn đề bảo mật
        request.sanitize();

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
            return ResponseEntity.status(409).body(ApiResponse.error("Bạn đã nộp đơn cho vị trí này trước đó"));
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

        // Gửi notification cho nhà tuyển dụng khi có đơn ứng tuyển mới
        try {
            User employer = jobPosting.getCreatedBy();
            if (employer != null) {
                notificationService.notifyNewApplication(
                        employer.getId(),                                // employerId
                        jobPosting.getTitle(),                           // job title
                        currentUser.getFullName(),                       // applicant name
                        "/employer/jobs/" + jobPosting.getId() // link trang quản lý
                );
            }
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo cho employer: {}", e.getMessage());
        }


        // Trả response
        ApplicationResponse response = convertToResponse(saved);

        // Audit
        String clientIp = getClientIpAddress(httpRequest);
        Long companyId = jobPosting.getCompany() != null ? jobPosting.getCompany().getId() : null;
        auditLogger.logApplicationSubmitted(saved.getId(), jobPosting.getId(), companyId, currentUser.getEmail(), clientIp, httpRequest.getHeader("User-Agent"), true);
        return ResponseEntity.ok(ApiResponse.success("Nộp đơn thành công", response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    /**
     * GET /api/applications/my
     * Lấy danh sách đơn ứng tuyển của applicant hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa xác thực"));
        }

        try {
            Pageable pageable = PaginationValidator.buildPageable(
                    page,
                    size,
                    sortBy,
                    sortDir,
                    Set.of("createdAt", "status", "updatedAt")
            );

            Page<Application> applications;
            if (status != null) {
                applications = applicationRepository.findByApplicantIdAndStatus(currentUser.getId(), status, pageable);
            } else {
                applications = applicationRepository.findByApplicantId(currentUser.getId(), pageable);
            }

            List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            PageResponse<ApplicationResponse> pageResponse = new PageResponse<>(
                    applicationResponses,
                    applications.getNumber(),
                    applications.getSize(),
                    applications.getTotalElements(),
                    applications.getTotalPages(),
                    applications.isFirst(),
                    applications.isLast(),
                    applications.hasNext(),
                    applications.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn ứng tuyển thành công", pageResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách đơn ứng tuyển: " + e.getMessage()));
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

        try {
            java.util.List<com.recruitment.system.dto.response.ApplicationTimelineResponse> timeline = applicationTimelineRepository
                    .findByApplicationIdOrderByChangedAtAsc(application.getId())
                    .stream()
                    .map(t -> {
                        com.recruitment.system.dto.response.ApplicationTimelineResponse tr = new com.recruitment.system.dto.response.ApplicationTimelineResponse();
                        tr.setId(t.getId());
                        tr.setFromStatus(t.getFromStatus());
                        tr.setToStatus(t.getToStatus());
                        tr.setNote(t.getNote());
                        tr.setChangedBy(t.getChangedBy());
                        tr.setChangedAt(t.getChangedAt());
                        return tr;
                    })
                    .toList();
            resp.setTimeline(timeline);
        } catch (Exception ignored) {}

        return resp;
    }

    /**
     * POST /api/applications/my/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdrawMyApplication(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa xác thực"));
        }

        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Đơn ứng tuyển không tồn tại"));
        }
        if (!app.getApplicant().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền rút đơn này"));
        }

        if (!(app.getStatus() == ApplicationStatus.RECEIVED || app.getStatus() == ApplicationStatus.REVIEWED)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ được rút đơn khi trạng thái là RECEIVED hoặc REVIEWED"));
        }

        ApplicationStatus from = app.getStatus();
        app.updateStatus(ApplicationStatus.WITHDRAWN, "Applicant withdraw application");
        Application saved = applicationRepository.save(app);

        // Decrement job applications count
        JobPosting job = saved.getJobPosting();
        if (job != null) {
            job.decrementApplicationsCount();
            jobPostingRepository.save(job);
        }

        // Timeline
        ApplicationTimeline timeline = new ApplicationTimeline();
        timeline.setApplicationId(saved.getId());
        timeline.setFromStatus(from);
        timeline.setToStatus(ApplicationStatus.WITHDRAWN);
        timeline.setNote("Applicant withdraw application");
        timeline.setChangedBy(currentUser.getId());
        applicationTimelineRepository.save(timeline);

        // Email notify employer
        try {
            if (job != null && job.getCreatedBy() != null) {
                User employer = job.getCreatedBy();
                String subjectJobTitle = job.getTitle();
                String applicantName = currentUser.getFullName();
                mailService.sendApplicationWithdrawnEmail(employer, subjectJobTitle, applicantName);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi email thông báo rút đơn: {}", e.getMessage());
        }

        // Audit
        String clientIp = getClientIpAddress(httpRequest);
        Long companyId = job != null && job.getCompany() != null ? job.getCompany().getId() : null;
        auditLogger.logApplicationStatusChanged(saved.getId(), companyId, from.name(), ApplicationStatus.WITHDRAWN.name(), currentUser.getEmail(), clientIp, httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.success("Rút đơn thành công", convertToResponse(saved)));
    }
}


