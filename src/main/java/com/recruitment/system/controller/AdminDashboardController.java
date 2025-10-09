package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
// Removed unused imports of enums
// Xoá bớt dependency không dùng trực tiếp tại controller
import com.recruitment.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/admin/dashboard
     * - Tổng số users
     * - Jobs theo trạng thái
     * - Applications theo trạng thái
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboard(
            @RequestParam(required = false) java.time.LocalDateTime from,
            @RequestParam(required = false) java.time.LocalDateTime to
    ) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("systemOverview", dashboardService.buildAdminOverview(from, to));
        payload.put("performanceMetrics", dashboardService.buildAdminPerformance(from, to));
        payload.put("growthTrends", dashboardService.buildAdminGrowth(from, to));

        return ResponseEntity.ok(ApiResponse.success("Admin dashboard", payload));
    }
}


