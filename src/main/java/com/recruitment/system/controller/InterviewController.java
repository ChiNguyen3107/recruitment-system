package com.recruitment.system.controller;

import com.recruitment.system.enums.NotificationType;
import com.recruitment.system.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import com.recruitment.system.repository.InterviewParticipantRepository;
import com.recruitment.system.entity.InterviewParticipant;
import com.recruitment.system.dto.request.InterviewParticipantsRequest;
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
@Tag(name = "Interviews", description = "Quản lý lịch phỏng vấn")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final InterviewParticipantRepository interviewParticipantRepository;

    private final NotificationService notificationService;

    private boolean isEmployerOrRecruiter(User user) {
        return user != null && (user.getRole().name().equals("EMPLOYER") || user.getRole().name().equals("RECRUITER") || user.getRole().name().equals("ADMIN")) && user.getCompany() != null;
    }

    @PostMapping("/{id}/participants")
    @Operation(summary = "Thêm người tham gia phỏng vấn")
    public ResponseEntity<ApiResponse<InterviewResponse>> addParticipants(@PathVariable Long id,
                                                                          @AuthenticationPrincipal User currentUser,
                                                                          @RequestBody InterviewParticipantsRequest request) {
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy interview"));
        Interview interview = opt.get();
        Optional<Application> appOpt = applicationRepository.findById(interview.getApplicationId());
        if (appOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy application"));
        Application application = appOpt.get();
        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền"));
        }

        String role = (request.getRole() == null || request.getRole().isBlank()) ? "INTERVIEWER" : request.getRole();
        for (Long uid : request.getUserIds()) {
            if (uid == null) continue;
            if (interviewParticipantRepository.existsByInterviewIdAndUserId(id, uid)) continue;
            InterviewParticipant p = new InterviewParticipant();
            p.setInterviewId(id);
            p.setUserId(uid);
            p.setRole(role);
            interviewParticipantRepository.save(p);
        }
        return ResponseEntity.ok(ApiResponse.success(toResponse(interview)));
    }

    @DeleteMapping("/{id}/participants")
    @Operation(summary = "Xóa người tham gia phỏng vấn")
    public ResponseEntity<ApiResponse<InterviewResponse>> removeParticipants(@PathVariable Long id,
                                                                             @AuthenticationPrincipal User currentUser,
                                                                             @RequestBody InterviewParticipantsRequest request) {
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy interview"));
        Interview interview = opt.get();
        Optional<Application> appOpt = applicationRepository.findById(interview.getApplicationId());
        if (appOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy application"));
        Application application = appOpt.get();
        if (!isEmployerOrRecruiter(currentUser) || !sameCompany(currentUser, application)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Không có quyền"));
        }
        for (Long uid : request.getUserIds()) {
            if (uid == null) continue;
            interviewParticipantRepository.deleteByInterviewIdAndUserId(id, uid);
        }
        return ResponseEntity.ok(ApiResponse.success(toResponse(interview)));
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
    @Operation(summary = "Tạo lịch phỏng vấn", description = "EMPLOYER/RECRUITER cùng công ty với application")
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

        // Kiểm tra trùng lịch cho applicant (ứng viên có lịch khác cùng thời gian)
        try {
            Long applicantId = application.getApplicant() != null ? application.getApplicant().getId() : null;
            if (applicantId != null) {
                List<Application> applicantApps = applicationRepository.findByApplicantId(applicantId);
                if (applicantApps != null && !applicantApps.isEmpty()) {
                    List<Long> appIds = applicantApps.stream().map(Application::getId).toList();
                    List<Interview> applicantInterviews = interviewRepository.findByApplicationIdIn(appIds);
                    boolean overlap = applicantInterviews.stream().anyMatch(iv -> {
                        if (iv.getId() != null && iv.getApplicationId().equals(application.getId())) return false; // bỏ qua chính lịch này (nếu có)
                        LocalDateTime ivStart = iv.getScheduledAt();
                        LocalDateTime ivEnd = iv.getEndTime() != null ? iv.getEndTime() : (ivStart != null ? ivStart.plusMinutes(resolveDuration(iv)) : null);
                        if (ivStart == null || ivEnd == null) return false;
                        return !(end.isBefore(ivStart) || start.isAfter(ivEnd));
                    });
                    if (overlap) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Ứng viên đã có lịch phỏng vấn khác trong khoảng thời gian này"));
                    }
                }
            }
        } catch (Exception ignored) {}

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

        //  Gửi notification bằng service để đồng bộ logic enum & audit
        notificationService.createNotification(
                application.getApplicant().getId(),
                NotificationType.INTERVIEW_SCHEDULED,
                "Bạn có lịch phỏng vấn mới",
                "Thời gian: " + saved.getScheduledAt(),
                "/applicant/interviews"
        );

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
    @Operation(summary = "Danh sách lịch của tôi", description = "Applicant: lịch của họ; Employer/Recruiter: lịch họ tạo")
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
    @Operation(summary = "Đổi lịch phỏng vấn")
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

        // Kiểm tra trùng lịch cho applicant khi dời lịch
        try {
            Long applicantId = application.getApplicant() != null ? application.getApplicant().getId() : null;
            if (applicantId != null) {
                List<Application> applicantApps = applicationRepository.findByApplicantId(applicantId);
                if (applicantApps != null && !applicantApps.isEmpty()) {
                    List<Long> appIds = applicantApps.stream().map(Application::getId).toList();
                    List<Interview> applicantInterviews = interviewRepository.findByApplicationIdIn(appIds);
                    boolean overlap = applicantInterviews.stream().anyMatch(iv -> {
                        if (iv.getId() != null && iv.getId().equals(interview.getId())) return false; // bỏ qua chính lịch này
                        LocalDateTime ivStart = iv.getScheduledAt();
                        LocalDateTime ivEnd = iv.getEndTime() != null ? iv.getEndTime() : (ivStart != null ? ivStart.plusMinutes(resolveDuration(iv)) : null);
                        if (ivStart == null || ivEnd == null) return false;
                        return !(end.isBefore(ivStart) || start.isAfter(ivEnd));
                    });
                    if (overlap) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Ứng viên đã có lịch phỏng vấn khác trong khoảng thời gian này"));
                    }
                }
            }
        } catch (Exception ignored) {}

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
    @Operation(summary = "Hủy lịch phỏng vấn")
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

        //  Thông báo hủy lịch phỏng vấn
        notificationService.createNotification(
                application.getApplicant().getId(),
                NotificationType.INTERVIEW_CANCELLED,
                "Lịch phỏng vấn đã bị hủy",
                request.getReason(),
                "/applicant/interviews"
        );

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
    @Operation(summary = "Hoàn tất phỏng vấn")
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

        // Thông báo phỏng vấn hoàn tất
        notificationService.createNotification(
                application.getApplicant().getId(),
                NotificationType.INTERVIEW_COMPLETED,
                "Phỏng vấn đã hoàn tất",
                request.getNotes(),
                "/applicant/interviews"
        );

        InterviewResponse resp = toResponse(saved);
        resp.setStatus(InterviewStatus.COMPLETED);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
    
    @GetMapping("/{id}/participants")
    @Operation(summary = "Lấy danh sách người tham gia phỏng vấn")
    public ResponseEntity<ApiResponse<List<InterviewParticipant>>> getParticipants(@PathVariable Long id) {
        List<InterviewParticipant> list = interviewParticipantRepository.findByInterviewId(id);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết buổi phỏng vấn theo ID")
    public ResponseEntity<ApiResponse<Interview>> getInterviewById(@PathVariable Long id) {
        return interviewRepository.findById(id)
            .map(interview -> ResponseEntity.ok(ApiResponse.success(interview)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Không tìm thấy buổi phỏng vấn.")));
}


}


