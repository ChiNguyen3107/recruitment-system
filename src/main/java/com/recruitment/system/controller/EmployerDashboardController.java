package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.recruitment.system.config.PaginationValidator;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.enums.JobStatus;
import java.util.stream.Collectors;
import java.util.Set;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerDashboardController {

    private final JobPostingRepository jobPostingRepository;
    private final DashboardService dashboardService;

    private boolean isEmployer(User user) {
        return user != null && (user.getRole() == UserRole.EMPLOYER || user.getRole() == UserRole.RECRUITER || user.getRole() == UserRole.ADMIN) && user.getCompany() != null;
    }

    /**
     * GET /api/employer/dashboard
     * - Jobs đang active của công ty
     * - Số ứng viên theo mỗi job
     * - Conversion rate basic: HIRED / tổng ứng viên của công ty
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployerDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) java.time.LocalDateTime from,
            @RequestParam(required = false) java.time.LocalDateTime to,
            @RequestParam(required = false) Integer limit
    ) {
        if (!isEmployer(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Chỉ employer/recruiter có công ty mới được truy cập"));
        }

        Long companyId = currentUser.getCompany().getId();
        Map<String, Object> payload = dashboardService.buildEmployerMetrics(companyId, from, to, limit);
        return ResponseEntity.ok(ApiResponse.success("Employer dashboard", payload));
    }

    /**
     * GET /api/employer/jobs
     * Danh sách jobs của công ty với phân trang
     */
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getCompanyJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) JobStatus status
    ) {
        if (!isEmployer(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Chỉ employer/recruiter có công ty mới được truy cập"));
        }

        try {
            Pageable pageable = PaginationValidator.buildPageable(
                    page,
                    size,
                    sortBy,
                    sortDir,
                    Set.of("createdAt", "title", "status", "applicationDeadline")
            );

            Long companyId = currentUser.getCompany().getId();
            Page<JobPosting> jobs;

            if (status != null) {
                jobs = jobPostingRepository.findByCompanyIdAndStatus(companyId, status, pageable);
            } else {
                jobs = jobPostingRepository.findByCompanyId(companyId, pageable);
            }

            List<JobPostingResponse> jobResponses = jobs.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            PageResponse<JobPostingResponse> pageResponse = new PageResponse<>(
                    jobResponses,
                    jobs.getNumber(),
                    jobs.getSize(),
                    jobs.getTotalElements(),
                    jobs.getTotalPages(),
                    jobs.isFirst(),
                    jobs.isLast(),
                    jobs.hasNext(),
                    jobs.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách jobs thành công", pageResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách jobs: " + e.getMessage()));
        }
    }

    private JobPostingResponse convertToResponse(JobPosting jobPosting) {
        JobPostingResponse response = new JobPostingResponse();
        response.setId(jobPosting.getId());
        response.setTitle(jobPosting.getTitle());
        response.setDescription(jobPosting.getDescription());
        response.setRequirements(jobPosting.getRequirements());
        response.setSkillsRequired(jobPosting.getSkillsRequired());
        response.setLocation(jobPosting.getLocation());
        response.setJobType(jobPosting.getJobType());
        response.setSalaryMin(jobPosting.getSalaryMin());
        response.setSalaryMax(jobPosting.getSalaryMax());
        response.setApplicationDeadline(jobPosting.getApplicationDeadline());
        response.setStatus(jobPosting.getStatus());
        response.setCreatedAt(jobPosting.getCreatedAt());
        response.setUpdatedAt(jobPosting.getUpdatedAt());
        return response;
    }
}


