package com.recruitment.system.service;

import com.recruitment.system.entity.Application;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.enums.NotificationType;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobDeadlineReminderScheduler {

    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh") //check vào 8h sáng mỗi ngày
    @Transactional
    public void sendDeadlineReminders() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfRange = LocalDate.now().plusDays(3).atTime(23, 59, 59);

        log.info("[Scheduler] Kiểm tra các tin tuyển dụng sắp hết hạn từ {} đến {}", startOfToday, endOfRange);

        // Lấy job có hạn nộp trong 3 ngày tới
        List<JobPosting> expiringJobs = jobPostingRepository
                .findByApplicationDeadlineBetween(startOfToday, endOfRange);

        if (expiringJobs.isEmpty()) {
            log.info("Không có tin tuyển dụng nào sắp hết hạn.");
            return;
        }

        for (JobPosting job : expiringJobs) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), job.getApplicationDeadline().toLocalDate());
            log.info("Job sắp hết hạn: {} - '{}', còn {} ngày", job.getId(), job.getTitle(), daysLeft);

            // Lấy danh sách ứng viên đã nộp cho job này
            List<Application> applications = applicationRepository.findByJobPostingId(job.getId());
            for (Application app : applications) {
                if (app.getApplicant() != null) {
                    Long applicantId = app.getApplicant().getId();

                    notificationService.createNotification(
                            applicantId,
                            NotificationType.JOB_DEADLINE_REMINDER,
                            "Tin tuyển dụng sắp hết hạn",
                            "Tin tuyển dụng '" + job.getTitle() + "' sẽ hết hạn trong " + daysLeft + " ngày tới.",
                            "/jobs/" + job.getId()
                    );

                    log.info("→ Đã tạo thông báo deadline cho user {} - job {}", applicantId, job.getId());
                }
            }
        }

        log.info("[Scheduler] Đã gửi thông báo JOB_DEADLINE_REMINDER cho các ứng viên liên quan.");
    }
}
