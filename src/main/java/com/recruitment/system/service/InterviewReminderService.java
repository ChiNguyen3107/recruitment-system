package com.recruitment.system.service;

import com.recruitment.system.entity.Application;
import com.recruitment.system.entity.Interview;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewReminderService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final MailService mailService;

    // Chạy mỗi 15 phút
    @Scheduled(cron = "0 */15 * * * *")
    public void sendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            remindAtOffsetHours(now, 24);
            remindAtOffsetHours(now, 2);
        } catch (Exception e) {
            log.warn("Interview reminder job failed: {}", e.getMessage());
        }
    }

    private void remindAtOffsetHours(LocalDateTime base, int hours) {
        LocalDateTime start = base.plusHours(hours);
        LocalDateTime end = start.plusMinutes(30); // cửa sổ 30' để gom batch
        List<Interview> upcoming = interviewRepository.findByDbStatusAndScheduledAtBetween("XAC_NHAN", start, end);
        for (Interview iv : upcoming) {
            Optional<Application> appOpt = applicationRepository.findById(iv.getApplicationId());
            if (appOpt.isEmpty()) continue;
            Application app = appOpt.get();
            try {
                if (app.getApplicant() != null && app.getApplicant().getEmail() != null) {
                    mailService.sendInterviewInvite(
                            app.getApplicant(),
                            iv.getScheduledAt(),
                            iv.getDurationMinutes() != null ? iv.getDurationMinutes() : 60,
                            (hours == 24 ? "[Nhắc trước 24h] " : "[Nhắc trước 2h] ") + app.getJobPosting().getTitle(),
                            iv.getNotes(),
                            iv.getLocation(),
                            iv.getMeetingLink(),
                            null
                    );
                }
            } catch (Exception ex) {
                log.debug("Skip send reminder for interview {}: {}", iv.getId(), ex.getMessage());
            }
        }
    }
}


