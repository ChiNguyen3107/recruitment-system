package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerDashboardController {

    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;

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
            @AuthenticationPrincipal User currentUser
    ) {
        if (!isEmployer(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Chỉ employer/recruiter có công ty mới được truy cập"));
        }

        Long companyId = currentUser.getCompany().getId();

        Map<String, Object> payload = new HashMap<>();

        // Danh sách jobs ACTIVE còn hạn của công ty
        List<JobPosting> activeJobs = jobPostingRepository.findActiveJobs(LocalDateTime.now())
                .stream()
                .filter(j -> j.getCompany() != null && j.getCompany().getId().equals(companyId))
                .toList();

        // Đếm ứng viên theo job
        List<Map<String, Object>> jobsWithCounts = activeJobs.stream().map(job -> {
            Map<String, Object> item = new HashMap<>();
            item.put("jobId", job.getId());
            item.put("title", job.getTitle());
            item.put("status", job.getStatus().name());
            item.put("applications", applicationRepository.countByJobPostingId(job.getId()));
            return item;
        }).toList();
        payload.put("activeJobs", jobsWithCounts);

        // Conversion rate basic = HIRED / tổng ứng viên của công ty
        long totalApplications = applicationRepository.countByCompanyId(companyId);
        long hired = applicationRepository.countByCompanyIdAndStatus(companyId, com.recruitment.system.enums.ApplicationStatus.HIRED);
        double conversionRate = totalApplications == 0 ? 0.0 : (double) hired / (double) totalApplications;
        payload.put("conversionRate", conversionRate);

        return ResponseEntity.ok(ApiResponse.success("Employer dashboard", payload));
    }
}


