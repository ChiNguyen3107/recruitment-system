package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.request.InterviewRescheduleRequest;
import com.recruitment.system.dto.request.InterviewCancelRequest;
import com.recruitment.system.dto.request.InterviewCompleteRequest;
import com.recruitment.system.dto.request.InterviewScheduleRequest;
import com.recruitment.system.dto.response.InterviewResponse;
import com.recruitment.system.entity.Application;
import com.recruitment.system.entity.ApplicationTimeline;
import com.recruitment.system.entity.Interview;
import com.recruitment.system.entity.Notification;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.ApplicationStatus;
import com.recruitment.system.enums.InterviewStatus;
import com.recruitment.system.enums.InterviewType;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.ApplicationTimelineRepository;
import com.recruitment.system.repository.InterviewRepository;
import com.recruitment.system.repository.NotificationRepository;
import com.recruitment.system.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    private boolean isEmployerOrRecruiter(User user) {
        return user != null && (user.getRole().name().equals("EMPLOYER") || user.getRole().name().equals("RECRUITER") || user.getRole().name().equals("ADMIN")) && user.getCompany() != null;
    }

    private boolean sameCompany(User user, Application application) {
        return application.getJobPosting() != null && application.getJobPosting().getCompany() != null
                && user.getCompany() != null
                && application.getJobPosting().getCompany().getId().equals(user.getCompany().getId());
    }

    private InterviewResponse toResponse(Interview interview) {
        InterviewResponse r = new InterviewResponse();
        r.setId(interview.getId());
        r.setApplicationId(interview.getApplicationId());
        r.setScheduledAt(interview.getScheduledAt());
        r.setDurationMinutes(resolveDuration(interview));
        r.setLocation(interview.getLocation());
        r.setMeetingLink(interview.getMeetingLink());
        r.setInterviewType(resolveType(interview));
        r.setNotes(interview.getNotes());
        r.setStatus(mapDbStatus(interview.getDbStatus()));
        r.setScheduledBy(interview.getScheduledBy());
        r.setCreatedAt(interview.getScheduledAt());
        r.setUpdatedAt(interview.getEndTime());
        return r;
    }

    private Integer resolveDuration(Interview iv) {
        if (iv.getDurationMinutes() != null) return iv.getDurationMinutes();
        if (iv.getScheduledAt() != null && iv.getEndTime() != null) {
            return (int) java.time.Duration.between(iv.getScheduledAt(), iv.getEndTime()).toMinutes();
        }
        return 60;
    }

    private InterviewType resolveType(Interview iv) {
        if (iv.getMethod() == null) return InterviewType.VIDEO;
        return "OFFLINE".equalsIgnoreCase(iv.getMethod()) ? InterviewType.ONSITE : InterviewType.VIDEO;
    }

    private InterviewStatus mapDbStatus(String s) {
        if (s == null) return InterviewStatus.SCHEDULED;
        switch (s) {
            case "HOAN_TAT": return InterviewStatus.COMPLETED;
            case "HUY": return InterviewStatus.CANCELLED;
            case "XAC_NHAN": return InterviewStatus.SCHEDULED;
            default: return InterviewStatus.SCHEDULED;
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(@AuthenticationPrincipal User currentUser,
                                                                   @RequestBody InterviewScheduleRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }

        Optional<Application> opt = applicationRepository.findById(request.getApplicationId());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy đơn ứng tuyển"));
        }
        Application application = opt.get();

        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền lên lịch cho đơn này"));
        }

        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Trạng thái đơn phải là INTERVIEW"));
        }

        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now().plusHours(1))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Thời gian phỏng vấn phải sau hiện tại ít nhất 1 giờ"));
        }

        // Kiểm tra trùng lịch của người lên lịch (interviewer)
        LocalDateTime start = request.getScheduledAt();
        LocalDateTime end = start.plusMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
        List<Interview> conflicts = interviewRepository.findByScheduledByAndScheduledAtBetween(currentUser.getId(), start.minusMinutes(59), end);
        if (!conflicts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Bị trùng lịch phỏng vấn khác"));
        }

        Interview interview = new Interview();
        interview.setApplicationId(application.getId());
        if (application.getJobPosting() != null && application.getJobPosting().getCompany() != null) {
            interview.setCompanyId(application.getJobPosting().getCompany().getId());
        }
        interview.setScheduledAt(request.getScheduledAt());
        interview.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
        interview.setEndTime(interview.getScheduledAt().plusMinutes(interview.getDurationMinutes()));
        interview.setMethod(request.getInterviewType() == InterviewType.ONSITE ? "OFFLINE" : "ONLINE");
        interview.setLocation(request.getLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNotes(request.getNotes());
        interview.setDbStatus("XAC_NHAN");
        interview.setScheduledBy(currentUser.getId());

        Interview saved = interviewRepository.save(interview);

        // Cập nhật application.interviewDate
        application.setInterviewDate(saved.getScheduledAt());
        applicationRepository.save(application);

        // Tạo timeline record
        ApplicationTimeline tl = new ApplicationTimeline();
        tl.setApplicationId(application.getId());
        tl.setFromStatus(ApplicationStatus.INTERVIEW);
        tl.setToStatus(ApplicationStatus.INTERVIEW);
        tl.setNote("Lên lịch phỏng vấn: " + saved.getScheduledAt());
        tl.setChangedBy(currentUser.getId());
        tl.setChangedAt(LocalDateTime.now());
        applicationTimelineRepository.save(tl);

        // Tạo notification cho applicant
        Notification n = new Notification();
        n.setRecipientId(application.getApplicant().getId());
        n.setType("INTERVIEW_SCHEDULED");
        n.setTitle("Bạn có lịch phỏng vấn mới");
        n.setContent("Thời gian: " + saved.getScheduledAt());
        notificationRepository.save(n);

        // Gửi email + .ics cho applicant và interviewer
        try {
            User applicant = application.getApplicant();
            if (applicant != null && applicant.getEmail() != null) {
                mailService.sendInterviewInvite(
                        applicant,
                        saved.getScheduledAt(),
                        saved.getDurationMinutes() != null ? saved.getDurationMinutes() : 60,
                        "Phỏng vấn cho vị trí " + application.getJobPosting().getTitle(),
                        request.getNotes(),
                        saved.getLocation(),
                        saved.getMeetingLink(),
                        currentUser.getEmail()
                );
            }
            // gửi cho interviewer (currentUser)
            if (currentUser.getEmail() != null) {
                mailService.sendInterviewInvite(
                        currentUser,
                        saved.getScheduledAt(),
                        saved.getDurationMinutes() != null ? saved.getDurationMinutes() : 60,
                        "Phỏng vấn với ứng viên " + applicantName(application),
                        request.getNotes(),
                        saved.getLocation(),
                        saved.getMeetingLink(),
                        currentUser.getEmail()
                );
            }
        } catch (Exception ex) {
            log.warn("Gửi email lịch phỏng vấn thất bại: {}", ex.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(toResponse(saved)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<InterviewResponse>>> my(@AuthenticationPrincipal User currentUser,
                                                                   @RequestParam(required = false) LocalDateTime start,
                                                                   @RequestParam(required = false) LocalDateTime end,
                                                                   @RequestParam(required = false) InterviewStatus status,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        LocalDateTime startAt = start != null ? start : LocalDateTime.now().minusMonths(1);
        LocalDateTime endAt = end != null ? end : LocalDateTime.now().plusMonths(3);
        Pageable pageable = PageRequest.of(page, size);

        Page<Interview> pageData = interviewRepository.findByScheduledAtBetweenOrderByScheduledAtAsc(startAt, endAt, pageable);

        // Lọc theo vai trò
        List<Interview> filtered = pageData.getContent().stream().filter(iv -> {
            if (currentUser.isApplicant()) {
                Optional<Application> app = applicationRepository.findById(iv.getApplicationId());
                return app.isPresent() && app.get().getApplicant().getId().equals(currentUser.getId());
            }
            if (isEmployerOrRecruiter(currentUser)) {
                if (!statusMatch(iv, status)) return false;
                return iv.getScheduledBy().equals(currentUser.getId());
            }
            return false;
        }).collect(Collectors.toList());

        Page<InterviewResponse> mapped = pageData.map(this::toResponse);
        // thay content bằng filtered map
        List<InterviewResponse> content = filtered.stream().map(this::toResponse).collect(Collectors.toList());
        Page<InterviewResponse> resp = new org.springframework.data.domain.PageImpl<>(content, pageable, content.size());
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    private boolean statusMatch(Interview iv, InterviewStatus status) {
        if (status == null) return true;
        return mapDbStatus(iv.getDbStatus()) == status;
    }

    private String applicantName(Application application) {
        try {
            return application.getApplicant() != null ? application.getApplicant().getFullName() : "ứng viên";
        } catch (Exception e) {
            return "ứng viên";
        }
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<InterviewResponse>> reschedule(@PathVariable Long id,
                                                                     @AuthenticationPrincipal User currentUser,
                                                                     @RequestBody InterviewRescheduleRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy interview"));
        }
        Interview interview = opt.get();
        Optional<Application> appOpt = applicationRepository.findById(interview.getApplicationId());
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy application"));
        }
        Application application = appOpt.get();

        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền"));
        }

        if (request.getNewScheduledAt() == null || request.getNewScheduledAt().isBefore(LocalDateTime.now().plusHours(1))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Thời gian mới phải sau hiện tại ít nhất 1 giờ"));
        }

        LocalDateTime start = request.getNewScheduledAt();
        LocalDateTime end = start.plusMinutes(interview.getDurationMinutes() != null ? interview.getDurationMinutes() : resolveDuration(interview));
        List<Interview> conflicts = interviewRepository.findByScheduledByAndScheduledAtBetween(currentUser.getId(), start.minusMinutes(59), end);
        if (!conflicts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Bị trùng lịch phỏng vấn khác"));
        }

        LocalDateTime old = interview.getScheduledAt();
        interview.setScheduledAt(request.getNewScheduledAt());
        interview.setEndTime(interview.getScheduledAt().plusMinutes(resolveDuration(interview)));
        interview.setDbStatus("XAC_NHAN");
        Interview saved = interviewRepository.save(interview);

        application.setInterviewDate(saved.getScheduledAt());
        applicationRepository.save(application);

        ApplicationTimeline tl = new ApplicationTimeline();
        tl.setApplicationId(application.getId());
        tl.setFromStatus(ApplicationStatus.INTERVIEW);
        tl.setToStatus(ApplicationStatus.INTERVIEW);
        tl.setNote("Đổi lịch phỏng vấn từ " + old + " sang " + saved.getScheduledAt() + (request.getReason() != null ? ". Lý do: " + request.getReason() : ""));
        tl.setChangedBy(currentUser.getId());
        tl.setChangedAt(LocalDateTime.now());
        applicationTimelineRepository.save(tl);

        try {
            User applicant = application.getApplicant();
            if (applicant != null && applicant.getEmail() != null) {
                mailService.sendInterviewInvite(
                        applicant,
                        saved.getScheduledAt(),
                        saved.getDurationMinutes() != null ? saved.getDurationMinutes() : 60,
                        "Lịch phỏng vấn đã thay đổi - " + application.getJobPosting().getTitle(),
                        request.getReason(),
                        saved.getLocation(),
                        saved.getMeetingLink(),
                        currentUser.getEmail()
                );
            }
            if (currentUser.getEmail() != null) {
                mailService.sendInterviewInvite(
                        currentUser,
                        saved.getScheduledAt(),
                        saved.getDurationMinutes() != null ? saved.getDurationMinutes() : 60,
                        "Đổi lịch phỏng vấn với ứng viên " + applicantName(application),
                        request.getReason(),
                        saved.getLocation(),
                        saved.getMeetingLink(),
                        currentUser.getEmail()
                );
            }
        } catch (Exception ex) {
            log.warn("Gửi email đổi lịch phỏng vấn thất bại: {}", ex.getMessage());
        }

        

        InterviewResponse resp = toResponse(saved);
        resp.setStatus(InterviewStatus.RESCHEDULED);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InterviewResponse>> cancel(@PathVariable Long id,
                                                                 @AuthenticationPrincipal User currentUser,
                                                                 @RequestBody InterviewCancelRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy interview"));
        }
        Interview interview = opt.get();
        Optional<Application> appOpt = applicationRepository.findById(interview.getApplicationId());
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy application"));
        }
        Application application = appOpt.get();
        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền"));
        }

        interview.setDbStatus("HUY");
        Interview saved = interviewRepository.save(interview);

        ApplicationTimeline tl = new ApplicationTimeline();
        tl.setApplicationId(application.getId());
        tl.setFromStatus(ApplicationStatus.INTERVIEW);
        tl.setToStatus(ApplicationStatus.INTERVIEW);
        tl.setNote("Hủy lịch phỏng vấn" + (request.getReason() != null ? (": " + request.getReason()) : ""));
        tl.setChangedBy(currentUser.getId());
        tl.setChangedAt(LocalDateTime.now());
        applicationTimelineRepository.save(tl);

        // thông báo
        Notification n = new Notification();
        n.setRecipientId(application.getApplicant().getId());
        n.setType("INTERVIEW_CANCELLED");
        n.setTitle("Lịch phỏng vấn đã bị hủy");
        n.setContent(request.getReason());
        notificationRepository.save(n);

        // email (không kèm .ics)
        try {
            User applicant = application.getApplicant();
            if (applicant != null && applicant.getEmail() != null) {
                mailService.sendApplicationStatusChangedEmail(applicant, application.getJobPosting().getTitle(), "Phỏng vấn bị hủy", request.getReason());
            }
        } catch (Exception ignored) { }

        InterviewResponse resp = toResponse(saved);
        resp.setStatus(InterviewStatus.CANCELLED);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<InterviewResponse>> complete(@PathVariable Long id,
                                                                   @AuthenticationPrincipal User currentUser,
                                                                   @RequestBody InterviewCompleteRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực"));
        }
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy interview"));
        }
        Interview interview = opt.get();
        Optional<Application> appOpt = applicationRepository.findById(interview.getApplicationId());
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy application"));
        }
        Application application = appOpt.get();
        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền"));
        }

        interview.setDbStatus("HOAN_TAT");
        Interview saved = interviewRepository.save(interview);

        ApplicationTimeline tl = new ApplicationTimeline();
        tl.setApplicationId(application.getId());
        tl.setFromStatus(ApplicationStatus.INTERVIEW);
        tl.setToStatus(ApplicationStatus.INTERVIEW);
        tl.setNote("Hoàn tất phỏng vấn" + (request.getNotes() != null ? (": " + request.getNotes()) : ""));
        tl.setChangedBy(currentUser.getId());
        tl.setChangedAt(LocalDateTime.now());
        applicationTimelineRepository.save(tl);

        Notification n = new Notification();
        n.setRecipientId(application.getApplicant().getId());
        n.setType("INTERVIEW_COMPLETED");
        n.setTitle("Phỏng vấn đã hoàn tất");
        n.setContent(request.getNotes());
        notificationRepository.save(n);

        InterviewResponse resp = toResponse(saved);
        resp.setStatus(InterviewStatus.COMPLETED);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}


