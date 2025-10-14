package com.recruitment.system.dto.response;

import com.recruitment.system.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private String actionUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
