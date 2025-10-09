package com.recruitment.system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledPrecomputeService {

    private final DashboardService dashboardService;

    // 15 phút một lần precompute các dashboard nặng
    @Scheduled(fixedDelay = 15 * 60 * 1000L)
    public void precomputeDashboards() {
        // Admin overview/performance/growth - sử dụng null để lấy tất cả dữ liệu
        dashboardService.buildAdminOverview(null, null);
        dashboardService.buildAdminPerformance(null, null);
        dashboardService.buildAdminGrowth(null, null);

        // Employer dashboards: có thể mở rộng preload theo top companies nếu cần
        // Ở đây không có danh sách sẵn, nên bỏ trống, controller sẽ cache theo companyId khi gọi lần đầu
    }
}


