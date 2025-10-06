package com.recruitment.system.dto.response;

import com.recruitment.system.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationTimelineResponse {
    private Long id;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private String note;
    private Long changedBy;
    private LocalDateTime changedAt;
}


