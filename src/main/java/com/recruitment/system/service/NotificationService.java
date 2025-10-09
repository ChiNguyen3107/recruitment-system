package com.recruitment.system.service;

import com.recruitment.system.dto.response.NotificationResponse;
import com.recruitment.system.entity.Notification;
import com.recruitment.system.enums.ApplicationStatus;
import com.recruitment.system.enums.NotificationType;
import com.recruitment.system.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Long userId, NotificationType type,
                            String title, String content, String actionUrl){
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setActionUrl(actionUrl);
        n.setRead(false);
        notificationRepository.save(n);
        log.info("Created notification for user {}: {}", userId, title);
    }


    public void notifyApplicationStatusChanged(Long applicantId, String jobTitle,
                                        ApplicationStatus newStatus, String actionUrl){
        createNotification(
                applicantId,
                NotificationType.APPLICATION_STATUS_CHANGED,
                "Cập nhật trạng thái đơn ứng tuyển",
                "Trạng thái đơn ứng tuyển cho vị trí "+jobTitle+ " đã được cập nhật thành " + newStatus,
                actionUrl
        );
    }

    public void notifyNewApplication(Long employerId, String jobTitle,
                              String applicantName, String actionUrl){
        createNotification(
                employerId,
                NotificationType.NEW_APPLICATION_RECEIVED,
                "Đơn ứng tuyển mới",
                applicantName + " vừa nộp đơn ứng tuyển cho vị trí " + jobTitle,
                actionUrl
        );
    }

    public void notifyJobDeadlineReminder(Long applicantId, String jobTitle,
                                   int daysLeft, String actionUrl){
        createNotification(
                applicantId,
                NotificationType.JOB_DEADLINE_REMINDER,
                "Tin tuyển dụng sắp hết hạn",
                "Tin tuyển dụng " + jobTitle + " sẽ hết hạn trong " + daysLeft + " ngày",
                actionUrl
        );
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long currentUserId, Boolean isRead,
                                           Pageable pageable){
        Page<Notification> page = (isRead == null)
                ? notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId,pageable)
                : notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(currentUserId, isRead, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public NotificationResponse markRead(Long currentUserId, Long notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }


    @Transactional
    public int markAllRead(Long currentUserId) {
        return notificationRepository.markAllRead(currentUserId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long currentUserId) {
        return notificationRepository.countByUserIdAndRead(currentUserId, false);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .actionUrl(n.getActionUrl())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
