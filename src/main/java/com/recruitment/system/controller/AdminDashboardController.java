package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.enums.ApplicationStatus;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * GET /api/admin/dashboard
     * - Tổng số users
     * - Jobs theo trạng thái
     * - Applications theo trạng thái
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboard() {
        Map<String, Object> payload = new HashMap<>();

        // Tổng users
        payload.put("totalUsers", userRepository.count());

        // Jobs theo trạng thái
        Map<String, Long> jobsStatusCounts = new HashMap<>();
        for (JobStatus status : JobStatus.values()) {
            jobsStatusCounts.put(status.name(), jobPostingRepository.countByStatus(status));
        }
        payload.put("jobsByStatus", jobsStatusCounts);

        // Applications theo trạng thái
        Map<String, Long> applicationsStatusCounts = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            applicationsStatusCounts.put(status.name(), applicationRepository.countByStatus(status));
        }
        payload.put("applicationsByStatus", applicationsStatusCounts);

        return ResponseEntity.ok(ApiResponse.success("Admin dashboard", payload));
    }
}


